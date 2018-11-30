import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ClientHandler extends Thread {
    private DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
    private DateFormat time = new SimpleDateFormat("hh:mm:ss");
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Socket socket;

    public ClientHandler(InputStream dis, OutputStream dos, Socket s) {
        this.inputStream = dis;
        this.outputStream = dos;
        this.socket = s;
    }

    @Override
    public void run(){

    }

    public DateFormat getDate() {
        return date;
    }

    public DateFormat getTime() {
        return time;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public Socket getSocket() {
        return socket;
    }
}
