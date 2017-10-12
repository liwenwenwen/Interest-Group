package data;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Ruoping Lin
 */
@SuppressWarnings("serial")
public class User implements Serializable{
    private String                  id;               // User is identified by ID
    private File                    userDataFile;     // Loads/Saves this User obj
    private ArrayList<String>       subscribedGroups; // Groups that this user has subscribed to
    private ArrayList<String>       readPostIds;      // Posts that the user has read (retrieve by post ID)

    public User() {
        subscribedGroups = new ArrayList<String>();
        readPostIds = new ArrayList<String>();
    }
    public User(String id) {
        this.id = id;
        
        subscribedGroups = new ArrayList<String>();
        readPostIds = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set a post as being read (retrieved by an ID)
     * @param postID post id
     * For rg command
     */
    public void readPost(String postID) {
        readPostIds.add(postID);
    }
    
    /**
     * Gets all posts (retrieved by IDs)
     * @return all posts' IDs
     */
    public ArrayList<String> getReadPosts() {
        return readPostIds;
    }
    
    /**
     * Gets all subscribed groups
     * @Usage sg command, listing all subscribed groups
     * @return 
     */
    public ArrayList<String> getSubscribedGroups() {
        return subscribedGroups;
    }
    
    /**
     * Load a new set of subscribed groups (initially, load to a new User obj)
     * @param subscribedGroups 
     */
    public void setSubscribedGroups(ArrayList<String> subscribedGroups) {
        this.subscribedGroups = subscribedGroups;
    }
    
    /**
     * Subscribe to a group
     * @Usage subcommand of ag: subscribe to a group
     * @param subscribedGroup: add this group to the subscribed group list
     **/
    public void subscribeGroup(String subscribedGroup) {
        this.subscribedGroups.add(subscribedGroup);
        
    }
    
    /**
     * Unsubscribe a group
     * @Usage subcommand of ag, unsubscribe; subcommand of sg, unsubscribe
     * @param group, remove this group from the subscribed group list
     */
    public void unsubscribeGroup(String group) {
        this.subscribedGroups.remove(group);
    }
    
    
    
}
