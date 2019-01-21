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
                if (command.length > 2) {
                    sendPrivateMessage(handler, readLine);
                } else {
                    Main.sendMessage(handler.getSocket(), "ERR Cannot send the message");
                }
                break;
            case "/quit":
                Main.broadcastMessage(null, "BCST " + handler.getUsername() + " left the server");
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
                            leaveGroup(handler);
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
        if (command.length >= 3) {
            if (Main.kickClientFromGroup(handler, command[3])) {
                Main.sendMessage(handler.getSocket(), "BCST Succesfully kicked " + command[3]);
            }
        } else {
            Main.sendMessage(handler.getSocket(), "ERR Please enter a username");
        }
    }

    private static void privateMessageGroup(ClientHandler handler, String[] command) {
        String message = commandToMessage(command);

        if (handler.getGroup() != null) {
            message = "BCST (" + handler.getUsername() + " -> [" + handler.getGroup().getGroupName() + "]): " + message;
            for (int i = 0; i < handler.getGroup().getMembers().size(); i++) {
                Main.sendMessage(handler.getGroup().getMembers().get(i).getSocket(), message);
            }
        } else {
            Main.sendMessage(handler.getSocket(), "ERR You are not in a group!");
        }
    }

    private static void leaveGroup(ClientHandler handler) {
        if (Main.leaveGroup(handler)) {
            Main.sendMessage(handler.getSocket(), "BCST Succesfully left your group");
        } else {
            Main.sendMessage(handler.getSocket(), "ERR You are not in a group!");
        }
    }

    private static void joinGroup(ClientHandler handler, String[] command) {
        if (command.length > 3) {
            if (Main.joinGroup(handler, command[3])) {
                Main.sendMessage(handler.getSocket(), "BCST Succesfully joined [" + command[3] + "]");
            } else {
                Main.sendMessage(handler.getSocket(), "ERR You can't join this group!");
            }
        } else {
            Main.sendMessage(handler.getSocket(), "ERR Please specify a group name!");
        }
    }

    private static void createGroup(ClientHandler handler, String[] command) {
        if (command.length > 3) {
            if (Main.createGroup(handler, command[3])) {
                Main.sendMessage(handler.getSocket(), "BCST Succesfully created [" + command[3] + "]");
            } else {
                Main.sendMessage(handler.getSocket(), "ERR you can't create this group!");
            }
        } else {
            Main.sendMessage(handler.getSocket(), "ERR Please specify a group name!");
        }
    }

    private static void getGroupList(ClientHandler handler) {
        StringBuilder groups;
        groups = new StringBuilder();
        for (int i = 0; i < Main.getGroups().size(); i++) {
            groups.append(" - ").append(Main.getGroups().get(i).getGroupName()).append(Main.BREAKLINE);
        }

        Main.sendMessage(handler.getSocket(), "BCST List of available groups:" + Main.BREAKLINE + groups);
    }

    private static void sendPrivateMessage(ClientHandler handler, String line) {
        String[] splitLine = line.split(" ");
        String receiver = splitLine[2];
        line = line.replaceFirst("BCST /msg " + receiver + " ", "");

        for (int i = 0; i < Main.getClientHandlers().size(); i++) {
            if (Main.getClientHandlers().get(i).getUsername().equalsIgnoreCase(receiver)) {
                if (Main.getClientHandlers().get(i) == handler) {
                    Main.sendMessage(handler.getSocket(), "ERR You cannot send a message to yourself");
                    return;
                }
                String message = "ENCR " + handler.getUsername() + " " + line;
                Main.sendMessage(Main.getClientHandlers().get(i).getSocket(), message);
                return;
            }
        }

        Main.sendMessage(handler.getSocket(), "ERR User not found");
    }

    private static void getOnlineUsers(ClientHandler handler) {
        StringBuilder users;
        users = new StringBuilder();
        for (int i = 0; i < Main.getClientHandlers().size(); i++) {
            users.append(" - ").append(Main.getClientHandlers().get(i).getUsername()).append(Main.BREAKLINE);
        }

        Main.sendMessage(handler.getSocket(), "BCST List of online users:" + Main.BREAKLINE + users);
    }

    private static void getCommandList(ClientHandler handler) {
        Main.sendMessage(handler.getSocket(),
                "BCST Here is a list of all available commands:" + Main.BREAKLINE +
                        "/list : View a list of all online users" + Main.BREAKLINE +
                        "/msg <user> <message> : Send a private message" + Main.BREAKLINE +
                        "/group list : View a list of all groups" + Main.BREAKLINE +
                        "/group create <groupname> : Create a group" + Main.BREAKLINE +
                        "/group join <groupname> : join a group" + Main.BREAKLINE +
                        "/group leave : Leave your group" + Main.BREAKLINE +
                        "/group msg <message> : Send a message to all members of this group" + Main.BREAKLINE +
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
