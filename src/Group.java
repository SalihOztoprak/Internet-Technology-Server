import java.util.ArrayList;

public class Group {
    private String groupName;
    private ClientHandler owner;
    private ArrayList<ClientHandler> members;

    public Group(String groupName, ClientHandler owner, ArrayList<ClientHandler> members) {
        this.groupName = groupName;
        this.owner = owner;
        this.members = members;
    }

    public String getGroupName() {
        return groupName;
    }

    public ClientHandler getOwner() {
        return owner;
    }

    public ArrayList<ClientHandler> getMembers() {
        return members;
    }
}
