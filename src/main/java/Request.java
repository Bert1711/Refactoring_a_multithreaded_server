import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String protocol;
    private Map<String, String> headers;
    private String body;
    private Map<String, String> queryParams;

    public Request(BufferedReader in, BufferedOutputStream out) throws IOException {
        headers = new HashMap<>();
        queryParams = new HashMap<>();
        parse(in, out);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    private void parse(BufferedReader in, BufferedOutputStream out) throws IOException {
        String line = in.readLine();
        String[] requestLine = line.split(" ");

        if (requestLine.length != 3) {
            out.write((
                    "HTTP/1.1 400 Bad Request\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return;
        }

        String[] pathAndQuery = requestLine[1].split("\\?", 2);
        path = pathAndQuery[0];

        if (pathAndQuery.length > 1) {
            List<NameValuePair> params = URLEncodedUtils.parse(pathAndQuery[1], StandardCharsets.UTF_8);
            for (NameValuePair param : params) {
                queryParams.put(param.getName(), param.getValue());
            }
        }

        method = requestLine[0];
        protocol = requestLine[2];
        body = null;

        final var validPaths = ValidPaths.getValidPaths("valid-paths.txt");
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return;
        }

        boolean inBody = false;
        StringBuilder bodyBuilder = new StringBuilder();
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                inBody = true;
                continue;
            }

            if (line.contains(": ") && !inBody) {
                String[] header = line.split(": ");
                headers.put(header[0], header[1]);
            }

            if (inBody) {
                int bodyLength = Integer.parseInt(headers.getOrDefault("Content-length", "4096"));
                char[] buffer = new char[bodyLength];
                int bytesRead = 0;
                while (bytesRead < bodyLength) {
                    int count = in.read(buffer, bytesRead, bodyLength - bytesRead);
                    if (count == -1) {
                        break;
                    }
                    bytesRead += count;
                }
                body = new String(buffer);

            }
        }
    }
}



