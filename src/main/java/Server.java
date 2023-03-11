import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final Map<String, Map<String, Handler>> handlers;

    public Server(int poolSize) {
        handlers = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);
    }

    public Map<String, Map<String, Handler>> getHandlers() {
        return handlers;
    }

    public void listen(int port) {

        try (final var serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен.");
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> connectionSocket(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }


    public void connectionSocket(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = new Request(in, out);
            var handler = handlers.get(request.getMethod()).get(request.getPath());
            handler.handle(request, out);

            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
