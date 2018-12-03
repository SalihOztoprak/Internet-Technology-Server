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
        while (true) {
            try {
                String message = Main.readMessage(socket.getInputStream());
                Main.sendMessage(socket.getOutputStream(), "+OK " + Main.encodeMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
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
}
