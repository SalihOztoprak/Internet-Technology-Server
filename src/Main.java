import com.sun.security.ntlm.Client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;


public class Main {
    private final static int PORT = 1337;
    public static final String BREAKLINE = "<br>";
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        //TODO fix that more users can login at the same time
        connectToServer();
    }

    private static void connectToServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();

                String username = null;

                sendMessage(socket, "HELO");
                String message = readMessage(socket, null);
                if (message != null) {
                    username = message.replace("HELO ", "");
                    username = username.replace(" ", "_");
                }

                sendMessage(socket, "+OK " + encodeMessage(message));
                sendMessage(socket, "BCST To view all commands, type /help");

                for (ClientHandler clientHandler1 : clientHandlers) {
                    if (clientHandler1.getUsername().equalsIgnoreCase(username)) {
                        username = username + "#";
                        sendMessage(socket, "BCST Your name has already been taken, so we changed it to " + username);
                        break;
                    }
                }
                Main.broadcastMessage(null, "BCST " + username + " joined the server");

                ClientHandler clientHandler = new ClientHandler(username, socket);
                clientHandlers.add(clientHandler);
                clientHandler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(ClientHandler sender, String message) {
        String msg;
        if (sender != null) {
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
            sendMessage(clientHandler.getSocket(), msg);
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

    public static String readMessage(Socket socket, ClientHandler handler) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readLine = reader.readLine();
            if (readLine != null) {
                if (readLine.startsWith("BCST /")) {
                    Commands.checkCommand(handler, readLine);
                    return null;
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

    public static ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public static ArrayList<Group> getGroups() {
        return groups;
    }
}
