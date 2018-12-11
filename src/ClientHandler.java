import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;

public class ClientHandler extends Thread {
    private DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
    private DateFormat time = new SimpleDateFormat("hh:mm:ss");
    private final String username;
    private final Socket socket;

    public ClientHandler(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        DisconnectIdle disconnectIdle = new DisconnectIdle(socket);
        disconnectIdle.start();
        while (true) {
            try {
                String message = Main.readMessage(socket);
                if (!message.equals("PONG")) {
                    Main.sendMessage(socket, "+OK " + Main.encodeMessage(message));
                    Main.broadcastMessage(this, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public DateFormat getDate() {
        return date;
    }

    public DateFormat getTime() {
        return time;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }
}
