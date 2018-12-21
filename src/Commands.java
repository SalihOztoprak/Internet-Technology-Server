public class Commands {

    public Commands() {
        //Left blank
    }

    public static void checkCommand(ClientHandler handler, String readLine) {
        String[] command = readLine.split(" ");
        switch (command[1]) {
            case "/help":
                getCommandList(handler);
                break;
            case "/list":
                getOnlineUsers(handler);
                break;
            case "/msg":
                sendPrivateMessage(handler, command);
                break;
            case "/group":
                if (command.length > 2) {
                    switch (command[2]) {
                        case "list":
                            getGroupList(handler);
                            break;
                        case "create":
                            createGroup(handler, command);
                            break;
                        case "join":
                            joinGroup(handler, command);
                            break;
                        case "leave":
                            leaveGroup(handler, command);
                            break;
                        case "msg":
                            privateMessageGroup(handler, command);
                            break;
                        case "kick":
                            kickInGroup(handler, command);
                            break;
                        default:
                            Main.sendMessage(handler.getSocket(), "ERR That is not a valid group command, check /help");
                            break;
                    }
                } else {
                    Main.sendMessage(handler.getSocket(), "ERR That is not a valid group command, check /help");
                }
                break;
            default:
                Main.sendMessage(handler.getSocket(), "ERR That is not a valid command, check /help");
                break;
        }
    }

    private static void kickInGroup(ClientHandler handler, String[] command) {
        if (Main.kickClientFromGroup(handler, command[3])) {
            Main.sendMessage(handler.getSocket(), "BCST Succesfully kicked " + command[3]);
        }
    }

    private static void privateMessageGroup(ClientHandler handler, String[] command) {
        String message = commandToMessage(command);

        if (handler.getGroup() != null) {
            for (int i = 0; i < handler.getGroup().getMembers().size(); i++) {
                if (!handler.getGroup().getMembers().get(i).getUsername().equalsIgnoreCase(handler.getUsername())) {
                    Main.sendMessage(handler.getSocket(), "BCST (" + handler.getUsername() + " -> [" + handler.getGroup().getGroupName() + "]) : " + message);
                }
            }
        } else {
            Main.sendMessage(handler.getSocket(), "ERR You are not in this group!");
        }
    }

    private static void leaveGroup(ClientHandler handler, String[] command) {
        if (Main.leaveGroup(handler)) {
            Main.sendMessage(handler.getSocket(), "BCST Succesfully left your group");
        } else {
            Main.sendMessage(handler.getSocket(), "ERR You are not in a group!");
        }
    }

    private static void joinGroup(ClientHandler handler, String[] command) {
        if (Main.joinGroup(handler, command[3])) {
            Main.sendMessage(handler.getSocket(), "BCST Succesfully joined [" + command[3] + "]");
        } else {
            Main.sendMessage(handler.getSocket(), "ERR You can't join this group!");
        }
    }

    private static void createGroup(ClientHandler handler, String[] command) {
        if (Main.createGroup(handler, command[3])) {
            Main.sendMessage(handler.getSocket(), "BCST Succesfully created [" + command[3] + "]");
        } else {
            Main.sendMessage(handler.getSocket(), "ERR This group already exists!");
        }
    }

    private static void getGroupList(ClientHandler handler) {
        StringBuilder groups;
        groups = new StringBuilder();
        for (int i = 0; i < Main.getGroups().size(); i++) {
            groups.append(" - ").append(Main.getGroups().get(i).getGroupName()).append("&");
        }

        Main.sendMessage(handler.getSocket(), "BCST List of groups:&" + groups);
    }

    private static void sendPrivateMessage(ClientHandler handler, String[] command) {
        String message = commandToMessage(command);

        for (int i = 0; i < Main.getClientHandlers().size(); i++) {
            if (Main.getClientHandlers().get(i).getUsername().equalsIgnoreCase(command[2])) {
                Main.sendMessage(Main.getClientHandlers().get(i).getSocket(), "BCST (" + handler.getUsername() + " -> " + command[2] + "): " + message);
                return;
            }
        }

        Main.sendMessage(handler.getSocket(), "ERR User not found");
    }

    private static void getOnlineUsers(ClientHandler handler) {
        StringBuilder users;
        users = new StringBuilder();
        for (int i = 0; i < Main.getClientHandlers().size(); i++) {
            users.append(" - ").append(Main.getClientHandlers().get(i).getUsername()).append("&");
        }

        Main.sendMessage(handler.getSocket(), "BCST List of online users:&" + users);
    }

    private static void getCommandList(ClientHandler handler) {
        Main.sendMessage(handler.getSocket(),
                "BCST Here is a list of all available commands:&" +
                        "/list : View a list of all online users&" +
                        "/msg <user> <message> : Send a private message&&" +
                        "/group list : View a list of all groups&" +
                        "/group create <groupname> : Create a group&" +
                        "/group join <groupname> : join a group&" +
                        "/group leave : Leave your group&" +
                        "/group msg <message> : Send a message to all members of this group&" +
                        "/group kick <groupname> <user> : Kick a groupmember (owners only)");
    }

    private static String commandToMessage(String[] command) {
        StringBuilder message;
        message = new StringBuilder();
        for (int i = 3; i < command.length; i++) {
            message.append(command[i]);
            if (i != command.length - 1) {
                message.append(" ");
            }
        }

        return message.toString();
    }
}
