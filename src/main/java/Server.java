import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    private int port;

    private String dir;

    public Server(int port, String dir) {
        this.port = port;
        this.dir = dir;
    }

    void start() {
        try(ServerSocket server = new ServerSocket(this.port)) {
            while(true) {
                Socket socket = server.accept();
                var thread = new Handler(socket, dir);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    //запускаем сервер
    public static void main(String ... args) {
        Scanner in = new Scanner(System.in);

        System.out.print("Введите номер порта: ");
        int port = in.nextInt();
        System.out.print("Введите директорию: ");
        var directory = in.next();
        new Server(port, directory).start();
        System.out.println("Server started!");
    }

}
