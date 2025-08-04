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
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.system.exitProcess

object ExampleWithHttpClient {
    private var mockServer = org.mockserver.integration.ClientAndServer.startClientAndServer(8080)

    /**
     * Configures SSL to trust all certificates and bypass hostname verification.
     * This allows the application to connect to servers with untrusted or self-signed certificates.
     * WARNING: This approach is insecure for production use.
     */
    init {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }

                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
                    // Do nothing - trust all clients
                }

                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
                    // Do nothing - trust all servers
                }
            }
        )

        var sc: SSLContext
        try {
            sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
            //} catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw RuntimeException(e)
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // Retrieve key for did doc manipulations
        //val eddsaKeyPair = Ed25519KeyPair.generate()

        // REPLACE this line with the custom logic how the did tdw log is published
        var didTdw = setupMockServer()

        // Resolve did to did doc
        var did: Did? = null
        var didTdwRead: TrustDidWeb? = null
        try {
            did = Did(didTdw) // may throw DidResolveException
            // make a HTTP GET request to get the did log
            val url = did.getUrl() // may throw DidResolveException
            val didLog = fetchDidLog(url)
            didTdwRead = TrustDidWeb.read(didTdw, didLog)
        } catch (e: Exception) {
            when (e::class) {
                is URISyntaxException -> throw RuntimeException(e)
                is DidResolveException -> throw RuntimeException(e)
                is TrustDidWebException -> throw RuntimeException(e)
            }
        } finally {
            did?.close()
        }
        val didDocStr = didTdwRead?.getDidDoc()

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
    private fun setupMockServer(): String {
        // Create did with did doc

        val issuerId = "did18fa7c77-9dd1-4e20-a147-fb1bec146085"

        val tdwDidLog = TdwCreator.builder()
            .verificationMethodKeyProvider(
                Ed25519VerificationMethodKeyProviderImpl(
                    FileInputStream("src/test/data/mykeystore.jks"),
                    "changeit",
                    "myalias",
                    "changeit"
                )
            )
            .forceOverwrite(true)
            .build()
            .create(
                URL.of(
                    URI(
                        "http://localhost:%d/%s".format(
                            mockServer.getLocalPort(),
                            issuerId
                        )
                    ), null
                )
            );

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

    @Throws(IOException::class, URISyntaxException::class)
    private fun fetchDidLog(url: String): String {
        val content = StringBuilder()

        val connection = (URI(url).toURL().openConnection() as HttpsURLConnection)
        connection.inputStream.bufferedReader().use { reader ->
            var inputLine: String?
            while (reader.readLine().also { inputLine = it } != null) {
                content.append(inputLine)
            }
        }

        return content.toString()
    }
}