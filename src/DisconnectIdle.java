import java.net.Socket;

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
                Thread.sleep(6000);
                Main.sendMessage(socket.getOutputStream(), "PING");
                String rsp = Main.readMessage(socket.getInputStream());
                //TODO The heartbeat doesn't work !


                for (int i = 0; i < 10; i++) {
//                    Thread.sleep(1000);
                    assert rsp != null;
                    if (rsp.equals("PONG")) {
                        i = 10;
                    }
                    if (i == 9) {
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
