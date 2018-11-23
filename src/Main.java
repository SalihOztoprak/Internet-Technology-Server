import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        new Main().run();
    }

    public void run(){
        connectToServer();
    }

    public static void connectToServer() {
        try (ServerSocket serverSocket = new ServerSocket(1337)) {
            Socket connectionSocket = serverSocket.accept();
        } catch (IOException ioe) {
            System.out.println(ioe.getStackTrace());
        }
    }
}
