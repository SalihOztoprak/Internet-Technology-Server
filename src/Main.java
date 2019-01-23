import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

/**
 * This class is used for starting the server
 */
public class Main {
    private final static int PORT = 1337;
    public static final String BREAKLINE = "<br>";
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        connectToServer();
    }

    /**
     * This method let's users connect to the server
     */
    private static void connectToServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler("", socket);
                clientHandlers.add(clientHandler);
                clientHandler.start();
            }

        } catch (IOException e) {
            System.out.println("A user tried to log in but something went wrong with his connection");
        }
    }

    /**
     * This method will broadcast a message to every client that is online
     *
     * @param sender  The sender of the message
     * @param message The message itself
     */
    public static void broadcastMessage(ClientHandler sender, String message) {
        String msg;
        if (sender != null && message.startsWith("BCST")) {
            msg = message.replace("BCST ", "");
            msg = sender.getUsername() + ": " + msg;
            if (sender.getGroup() != null) {
                msg = "[" + sender.getGroup().getGroupName() + "] " + msg;
            }
            msg = "BCST " + msg;
        } else {
            msg = message;
        }

        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.equals(sender)) {
                sendMessage(clientHandler.getSocket(), msg);
            }
        }
    }

    /**
     * This method will send a message to the given sockey
     *
     * @param socket  The socket you want to send the message to
     * @param message The message you want to send
     */
    public static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            writer.println(message);
            writer.flush();
        } catch (Exception e) {
            System.out.println("Can't send message since client has disconnected");
        }
    }

    /**
     * This method will read the message sent by the clients
     *
     * @param socket  The socket from the sender
     * @param handler The clienthandler that send the message
     * @return The message when it has to be send to other clients, otherwise it is null
     */
    public static String readMessage(Socket socket, ClientHandler handler) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readLine = reader.readLine();
            if (readLine != null) {
                if (readLine.startsWith("BCST /")) {
                    Commands.checkCommand(handler, readLine);
                    return null;
                } else if (readLine.startsWith("KEYS") || readLine.startsWith("ENCR") || readLine.startsWith("FILE")) {
                    String[] splitLine = readLine.split(" ");
                    for (ClientHandler clientHandler : clientHandlers) {
                        if (clientHandler.getUsername().equalsIgnoreCase(splitLine[1])) {
                            String msg;
                            if (splitLine.length > 3) {
                                msg = splitLine[0] + " " + handler.getUsername() + " " + splitLine[2] + " " + splitLine[3];
                            } else {
                                msg = splitLine[0] + " " + handler.getUsername() + " " + splitLine[2];
                            }
                            sendMessage(clientHandler.getSocket(), msg);
                            return null;
                        }
                    }
                    sendMessage(handler.getSocket(), "ERR User not found");
                }
            }
            return readLine;
        } catch (IOException e) {
            System.out.println("Can't read message since client has disconnected");
        }
        return null;
    }

    /**
     * This method encodes the string with a Base64 encoder
     *
     * @param message The string you want to encode
     * @return The Base64 encoded string
     */
    public static String encodeMessage(String message) {
        try {
            return Base64.getEncoder().encodeToString(MD5.getMd5(message));
        } catch (Exception e) {
            System.out.println("Can't encode message since client has disconnected");
        }
        return null;
    }

    /**
     * This method kicks the selected client
     *
     * @param handler The handler you want to kick
     */
    public static void kickClient(ClientHandler handler) {
        //Find the client you want to kick from the server
        for (int i = 0; i < clientHandlers.size(); i++) {
            if (clientHandlers.get(i).equals(handler)) {
                clientHandlers.remove(i);
                Main.broadcastMessage(null, "BCST " + handler.getUsername() + " left the server");
                break;
            }
        }

        //Try to close the socket of the client
        try {
            handler.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.interrupt();
    }

    /**
     * This method will create a group and add it to the list of groups
     *
     * @param handler   The handler that created the group
     * @param groupName The name of the group
     * @return True if the group created successfully
     */
    public static boolean createGroup(ClientHandler handler, String groupName) {
        //Check if you are in a group
        if (handler.getGroup() != null) {
            return false;
        }

        //Check if the name of your group already exists
        for (Group group1 : groups) {
            if (group1.getGroupName().equalsIgnoreCase(groupName)) {
                return false;
            }
        }

        //Create the group and join it
        Group group = new Group(groupName, handler, new ArrayList<>());
        group.getMembers().add(handler);
        handler.setGroup(group);
        groups.add(group);
        return true;
    }

    /**
     * This method lets you join the given group
     *
     * @param handler   The handler that wants to join the group
     * @param groupName The name of the group you want to join
     * @return True if the group joined successfully
     */
    public static boolean joinGroup(ClientHandler handler, String groupName) {
        //Check if you aren't in a group already
        if (handler.getGroup() == null) {
            for (Group group : groups) {
                //Find the group and join
                if (group.getGroupName().equalsIgnoreCase(groupName)) {
                    group.getMembers().add(handler);
                    handler.setGroup(group);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method lets you leave it's current group
     *
     * @param handler The handler that wants to leave his group
     * @return True if the group left successfully
     */
    public static boolean leaveGroup(ClientHandler handler) {
        //Check if client is in a group
        if (handler.getGroup() != null) {
            for (Group group : groups) {
                //Find the group of the user
                if (group.equals(handler.getGroup())) {
                    group.getMembers().remove(handler);
                    handler.setGroup(null);

                    //If you are the last person in the group, delete it
                    if (group.getMembers().size() == 0) {
                        groups.remove(group);
                        return true;
                    }

                    //If you are the owner, make someone else the owner
                    if (group.getOwner().equals(handler)) {
                        group.setOwner(group.getMembers().get(0));
                    }

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method kicks a client from the group
     *
     * @param handler The user that wants to kick a client (has to be the owner)
     * @param user    The user that has to be kicked
     * @return True if the kicking was successful
     */
    public static boolean kickClientFromGroup(ClientHandler handler, String user) {
        //Check if you are in a group
        if (handler.getGroup() == null) {
            sendMessage(handler.getSocket(), "ERR You are not in a group!");
            return false;
        }

        //Check if you are the owner of the group
        if (handler.getGroup().getOwner() != handler) {
            sendMessage(handler.getSocket(), "ERR You are not the owner of this group!");
            return false;
        }

        //Check if you don't want to kick yourself
        if (handler.getUsername().equalsIgnoreCase(user)) {
            sendMessage(handler.getSocket(), "ERR You cannot kick yourself!");
            return false;
        }

        //Try to find the person you want to kick and kick him
        for (int i = 0; i < handler.getGroup().getMembers().size(); i++) {
            ClientHandler currentHandler = handler.getGroup().getMembers().get(i);
            if (currentHandler.getUsername().equalsIgnoreCase(user)) {
                handler.getGroup().getMembers().remove(currentHandler);
                currentHandler.setGroup(null);
                Main.sendMessage(currentHandler.getSocket(), "BCST You have been kicked from [" + handler.getGroup().getGroupName() + "]");
                return true;
            }
        }

        //If you couldn't find the person throw error
        sendMessage(handler.getSocket(), "ERR This person is not in this group!");
        return false;
    }

    /**
     * This method returns the arraylist of clienthandlers
     *
     * @return The arraylist with clienthandlers
     */
    public static ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    /**
     * This method returns the arraylist of groups
     *
     * @return The arraylist with groups
     */
    public static ArrayList<Group> getGroups() {
        return groups;
    }
}
