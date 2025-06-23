package org.examples;

import ch.admin.bj.swiyu.didtoolbox.Ed25519VerificationMethodKeyProviderImpl;
import ch.admin.bj.swiyu.didtoolbox.TdwCreator;
import ch.admin.bj.swiyu.didtoolbox.TdwUpdaterException;
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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

public class ExampleWithHttpClient {

    private static ClientAndServer mockServer = ClientAndServer.startClientAndServer(8080);

    /**
     * Configures SSL to trust all certificates and bypass hostname verification.
     * This allows the application to connect to servers with untrusted or self-signed certificates.
     * WARNING: This approach is insecure for production use.
     */
    static {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing - trust all clients
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing - trust all servers
                    }
                }
        };

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    public static void main(String[] args) {

        String didTdw = null;
        try {
            didTdw = setupMockServer();
        } catch (InvalidKeySpecException | IOException | TrustDidWebException | URISyntaxException | KeyStoreException |
                 CertificateException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyException |
                 TdwUpdaterException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Resolve did to did doc
        Did did = null;
        String didLog = "";
        TrustDidWeb didTdwRead = null;
        try {
            did = new Did(didTdw); // may throw DidResolveException
            // make a HTTP GET request to get the did log
            var url = did.getUrl(); // may throw DidResolveException
            didLog = fetchDidLog(url); // may throw IOException, URISyntaxException
            didTdwRead = TrustDidWeb.Companion.read(didTdw, didLog); // may throw TrustDidWebException
        } catch (DidResolveException | IOException | URISyntaxException | TrustDidWebException e) {
            throw new RuntimeException(e);
        } finally {
            if (did != null) {
                did.close();
            }
        }
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
    private static String setupMockServer() throws TrustDidWebException, IOException, URISyntaxException, InvalidKeySpecException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyException, TdwUpdaterException {

        // Create did with did doc
        String issuerId = "did18fa7c77-9dd1-4e20-a147-fb1bec146085";

        var didLog = TdwCreator.builder()
                /*.verificationMethodKeyProvider(new Ed25519VerificationMethodKeyProviderImpl(
                        new File("src/test/data/id_ed25519"),
                        new File("src/test/data/id_ed25519.pub")))*/
                .verificationMethodKeyProvider(new Ed25519VerificationMethodKeyProviderImpl(
                        new FileInputStream("src/test/data/mykeystore.jks"), "changeit", "myalias", "changeit"))
                .forceOverwrite(true) // to avoid The PEM file(s) exist(s) already and will remain intact until overwrite mode is engaged: .didtoolbox/auth-key-01
                .build()
                .create(URL.of(new URI(String.format("http://localhost:%d/%s", mockServer.getLocalPort(), issuerId)), null));

        /*
        var updatedDidLog = new StringBuilder(didLog)
                .append(System.lineSeparator())
                .append(TdwUpdater.builder()
                        .verificationMethodKeyProvider(new Ed25519VerificationMethodKeyProviderImpl(
                                new FileInputStream("src/test/data/mykeystore.jks"), "changeit", "myalias", "changeit"))
                        .assertionMethodKeys(Map.of("my-assert-key-01", JwkUtils.loadECPublicJWKasJSON(new File("src/test/data/assert-key-01.pub"), "my-assert-key-01")))
                        .authenticationKeys(Map.of("my-auth-key-01", JwkUtils.loadECPublicJWKasJSON(new File("src/test/data/auth-key-01.pub"), "my-auth-key-01")))
                        //.updateKeys(Set.of(new File("src/test/data/public.pem")))
                        .build()
                        .update(initialDidLogEntry)).toString();
         */

        mockServer.when(new RequestDefinition() {
            @Override
            public RequestDefinition shallowClone() {
                return null;
            }
        }).respond(new HttpResponse().withBody(didLog));

        return JsonParser.parseString(didLog.lines().toList().getFirst()).getAsJsonArray().get(3).getAsJsonObject().get("value").getAsJsonObject().get("id").getAsString();
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

    private static String fetchDidLog(String url) throws IOException, URISyntaxException {

        StringBuilder content = new StringBuilder();

        // Open HTTPS connection and read content
        HttpsURLConnection connection = (HttpsURLConnection) (new URI(url)).toURL().openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }

        return content.toString();
    }
}
