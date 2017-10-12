package data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Ruoping Lin
 */

/**
 * Manages a post
 * NOTE: A newly created post is marked as UNREAD by the author before he reads it
 */
@SuppressWarnings("serial")
public class Post  implements Serializable{
    private String groupName;       // Group that the post belongs to
    private String groupID;         // Do we actually need this? We'll identify groups by their unique names
    private String subject;         // Post's subject
    private String author;          // Post's author
    private String content;         // Post's content
    private String postedDate;      // Timestamp    
    private String postID;          // Post is identified by ID

    public Post() {

    }
    
    /**
     * Every post is assigned a unique ID
     * @param postID post's ID
     */
    public Post(String postID) {
        this.postID = postID;
    }
    
    /** 
     * Which group that the post belongs to
     * @return group
     */
    public String getGroupName() {
        return this.groupName;
    }
    
    /**
     * Which group that the post belongs to
     * @Usage subcommand of rg, composition of a new post
     * @param groupName 
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /**
     * Which group that the post belongs to
     * @return  group ID
     */
    public String getGroupID() {
        return this.groupID;
    }
    /**
     * Group name must be consistent with group ID
     * @Usage subcommand of rg
     */
    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }
    
    /**
     * Retrieves a post by ID
     * @return 
     */
    public String getPostByID() {
        return this.postID;
    }
    
    /**
     * Assigns an unique ID to a new post
     * @Usage subcommand of rg, composition of a new post
     * @param postID a post is identified by its ID
     */
    public void setPostID(String postID) {
        //@bug: if an existing post has the same ID# as the newly created one,
        // the new post will overwrite old post
        this.postID = postID;
    }
    
    public String getPostSubject() {
        return this.subject;
    }
    
    /**
     * @Usage: subcommand of rg, composition of a new post
     * @param subject 
     */
    public void setPostSubject(String subject) {
        this.subject = subject;
    }
    /**
     * Gets a post's author
     * @return 
     */
    public String getPostAuthor() {
        return this.author;
    }
    
    /**
     * @Usage subcommand of rg, composition of a new post
     * @param author 
     */
    public void setPostAuthor(String author) {
        this.author = author;
    }
    
    /**
     * Gets a post's content
     * @Usage subcommand of rg, displaying
     * @return 
     */
    public String getPostContent() {
        return this.content;
    }
    
    /**
     * Writes content 
     * @Usage subcommand of rg, composition of a new post
     * @param content 
     */
    public void setPostContent(String content) {
        this.content = content;
    }
    
    /**
     * When the post is created
     * @Usage subcommand of rg, displaying
     * @return a string representation of posted date
     */
    public String getPostedDate() {
        return this.postedDate;
    }
    
    /**
     * Takes a Date obj, converts it to String
     * @Usage subcommand of rg, composition a new post
     * @param curDate 
     */
    public void setPostedDate(Date curDate) {
        SimpleDateFormat format = new SimpleDateFormat();
        String DateToString = format.format(curDate);
        this.postedDate = DateToString;
    }
    

}