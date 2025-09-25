import SwiftUI

import Swifter

import DidResolver

import JsonSchemaValidator

// MARK: - ContentView

struct ContentView: View {

  @State private var message = ""

  var body: some View {
      let _ = Self._printChanges()
    VStack {
      Image(systemName: "globe")
        .imageScale(.large)
        .foregroundStyle(.tint)
      Text(message)
    }
    .padding()
    .onAppear {
        
        let server = HttpServer()
        // CAUTION The path MUST match the one residing in the TdwDidLog.jsonl resource
        server["/123456789/:path"] = shareFile(Bundle.main.resourcePath! + "/TdwDidLog.jsonl")
        let serverTask = Task {
            do {
                // CAUTION The port MUST match the one residing in the TdwDidLog.jsonl resource
                try server.start(54858, forceIPv4: true)
                print("Server has started. Try visiting http://localhost:\(try server.port())\(server.routes[0])/did.jsonl")
            } catch {
                print("Server start error: \(error)")
            }
        }
        
        tryDidResolver()
        
        server.stop()
        serverTask.cancel()
    }
  }
}

extension ContentView {

  func tryDidResolver() {

    // CAUTION didTdw MUST match the one residing in the TdwDidLog.jsonl resource
    let didTdw = "did:tdw:QmUSyQohHF4tcRhdkJYoamuMQAXQmYBoFLCot35xd7dPda:127.0.0.1%3A54858:123456789"
    let did = try? Did(did: didTdw); // may throw DidResolveError

    // make a HTTP GET request to get the did log
    let httpsUrl = try? did?.getUrl(); // may throw DidResolveError
    print("DID URL: " + httpsUrl!)
    let url = httpsUrl?.replacingOccurrences(of: "https", with: "http") // since our HttpServer is HTTP-only

    var didLog = ""
    let task = URLSession.shared.dataTask(with: URL(string: url!)!) {(data, response, error) in
        guard let data = data else {
            self.message = "\n❌ DID unavailable due to: \n\n" + error!.localizedDescription
            return
        }

        didLog = String(data: data, encoding: .utf8)!

        /*
        if let jsonData = didLog.data(using: String.Encoding.utf8) {
            do {
                _ = try JSONSerialization.jsonObject(with: jsonData)
                    //print("JSON OK: \(x.self)")
                } catch {
                    fatalError("Invalid JSON: \(error.localizedDescription)")
            }
        }
         */
        
        // CAUTION The validation here is for illustration purposes only - it is already part of the DID resolution process!
        let tdwJsonSchemaURL = URL(string: "https://raw.githubusercontent.com/swiyu-admin-ch/did-tdw/main/src/embed/jsonschema/did_log_jsonschema_v_0_3_eid_conform.json");
        //let webvhJsonSchemaURL = URL(string: "https://raw.githubusercontent.com/swiyu-admin-ch/did-webvh/main/src/embed/jsonschema/did_log_jsonschema_v_1_0_eid_conform.json");
        let jsonSchemaFromURL = try? String(contentsOf: tdwJsonSchemaURL!);
        let validator = try? Validator(schema: jsonSchemaFromURL!); // may throw ValidatorError::InvalidSchema
        let isValid = try? validator?.isValid(instance: didLog); // may throw ValidatorError::DeserializationError
        if !isValid! {
            fatalError("The DID log is NOT JSON-schema-conform")
        }

        guard let didDocExtended = try? did?.resolveAll(didLog: didLog) else { // may throw DidResolveError
            fatalError("Resolution failed")
        }
        
        let didDoc = didDocExtended.getDidDoc();
        let didDocId = didDoc.getId();
        print("DID Doc ID: " + didDocId)

        self.message = "\n✅ DID OK (\(Date.now.ISO8601Format())) -> \n\n" + didDocId
    }

    task.resume()
  }
}
