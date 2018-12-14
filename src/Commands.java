import java.net.Socket;

public class Commands {

    public Commands() {
        //Left blank
    }

    public static String getCommandList() {
        return "BCST Here is a list of all available commands:&/list : View a list of all online users&/msg <user> <message> : Send a private message&&/group list : View a list of all groups&/group create <groupname> : Create a group&/group join <groupname> : join a group&/group leave <groupname> : Leave a group&/group msg <groupname> : Send a message to all members of this group&/group kick <groupname> <user> : Kick a groupmember (owners only)";
    }

    public static void checkCommand(Socket socket, String readLine) {
        String[] command = readLine.split(" ");
        switch (command[1]) {
            case "/help":
                Main.sendMessage(socket, Commands.getCommandList());
                break;
            case "/list":
                //TODO show a list of online users
                break;
            case "/msg":
                //TODO send a private message
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
}
