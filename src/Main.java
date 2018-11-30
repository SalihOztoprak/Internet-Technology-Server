import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

public class Main {
    private final static int PORT = 1337;
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

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

                sendMessage(os, "HELO");
                String message = readMessage(is);
                sendMessage(os, "+OK " + encodeMessage(message));

                String username = null;
                if (message != null) {
                    username = message.replace("HELO ", "");
                }

                ClientHandler clientHandler = new ClientHandler(username, socket);
                clientHandlers.add(clientHandler);
                clientHandler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(ClientHandler handler, String message) {

    }

    public static void sendMessage(OutputStream outputStream, String message) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            writer.println(message);
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String readMessage(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeMessage(String message){
        return Base64.getEncoder().encodeToString(MD5.getMd5(message));
    }
}
