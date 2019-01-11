import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;


public class Main {
    private final static int PORT = 1337;
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        connectToServer();
    }

    private static void connectToServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();

                sendMessage(socket, "HELO");
                String message = readMessage(socket, null);
                sendMessage(socket, "+OK " + encodeMessage(message));
                sendMessage(socket, "BCST To view all commands, type /help");

                String username = null;
                if (message != null) {
                    username = message.replace("HELO ", "");
                }

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
            //TODO fix that the groupname is in front of the name
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
        for (int i = 0; i < clientHandlers.size(); i++) {
            if (clientHandlers.get(i).equals(handler)) {
                clientHandlers.remove(i);
                break;
            }
        }

        try {
            handler.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.interrupt();
    }

    public static boolean createGroup(ClientHandler handler, String groupName) {
        for (Group group1 : groups) {
            if (group1.getGroupName().equalsIgnoreCase(groupName)) {
                return false;
            }
        }

        Group group = new Group(groupName, handler, new ArrayList<>());
        group.getMembers().add(handler);
        handler.setGroup(group);
        groups.add(group);
        return true;
    }

    public static boolean joinGroup(ClientHandler handler, String groupName) {
        if (handler.getGroup() == null) {
            for (Group group : groups) {
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
        if (handler.getGroup() != null) {
            for (Group group : groups) {
                if (group.equals(handler.getGroup())) {
                    group.getMembers().remove(handler);
                    handler.setGroup(null);

                    if (group.getMembers().size() == 0) {
                        groups.remove(group);
                        return true;
                    }

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
        if (handler.getGroup() != null) {
            if (handler.getGroup().getOwner().getUsername().equalsIgnoreCase(handler.getUsername())) {
                for (int i = 0; i < handler.getGroup().getMembers().size(); i++) {
                    if (handler.getGroup().getMembers().get(i).getUsername().equalsIgnoreCase(user)) {
                        handler.getGroup().getMembers().remove(handler);
                        handler.setGroup(null);
                        return true;
                    }
                }
                sendMessage(handler.getSocket(), "ERR This person is not in this group!");
                return false;
            } else {
                sendMessage(handler.getSocket(), "ERR You are the owner of this group!");
                return false;
            }
        } else {
            sendMessage(handler.getSocket(), "ERR You are not in a group!");
            return false;
        }
    }


    public static ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public static ArrayList<Group> getGroups() {
        return groups;
    }
}
