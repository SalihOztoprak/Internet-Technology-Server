import java.net.Socket;
import java.util.ArrayList;

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
                //TODO Make the pingpong system repeat
                Thread.sleep(60000);
                Main.sendMessage(socket, "PING");
                String rsp = Main.readMessage(socket);
                assert rsp != null;
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    assert rsp != null;
                    if (rsp.equals("PONG")) {
                        i = 10;
                    }
                    if (i == 9) {
//                        socket.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
