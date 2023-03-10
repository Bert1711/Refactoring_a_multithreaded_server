public class Main {
    public static void main(String[] args) {
        int poolSize = 64;
        int port = 9999;
        Server server1 = new Server(poolSize);
        server1.listen(port);
    }
}
