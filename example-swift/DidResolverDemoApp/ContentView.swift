import SwiftUI

#if canImport(didFFI)
import didFFI
#endif


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

      RunLoop.main.perform(
        {
            //let did = "did:tdw:QmRjT8JCbQkEffVBWSbQd8nbMVNfAxiXStLPmqkQUWcsfv:gist.githubusercontent.com:vst-bit:32b64cfac9075b2a3ab7301b772bcdef:raw:8b4bd2b715101d5b69b3395f5c560c37e1ae9992"
            let did = "did:webvh:QmXi8p2LNXA6kbc2brwdpXwGETHCrPoFk15yPbLaAu27Pj:gist.githubusercontent.com:vst-bit:20c3f59d8179e324a6e29aef45240db4:raw:7870280f80dfcfb7459ee1488df4ab33f2bcf709"
            let didObj = try? Did(did: did); // may throw DidResolveError

            // make a HTTP GET request to get the did log
            let url = try? didObj?.getUrl(); // may throw DidResolveError
            
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

                // The `resolve` method is @deprecated as of 2.2.0 replaced by more potent `resolve_all`
                guard let didDocExtended = try? didObj?.resolveAll(didLog: didLog) else { // may throw DidResolveError
                    self.message = "NOK"
                    fatalError("Resolution failed")
                }
                self.message = "NOK"
                //let verifMethod = didDocExtended.getDidDoc().getVerificationMethod();
                let didDocId = didDocExtended.getDidDoc().getId();

                //self.message = didDocId
                self.message = "DID OK -> " + didDocId
            }
            task.resume()
        }
      )

      RunLoop.main.run(until: Date().addingTimeInterval(5)) // wait up a bit...
  }
}
