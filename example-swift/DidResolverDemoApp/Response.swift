import UniformTypeIdentifiers

struct Response {
    let httpVersion = "HTTP/1.1"
    let status: Status
    let headers: [Header: String]
    let body: Data

    enum Status: Int, CustomStringConvertible {
        case ok = 200
        case notFound = 404

        var description: String {
            switch self {
                case .ok: return "OK"
                case .notFound: return "Not Found"
            }
        }
    }

    enum Header: String {
        case contentLength = "Content-Length"
        case contentType = "Content-Type"
    }

    init(
        _ status: Status = .ok,
        headers: [Header: String] = [:],
        body: Data = Data(),
        contentType: UTType? = nil
    ) {
        self.status = status
        self.body = body
        self.headers = headers.merging(
            [
                .contentLength: String(body.count),
                .contentType: contentType?.preferredMIMEType,
            ].compactMapValues { $0 },
            uniquingKeysWith: { _, new in new }
        )
    }

    init(_ text: String, contentType: UTType = .plainText) {
        self.init(body: text.data(using: .utf8)!, contentType: contentType)
    }

    init(file url: URL) throws {
        try self.init(
            body: Data(contentsOf: url),
            contentType: url.resourceValues(forKeys: [.contentTypeKey]).contentType
        )
    }

    var messageData: Data {
        let statusLine = "\(httpVersion) \(status.rawValue) \(status)"

        var lines = [statusLine]
        lines.append(contentsOf: headers.map({ "\($0.key.rawValue): \($0.value)" }))
        lines.append("")
        lines.append("")    // adds extra blank line
        let header = lines.joined(separator: "\r\n").data(using: .utf8)!

        return header + body
    }
}
