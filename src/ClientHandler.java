import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler extends Thread {
    private boolean running = true;
    private final String username;
    private final Socket socket;
    private Timer timer;


    public ClientHandler(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        pingSender();
        inactiveTimer();
        while (running) {
            try {
                String message = Main.readMessage(socket,this);
                if (message != null) {
                    System.out.println("Resetting the timer");
                    inactiveTimer();
                    if (!message.equals("PONG ")) {
                        Main.broadcastMessage(this, message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pingSender() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        if (running) {
                            Main.sendMessage(socket, "PING");
                        } else {
                            this.cancel();
                        }
                    }
                },
                60000,
                60000);
    }

    private void inactiveTimer() {
        try {
            timer.cancel();
        } catch (NullPointerException npe){
            System.out.println("No timer found, creating one...");
        }
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Close this maddafakka");
                timer.cancel();
                disconnect();
            }
        };
        timer.schedule(timerTask, 63000);
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    private void disconnect(){
        running = false;
        Main.kickClient(this);
    }
}
