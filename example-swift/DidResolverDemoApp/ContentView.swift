import SwiftUI

// MARK: - ContentView

struct ContentView: View {

  @State private var message = "Hello, world!"

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
        tryDidResolver()
    }
  }
}

extension ContentView {

  func tryDidResolver() {

      // CAUTION didTdw MUST match the one residing in the TestDidLog resource
      let _ = Server(port: 54858)

      RunLoop.main.perform(
        {
            // CAUTION didTdw MUST match the one residing in the TestDidLog resource
            let didTdw = "did:tdw:QmUSyQohHF4tcRhdkJYoamuMQAXQmYBoFLCot35xd7dPda:127.0.0.1%3A54858:123456789"
            //let did = try? Did(didTdw: didTdw); // may throw DidResolveError
            let did = try? Did(didTdw: didTdw); // may throw DidResolveError

            // make a HTTP GET request to get the did log
            let url = try? did?.getUrl(); // may throw DidResolveError

            var didLog = ""
            let task = URLSession.shared.dataTask(with: URL(string: url!)!) {(data, response, error) in
                guard let data = data else { return }

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

                guard let didDoc = try? did?.resolve(didTdwLog: didLog) else { // may throw DidResolveError
                    self.message = "NOK"
                    fatalError("Resolution failed")
                }
                //let verifMethod = didDoc.getVerificationMethod();
                let didDocId = didDoc.getId();

                //self.message = didDocId
                self.message = "DID OK -> " + didDocId
            }
            task.resume()
        }
      )

      RunLoop.main.run(until: Date().addingTimeInterval(5)) // wait up a bit...
  }
}
