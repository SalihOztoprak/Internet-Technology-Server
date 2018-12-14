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

                sendMessage(socket, "HELO");
                String message = readMessage(socket);
                sendMessage(socket, "+OK " + encodeMessage(message));
                sendMessage(socket, "BCST To view all commands, type /help");

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

    public static void broadcastMessage(ClientHandler handler, String message) {
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

                sendMessage(clientHandler.getSocket(), msg);
            }
        }
    }

    public static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            writer.println(message);
            writer.flush();
        } catch (Exception e) {
            System.out.println("Can't send message since client has disconnected");
        }
    }

    public static String readMessage(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readLine = reader.readLine();
            if (readLine != null) {
                if (readLine.startsWith("BCST /")) {
                    Commands.checkCommand(socket, readLine);
                }
            }
            return readLine;
        } catch (IOException e) {
            System.out.println("Can't read message since client has disconnected");
        }
        return null;
    }

    public static String encodeMessage(String message) {
        return Base64.getEncoder().encodeToString(MD5.getMd5(message));
    }

    public static void kickClient(ClientHandler handler) {
        for (int i = 0; i < clientHandlers.size(); i++) {
            if (clientHandlers.get(i).equals(handler)) {
                clientHandlers.remove(i);
                break;
            }
        }

        try {
            handler.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.interrupt();
    }
}
