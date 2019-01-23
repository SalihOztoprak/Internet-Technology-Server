import java.util.ArrayList;

/**
 * This class is used to define a group object
 */
public class Group {
    private String groupName;
    private ClientHandler owner;
    private ArrayList<ClientHandler> members;

    /**
     * This is the default constructor for this class
     *
     * @param groupName The name of the group
     * @param owner     The owner of the group
     * @param members   A list with members of this group
     */
    public Group(String groupName, ClientHandler owner, ArrayList<ClientHandler> members) {
        this.groupName = groupName;
        this.owner = owner;
        this.members = members;
    }

    /**
     * This method returns the groupname
     *
     * @return The groupname
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * This method returns the owner
     *
     * @return The owner
     */
    public ClientHandler getOwner() {
        return owner;
    }

    /**
     * This method returns a list with members
     *
     * @return The list with members
     */
    public ArrayList<ClientHandler> getMembers() {
        return members;
    }

    /**
     * This method sets a new owner
     *
     * @param owner The new owner
     */
    public void setOwner(ClientHandler owner) {
        this.owner = owner;
    }
}
