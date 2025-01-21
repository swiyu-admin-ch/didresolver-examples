import Foundation

struct Request {
    let method: String          // "GET"
    let path: String            // "/foo/bar"
    let httpVersion: String     // "HTTP/1.1"
    let headers: [String: String]

    init?(_ data: Data) {
        let str = String(data: data, encoding: .utf8)!
        let lines = str.components(separatedBy: "\r\n")
        guard let firstLine = lines.first,
              let lastLine = lines.last, lastLine.isEmpty else {
            return nil
        }

        let parts = firstLine.components(separatedBy: " ")
        guard parts.count == 3 else {
            return nil
        }

        self.method = parts[0]
        self.path = parts[1].removingPercentEncoding!
        self.httpVersion = parts[2]

        let headerPairs = lines.dropFirst()
            .map { $0.split(separator: ":", maxSplits: 1) }
            .filter { $0.count == 2 }
            .map { ($0[0].lowercased(), $0[1].trimmingCharacters(in: .whitespaces)) }

        self.headers = Dictionary(headerPairs, uniquingKeysWith: { old, _ in old })
    }
}
