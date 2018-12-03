import java.io.IOException;
import java.net.Socket;
import java.util.logging.SocketHandler;

public class DisconnectIdle extends Thread {
    private Socket socket;

    public DisconnectIdle(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                Thread.sleep(60000);
                Main.sendMessage(socket.getOutputStream(), "PING");

                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    String rsp = Main.readMessage(socket.getInputStream());
                    assert rsp != null;
                    if (rsp.equals("PONG")) {
                        i = 10;
                    }
                    if (i == 9){
                        socket.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
