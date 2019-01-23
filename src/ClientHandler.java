import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class will be used for creating a client on the server
 */
public class ClientHandler extends Thread {
    private boolean running = true;
    private String username;
    private final Socket socket;
    private Group group = null;
    private Timer timer;

    /**
     * This is the default constructor for this class
     *
     * @param username The username of the client
     * @param socket   The socket of the client
     */
    public ClientHandler(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
    }

    /**
     * This method will be executed when the client joins for the first time
     * The method is overridden from a thread so different users can join at the same time
     */
    @Override
    public void run() {
        String username = "";
        Main.sendMessage(socket, "HELO");
        String message = Main.readMessage(socket, null);

        if (message != null) {
            username = message.replace("HELO ", "");
            username = username.replace(" ", "_");
        }

        Main.sendMessage(socket, "+OK " + Main.encodeMessage(message));
        Main.sendMessage(socket, "BCST To view all commands, type /help");

        for (ClientHandler clientHandler1 : Main.getClientHandlers()) {
            if (clientHandler1.getUsername().equalsIgnoreCase(username)) {
                username = username + "(1)";
                Main.sendMessage(socket, "BCST Your name has already been taken, so we changed it to " + username);
                break;
            }
        }

        Main.broadcastMessage(null, "BCST " + username + " joined the server");

        for (ClientHandler client : Main.getClientHandlers()) {
            if (client.getUsername().equals(username)) {
                Main.sendMessage(socket, "The username " + username + " is already taken, please try a new name");
                username = message.replace("HELO ", "");
            }

        }

        this.username = username;

        pingSender();
        inactiveTimer();

        while (running) {
            try {
                String msg = Main.readMessage(socket, this);
                if (msg != null) {
                    inactiveTimer();
                    if (!msg.equals("PONG ")) {
                        Main.broadcastMessage(this, msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method sends a PING message every 60 seconds to make sure the client is still active
     */
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

    /**
     * This method checks if the client has responded with PONG or another message atleast once every 63 seconds
     * The server kicks the user automatically if the user is idle for more than 63 seconds
     */
    private void inactiveTimer() {
        try {
            timer.cancel();
        } catch (NullPointerException npe) {
            System.out.println("No pingpong timer found for " + username + ", creating one...");
        }
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Closed the connection of " + username + " because he was idle.");
                timer.cancel();
                disconnect();
            }
        };
        timer.schedule(timerTask, 63000);
    }

    /**
     * This method is used to disconnect the client
     */
    private void disconnect() {
        running = false;
        Main.kickClient(this);
    }

    /**
     * This method returns the socket
     *
     * @return The socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * This method returns the username
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method returns the current group
     *
     * @return The group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * This method sets the current group
     *
     * @param group
     */
    public void setGroup(Group group) {
        this.group = group;
    }
}
