import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

public class Main {
    private final static int PORT = 1337;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        connectToServer();
    }

    private static void connectToServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();

                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                // Block thread until socket input has been read.
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

                writer.println("HELO");
                writer.flush();

                String line = reader.readLine();
                final String encodedMessage = Base64.getEncoder().encodeToString(MD5.getMd5(line));
                writer.println("+OK " + encodedMessage);
                ClientHandler clientHandler = new ClientHandler(is,os,socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
