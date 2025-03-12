
import ch.admin.bj.swiyu.didtoolbox.Ed25519VerificationMethodKeyProviderImpl
import ch.admin.bj.swiyu.didtoolbox.TdwCreator
import ch.admin.eid.didresolver.Did
import ch.admin.eid.didresolver.DidResolveException
import ch.admin.eid.didtoolbox.TrustDidWeb
import ch.admin.eid.didtoolbox.TrustDidWebException
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import org.mockserver.model.HttpResponse
import org.mockserver.model.RequestDefinition
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import kotlin.system.exitProcess

private var mockServer = org.mockserver.integration.ClientAndServer.startClientAndServer(8080)

@Throws(TrustDidWebException::class)
fun main() {

    // Retrieve key for did doc manipulations
    //val eddsaKeyPair = Ed25519KeyPair.generate()

    //var verificationMethodKeyProvider = Ed25519VerificationMethodKeyProviderImpl(File("private-key.pem"), File("public-key.pem"));
    var verificationMethodKeyProvider = Ed25519VerificationMethodKeyProviderImpl(
        "z6Mkw9HFnueQzPrbcD5DzSsPswzKL1Ut4ExYwcbivPwcFPzf",
        "z6MkvdAjfVZ2CWa38V2VgZvZVjSkENZpiuiV5gyRKsXDA8UP");

    // REPLACE this line with the custom logic how the did tdw log is published
    var didTdw = setupMockServer(verificationMethodKeyProvider)

    // Resolve did to did doc
    var did: Did = null
    var didLog = ""
    try {
        did = Did(didTdw) // may throw DidResolveException
        // make a HTTP GET request to get the did log
        val url = did.getUrl() // may throw DidResolveException
        didLog = fetchDidLogContent(url)
    } catch (e: Exception) {
        when (e::class) {
            is URISyntaxException -> throw RuntimeException(e)
            is DidResolveException -> throw RuntimeException(e)
        }
    } finally {
        did.close()
    }
    val didTdwRead = TrustDidWeb.read(didTdw, didLog)
    val didDocStr = didTdwRead.getDidDoc()

    // Add assertion key for credential issuing
    val didDoc = JsonParser.parseString(didDocStr.toString()).asJsonObject
    val assertionMethod = JsonObject()
    assertionMethod.add("id", JsonPrimitive("$didTdw#issuing"))
    assertionMethod.add("controller", JsonPrimitive(didTdw))
    assertionMethod.add("type", JsonPrimitive("Bls12381G2Key2020"))
    // TODO assertionMethod.add("publicKeyMultibase", JsonPrimitive(bbsKeyPair.getPublicKey().toMultibase()))
    val verificationMethod = didDoc.get("verificationMethod").asJsonArray
    verificationMethod.add(assertionMethod)
    didDoc.add("verificationMethod", verificationMethod)

    println(didDocStr)

    /*
    // TODO Update DidDoc
    var didTdwUpdate = TrustDidWeb.update(didTdw, didLog, didDoc.toString(), eddsaKeyPair, null);

    // REPLACE this line with the custom logic how the did tdw log is update
    //var didTdwUpdateDidLog = didTdwUpdate.getDidLog();

    restartMockServer(didTdwUpdateDidLog);

    // Get modified did doc
    val modifiedDidDocStr = TrustDidWeb.read(didTdw, didTdwUpdateDidLog, null)
    println(modifiedDidDocStr)
     */

    exitProcess(0)
}

@Throws(TrustDidWebException::class)
private fun setupMockServer(
    verificationMethodKeyProvider: Ed25519VerificationMethodKeyProviderImpl
): String {
    // Create did with did doc

    val issuerId = "someIssuerIdNotReallyRelevantInThisContext"

    val tdwDidLog = TdwCreator.builder()
        .verificationMethodKeyProvider(verificationMethodKeyProvider)
        .build()
        .create(URL.of(URI("http://localhost:%d/%s".format(mockServer.getLocalPort(), issuerId)), null));

    mockServer.`when`(object : RequestDefinition() {
        override fun shallowClone(): RequestDefinition? {
            return null
        }
    }).respond(HttpResponse().withBody(tdwDidLog))

    return JsonParser.parseString(tdwDidLog).asJsonArray[3].asJsonObject["value"].asJsonObject["id"].asString
}

private fun restartMockServer(body: String) {

    val port = mockServer.port
    mockServer.stop()
    mockServer = org.mockserver.integration.ClientAndServer.startClientAndServer(port)

    val prettyJsonBody = (GsonBuilder().setPrettyPrinting().create()).toJson(body)

    val reqDef = object : RequestDefinition() {
        override fun shallowClone(): RequestDefinition? {
            return null
        }
    }
    mockServer.`when`(reqDef).respond(HttpResponse().withBody(prettyJsonBody))
}

@Throws(IOException::class, InterruptedException::class, URISyntaxException::class)
private fun fetchDidLogContent(url: String): String {
    val request = HttpRequest.newBuilder()
        .uri(URI(url))
        .GET()
        .build()

    val response = HttpClient
        .newBuilder()
        .build()
        .send(request, BodyHandlers.ofString())

    return response.body()
}