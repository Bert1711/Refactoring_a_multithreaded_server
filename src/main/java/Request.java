import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private String method;
    private String path;
    private String protocol;
    private Map<String, String> headers;
    private String body;





    public Request(BufferedReader in, BufferedOutputStream out) throws IOException {
        headers = new HashMap<>();
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
            return;  // Завершит метод connectionSocket
        }

        method = requestLine[0];
        path = requestLine[1];
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
                // пустая строка означает начало тела запроса
                inBody = true;
                continue;
            }


            if (line.contains(": ") && !inBody) {
                String[] header = line.split(": ");
                headers.put(header[0], header[1]);
            }


            if (line.isEmpty()) {
                // пустая строка означает начало тела запроса
                inBody = true;
                continue;
            }
            if (inBody) {
                int bodyLength = Integer.parseInt(headers.getOrDefault("Content-length", "1024"));
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


