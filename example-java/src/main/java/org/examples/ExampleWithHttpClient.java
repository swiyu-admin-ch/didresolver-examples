package org.examples;

import ch.admin.bj.swiyu.didtoolbox.Ed25519VerificationMethodKeyProviderImpl;
import ch.admin.bj.swiyu.didtoolbox.TdwCreator;
import ch.admin.bj.swiyu.didtoolbox.VerificationMethodKeyProvider;
import ch.admin.eid.didresolver.Did;
import ch.admin.eid.didresolver.DidResolveException;
import ch.admin.eid.didtoolbox.TrustDidWeb;
import ch.admin.eid.didtoolbox.TrustDidWebException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.RequestDefinition;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class ExampleWithHttpClient {

    private static ClientAndServer mockServer = ClientAndServer.startClientAndServer(8080);

    public static void main(String[] args) throws IOException, InterruptedException, TrustDidWebException, URISyntaxException {

        // Retrieve key for did doc manipulations
        //final var eddsaKeyPair = Ed25519KeyPair.Companion.generate();

        //var verificationMethodKeyProvider = Ed25519VerificationMethodKeyProviderImpl(File("private-key.pem"), File("public-key.pem"));
        var verificationMethodKeyProvider = new Ed25519VerificationMethodKeyProviderImpl(
                "z6Mkw9HFnueQzPrbcD5DzSsPswzKL1Ut4ExYwcbivPwcFPzf",
                "z6MkvdAjfVZ2CWa38V2VgZvZVjSkENZpiuiV5gyRKsXDA8UP");

        // REPLACE this line with the custom logic how the did tdw log is published
        var didTdw = setupMockServer(verificationMethodKeyProvider);

        // Resolve did to did doc
        Did did = null;
        String didLog = "";
        try {
                did = new Did(didTdw); // may throw DidResolveException
                // make a HTTP GET request to get the did log
                var url = did.getUrl(); // may throw DidResolveException
                didLog = fetchDidLogUsingHttpClient(url);
        } catch (URISyntaxException|DidResolveException e) {
                throw new RuntimeException(e);
        } finally {
            if (did != null) {
                did.close();
            }
        }
        var didTdwRead = TrustDidWeb.Companion.read(didTdw, didLog);
        String didDocStr = didTdwRead.getDidDoc();
        System.out.println(didDocStr);

        // Add assertion key for credential issuing
        var didDoc = JsonParser.parseString(didDocStr).getAsJsonObject();
        var assertionMethod = new JsonObject();
        assertionMethod.add("id", new JsonPrimitive(didTdw + "#issuing"));
        assertionMethod.add("controller", new JsonPrimitive(didTdw));
        assertionMethod.add("type", new JsonPrimitive("JsonWebKey2020"));
        // TODO assertionMethod.add("publicKeyJwk", new JsonPrimitive(bbsKeyPair.getPublicKey().toMultibase()));
        var verificationMethod = didDoc.get("verificationMethod").getAsJsonArray();
        verificationMethod.add(assertionMethod);
        didDoc.add("verificationMethod", verificationMethod);

        /*
        // TODO Try to update DID log
        
        // REPLACE this line with the custom logic how the did tdw log is update
        String didTdwUpdateDidLog = didTdwUpdate.getDidLog();

        restartMockServer(didTdwUpdateDidLog);
        
        // Get modified did doc
        String modifiedDidDocStr = TrustDidWeb.Companion.read(didTdw, didTdwUpdateDidLog, true).getDidDoc();
        System.out.println(modifiedDidDocStr);
         */

        System.exit(0);
    }

    /**
     * Yet another handy private helper intended for testing purposes only.
     */
    private static String setupMockServer(VerificationMethodKeyProvider verificationMethodKeyProvider) throws TrustDidWebException, MalformedURLException, IOException, URISyntaxException {

        // Create did with did doc
        String issuerId = "someIssuerIdNotReallyRelevantInThisContext";

        var didLog = TdwCreator.builder()
                .verificationMethodKeyProvider(verificationMethodKeyProvider)
                .build()
                .create(URL.of(new URI(String.format("http://localhost:%d/%s", mockServer.getLocalPort(), issuerId)), null));

        mockServer.when(new RequestDefinition() {
                @Override
                public RequestDefinition shallowClone() {
                        return null;
                }
        }).respond(new HttpResponse().withBody(didLog));

        return JsonParser.parseString(didLog).getAsJsonArray().get(3).getAsJsonObject().get("value").getAsJsonObject().get("id").getAsString();
    }

    private static void restartMockServer(String body) {

        var port = mockServer.getPort();
        mockServer.stop();
        mockServer = ClientAndServer.startClientAndServer(port);

        var prettyJsonBody = (new GsonBuilder().setPrettyPrinting().create()).toJson(body);

        mockServer.when(new RequestDefinition() {
                @Override
                public RequestDefinition shallowClone() {
                        return null;
                }
        }).respond(new HttpResponse().withBody(prettyJsonBody));
    }

    private static String fetchDidLogUsingHttpClient(String url) throws IOException, InterruptedException, URISyntaxException {

        var request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        var response = HttpClient
                .newBuilder()
                .build()
                .send(request, BodyHandlers.ofString());

        return response.body();
    }
}