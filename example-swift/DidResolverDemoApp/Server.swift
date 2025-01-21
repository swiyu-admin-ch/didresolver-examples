import Network
import Foundation

final class Server {
    let listener: NWListener

    init(port: NWEndpoint.Port) {
        self.listener = try! NWListener(using: .tcp, on: port)

        listener.stateUpdateHandler = {
            print("listener.state = \($0)")
        }

        listener.newConnectionHandler = handleConnection

        listener.start(queue: .main)
    }

    func handleConnection(_ connection: NWConnection) {
        print("New connection!")

        connection.stateUpdateHandler = {
            print("connection.state = \($0)")
        }
        connection.start(queue: .main)
        receive(from: connection)
    }

    func receive(from connection: NWConnection) {
        connection.receive(
            minimumIncompleteLength: 1,
            maximumLength: connection.maximumDatagramSize
        ) { content, _, isComplete, error in

            if let error {
                print("Error: \(error)")
            } else if let content, let request = Request(content) {
                print("Received request!")
                print(request)
                self.respond(on: connection)
            }

            if !isComplete {
                self.receive(from: connection)
            }
        }
    }
 
    func respond(on connection: NWConnection) {
        let url = Bundle.main.url(forResource: "TestDidLog", withExtension: "json")
        let response = try! Response(file: url!)

        connection.send(content: response.messageData, completion: .idempotent)
    }
}
