import java.net.Socket;

public class Commands {

    public Commands() {
        //Left blank
    }

    public static void checkCommand(ClientHandler handler, String readLine) {
        String[] command = readLine.split(" ");
        switch (command[1]) {
            case "/help":
                Main.sendMessage(handler.getSocket(), getCommandList());
                break;
            case "/list":
                Main.sendMessage(handler.getSocket(), getOnlineUsers());
                break;
            case "/msg":
                sendPrivateMessage(handler, command);
                break;
            case "/group":
                switch (command[2]) {
                    case "list":
                        //TODO show a list of available groups
                        break;
                    case "create":
                        //TODO create a group
                        break;
                    case "join":
                        //TODO join a group
                        break;
                    case "leave":
                        //TODO leave a group
                        break;
                    case "msg":
                        //TODO message a group
                        break;
                    case "kick":
                        //TODO kick someone from the group
                        break;
                    default:
                        //TODO create an error message
                        break;
                }
                break;
            default:
                //TODO create an error message
                break;
        }
    }

    private static String getCommandList() {
        return "BCST Here is a list of all available commands:&/list : View a list of all online users&/msg <user> <message> : Send a private message&&/group list : View a list of all groups&/group create <groupname> : Create a group&/group join <groupname> : join a group&/group leave <groupname> : Leave a group&/group msg <groupname> : Send a message to all members of this group&/group kick <groupname> <user> : Kick a groupmember (owners only)";
    }

    private static String getOnlineUsers() {
        StringBuilder users;
        users = new StringBuilder();
        for (int i = 0; i < Main.getClientHandlers().size(); i++) {
            users.append(" - ").append(Main.getClientHandlers().get(i).getUsername()).append("&");
        }

        return "BCST List of online users:&" + users;
    }

    private static String sendPrivateMessage(ClientHandler handler, String[] command) {
        StringBuilder message;
        message = new StringBuilder();
        for (int i = 0; i < command.length - 3; i++) {
            message.append(command[i + 3]);
        }

        //TODO fix the array out of bound exception
        //TODO fix that you can send messages to eachother
        for (int i = 0; i < Main.getClientHandlers().size(); i++) {
            if (Main.getClientHandlers().get(i).equals(command[2])) {
                Main.sendMessage(Main.getClientHandlers().get(i).getSocket(), handler.getUsername() + ": " + message.toString());
                return null;
            }
        }

        Main.sendMessage(handler.getSocket(),"BCST Error: User not found");
        return null;
    }
}
