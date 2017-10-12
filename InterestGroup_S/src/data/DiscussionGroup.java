package data;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
// server side

/**
 *
 * @author Ruoping Lin
 */

/**
 * Manages a discussion group
 * 
 * A list of discussion group is stored in DiscussionGroupList.json 
 * and group names are in Constants class
 */
@SuppressWarnings("serial")
public class DiscussionGroup implements Serializable{

        private String          groupName;      // Every group is identified by its unique group name
	private String          groupID;        // Do we actually need this?

        private ArrayList<Post> posts;          // Posts under this group
        private DataManager     dataManager;
        
        
	public DiscussionGroup() throws IOException, ParseException {
            
            groupName = null;              
            dataManager = new DataManager();
	}
        
        public DiscussionGroup(String groupName) throws IOException, ParseException {
            
            this.groupName = groupName;
            dataManager = new DataManager();
        }

        /**
         * Uses group name to identify each group
         * @return 
         */
        public String getGroupName() {
            return this.groupName;
        }
        
        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }
        
        /**
         * We use groupName to distinguish groups
         * @return 
         */
        public String getGroupID() {
            return this.groupID;
        }
        public void setGroupiD(String groupID) {
            this.groupID = groupID;
        }
        
        /**
         * Gets all posts that belong to this group
         * @param groupName a specified group
         * @return a list of posts
         */
        public ArrayList<Post> getPosts() throws IOException, ParseException {
            posts = new ArrayList<>();
            
            // Loads all posts from data base (group name must be known)
            if (this.groupName != null)
                posts = dataManager.loadAllPosts(this.groupName);
            else 
                System.out.println("Group nama is empty");
            return posts;
        }
        
        /**
         * Makes a new post to a specific group
         * For subcommand rgp (post to group) of rg (read group)
         * @param post a new post
         */
        public void setNewPostToGroup(Post post) {
            this.posts.add(post);
        }
        
        
}