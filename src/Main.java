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

    public static void broadcastMessage(ClientHandler handler, String message) throws IOException {
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.equals(handler)) {
                String msg;
                if (handler != null) {
                    msg = message.replace("BCST ", "");
                    msg = handler.getUsername() + ": " + msg;
                    msg = "BCST " + msg;
                } else {
                    msg = message;
                }

                sendMessage(clientHandler.getSocket().getOutputStream(), msg);
            }
        }
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
            String readLine = reader.readLine();
            if (readLine.equals("PONG ")) {
                String[] trimmedString = readLine.split(" ");
                readLine = trimmedString[0];
            }
            return readLine;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeMessage(String message) {
        return Base64.getEncoder().encodeToString(MD5.getMd5(message));
    }
}
