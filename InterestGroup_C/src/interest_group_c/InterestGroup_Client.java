 package interest_group_c;

/**
 *
 * @author Jia Sheng Ma
 */
//import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import interest_group_c.State;
import data.Constants;
import data.DataManager;
import data.DiscussionGroup;
import data.User;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.ParseException;

/**
 * client side 
 */
@SuppressWarnings("unchecked")
public class InterestGroup_Client {
    private static DataManager dataManager;
    private static String state;            // user state in the session
    private static String prompt;
    
    private static String command;          // user command
    private static ArrayList<String> cmdTokens;
    private static Object response;         // server response
    
    private static Socket socket;
    private static ObjectOutputStream output_to_server;
    private static ObjectInputStream input_from_server;
    
    private static String hostmachine;
    private static int portnumber;
    private static User user;
    private static Scanner user_input_scn;
    
    private static boolean isLastGroup;            // Subcommand of ag. Determines if all groups have been displayed. If so, exit from ag mode
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // System.out.println("args[0]: " + args[0] + "   args[1]" + args[1]);
        // get hostmachine and port number 
        hostmachine = args[0];
        // hostmachine = "localhost";
        
		try {
        	portnumber = Integer.parseInt(args[1]);
		} catch(NumberFormatException nfe) {
			System.out.println("Usage: java CLASS HOSTMACHINE PORTNUMBER");
			System.exit(1);
		}
        // portnumber = 6666;
        
        // user is not logged in yet 
        state = State.NOT_LOGGED_IN;
        user = null;
        
            try{
                // user input scanner
                user_input_scn = new Scanner(System.in);
                // create socket
                socket = new Socket(hostmachine, portnumber);
                System.out.println(">> Connected to " + hostmachine + " at " + portnumber);
				
				dataManager = new DataManager();
                
                // create socket with hostmachine and portnumber 
                // Socket socket = new Socket(hostmachine, portnumber);
                // feedback: connected to hostmachine 
                // System.out.println(">> Connected to " + hostmachine + "\n");

                // output buffer (to server)
                output_to_server = new ObjectOutputStream(socket.getOutputStream());
                // input buffer (from server)
                input_from_server = new ObjectInputStream(socket.getInputStream());
                
                /*****************************
                    listen for user command 
                *****************************/
                do {
                    /*****************************
                        get user command
                    *****************************/
                    printPrompt();
                    command = user_input_scn.nextLine();
                    // tokenize command
                    cmdTokens = tokenizeCMD(command);
                    
                    // prints help menu
                    if(command.equals(Constants.HELP)) {
                        printHelpMenu();
                        continue;
                    }
                    if(command.equals(Constants.LOGOUT)) {
                        //TODO: notify server so that server can close the socket 
                        System.out.println("Logging out ...");
                        socket.close();
                        System.exit(0);
                    }
                    /*****************************
                        send server command 
                            package user command before sending it to server
                            but if it is not logged in, just send the command
                    *****************************/                        
                    if(state.equals(State.NOT_LOGGED_IN)) {
                        
                        if(!cmdTokens.get(0).equals(Constants.LOGIN)){
                            // feedback: not logged in
                            System.out.println(">> Please log in first\n");
                            continue;
                        } else {
                            // send login command to server
                            if(validateCMD()) {
                                output_to_server.writeObject((Object)(formatCMD(command)));
                                output_to_server.flush();
                            } else {
                                continue;
                            }
                        }
                    } else {
                        if(isCMDValid(command)) {
                            // further validates command (check illegal arguments)
                            if(validateCMD()) {
                                // System.out.println("entere veaild csm");
                                // System.out.println("commmmmabd"+command);
                                output_to_server.writeObject((Object)(formatCMD(command)));
                                output_to_server.flush();
                            } else {
                                continue;
                            }
                        } else {
                            System.out.println(">> No such command. Type \"help\" to see available commands");
                            continue;
                        }
                    }
                    
                    /*****************************
                        get response from server
                     *****************************/
                    // System.out.println(">> Contacting server...");
                    System.out.println();

                    response = input_from_server.readObject();

                    // System.out.println("200 OK "+ response);
                    handleServerResponse(response);
                    
                } while (true);

            } catch(IOException ioe) {
                System.out.println("<< FAILED TO CONNECT TO HOST");
            } catch(ClassNotFoundException cnf) {
                cnf.printStackTrace();
            } catch(ParseException pe) {
            	System.out.println("<< error in parsing");
			}
    }
    
    /**
     * Handles server's response
     * @param response
     * @param input_from_server
     * @param output_to_server 
     */
    public static void handleServerResponse(Object response) throws IOException, ClassNotFoundException  {
        
        /* server response is just an object depending one what 
           request the client sent to the server 
           1. if login cmd is sent: response = user : User
           2. if ag    cmd is sent: response = allGroups : ArrayList<String>
           3. //TODO: 
        */
        
        String cmd = cmdTokens.get(0);
        
        if(cmd.equals(Constants.AG)) {
            if (cmdTokens.size() == 1)          // If N is not given, use default N
                ag_mode((ArrayList)response);
            else
                ag_mode((ArrayList)response, Integer.parseInt(cmdTokens.get(1)));
            
        } else if(cmd.equals(Constants.SG)) {
            int subCmd=0;
            int count=0;
            int minal = 0;
            if(cmdTokens.size()>1){
                subCmd = Integer.parseInt(cmdTokens.get(1));
                count =0;
                int numberOfPostsInList = ((ArrayList)response).size();
                minal = Math.min(subCmd,numberOfPostsInList);
                for(int i=0;i<minal;i++){
                    System.out.println((i+1) +".   "+((ArrayList)((ArrayList)response).get(i)).get(1)
					+ "  " +((ArrayList)((ArrayList)response).get(i)).get(0));
                    count++;
                }
            }else{
                for(int i=0;i<((ArrayList)response).size();i++){
                    System.out.println((i+1) +".   "+((ArrayList)((ArrayList)response).get(i)).get(1)
					+ "  " +((ArrayList)((ArrayList)response).get(i)).get(0));
                }
            }
            System.out.println();
            sg_mode((ArrayList)response,count,minal);
        } else if(cmd.equals(Constants.RG)) {

            int userInputSize = 5;
            int count =0;
            if(cmdTokens.size()==3) {
                userInputSize = Integer.parseInt(cmdTokens.get(2));
            }
            int minal = Math.min(userInputSize,((ArrayList)response).size());
            for(int i=0;i<minal;i++){
                if(((ArrayList) ((ArrayList) response).get(i)).size()==5){
                    System.out.println((i + 1) + "." + ((ArrayList) ((ArrayList) response).get(i)).get(4)
					+"   "+((ArrayList) ((ArrayList) response).get(i)).get(0) + "  " 
					+ ((ArrayList) ((ArrayList) response).get(i)).get(1));
                }else {
                    System.out.println((i + 1) + ".      " + ((ArrayList) ((ArrayList) response).get(i)).get(0) + "  " + ((ArrayList) ((ArrayList) response).get(i)).get(1));
                }
                count++;
            }

            rg_mode((ArrayList)response,count,minal);
        } else if(cmd.equals(Constants.LOGIN)) {            
            handleLoginResponse();
        } 
        // else if(cmd.equals(Constants.LOGOUT)) {
        //    try {
                // close sockets and exit
        //        socket.close();
        //    } catch (IOException ex) {
        //        Logger.getLogger(InterestGroup_Client.class.getName()).log(Level.SEVERE, null, ex);
        //    }
        //    System.exit(0);
        // }
    }
    
    /**
     * Helper method: in case N is not given by the user
     * @param response 
     */
    private static void ag_mode(ArrayList response) throws IOException, ClassNotFoundException {
        ag_mode(response, Constants.N);
    }
    
    /**
     * Enables sub commands available under "ag" mode
     * 
     * Subcommands:
     * s: subscribe to group(s)
     * u: unsubscribe to group(s)
     * n: display N groups at a time. if N is not specified, use a default number
     */
    private static void ag_mode(ArrayList response, int n) throws IOException, ClassNotFoundException {
        System.out.println("################################");
        System.out.println("#          all groups          #");
        System.out.println("################################");
        
        ArrayList<String> allGroups = (ArrayList<String>)response;
        
        // Gets groups that the user has subscribed 
        ArrayList<String> subscribedGroups = new ArrayList<>();
        subscribedGroups = user.getSubscribedGroups(); 
        
        // update current state as "ag"
        state = State.IN_AG;
        
        // Determines if last group name has been displayed. If true, break the do while loop, and exit from ag mode
        isLastGroup = false;
        
        // Displays N groups at a time
        int startPrintN = 0;    // will be set to endPrintN
        int endPrintN   = n;    // will be incremented by n
        
        // Prints the first n group names as soon as user enters ag mode
        while (startPrintN < endPrintN) {
            // If subscribed group list contains current group, add (s) in front, else leave it empty
            if (subscribedGroups.contains(allGroups.get(startPrintN)))
                System.out.println("(" + (startPrintN + 1) + ") (s) " + allGroups.get(startPrintN));
            else
                System.out.println("(" + (startPrintN + 1) + ") ( ) " + allGroups.get(startPrintN));
            startPrintN++;
            
            // in case n > number of groups
            if(startPrintN == allGroups.size()) {
                isLastGroup = true;
                break;
            }
        }
       
        do { // listen for user commands 
            printPrompt();
            command = user_input_scn.nextLine();
            cmdTokens = tokenizeCMD(command);
            String cmd = cmdTokens.get(0);
            
            if(cmd.equals("q")) {
                state = State.LOGGED_IN;
                printMainMenuHeader();
                break;
            } else if(cmd.equals("s")) {
                // size > 1 means user has entered at least 1 group that he wants to subscribe to
                if(cmdTokens.size() > 1) {
                    
                    boolean isValidRange = true;    // Determines if group index is valid
                    // Checks if group index is within range 1 - 15. Ignore index = 0 because it is "s"
                    for (int cmdIndex = 1; cmdIndex < cmdTokens.size(); cmdIndex++) {
                        
                        // Catches NumberFormatException when index entered is not a number string
                        try {
                            int groupIndex = Integer.parseInt(cmdTokens.get(cmdIndex));
                            
                            if ( groupIndex < 1 || groupIndex > 15 ) {
                                isValidRange = false;   // false if not within 1 - 15
                                System.out.println("Group index out of range");
                                break;
                            } 
                        } catch (NumberFormatException e) {
                            isValidRange = false;
                            System.out.println("Please enter a valid group index");
                            break;
                        }
                    }
                    
                    if (isValidRange == true) {
                        int subsIndex = 1;
                        // subscribe to group given by its index (shown in the front)
                        while (subsIndex < cmdTokens.size()) {
                            s_handler_ag(Integer.parseInt(cmdTokens.get(subsIndex)), allGroups);
                            subsIndex++;
                        }
                        output_to_server.writeObject((Object)(formatCMD("ag s")));
                                output_to_server.flush();
                    }
                    
                } else {
                    System.out.println("Please enter at least 1 group number.\r\nCommand format example: s 1 2 3");
                }
                
            } else if (cmd.equals("u")){
                // size > 1 means user has entered at least 1 group that he wants to unsubscribe
                if(cmdTokens.size() > 1) {
                    
                    boolean isValidRange = true;    // Determines if group index is valid
                    // Checks if group index is within range 1 - 15. Ignore index = 0 because it is "s"
                    for (int cmdIndex = 1; cmdIndex < cmdTokens.size(); cmdIndex++) {
                        // Throws NumberFormatException when index entered is not a number string
                        try {
                            int groupIndex = Integer.parseInt(cmdTokens.get(cmdIndex));
                            
                            if ( groupIndex < 1 || groupIndex > 15 ) {
                                isValidRange = false;   // false if not within 1 - 15
                                System.out.println("Group index out of range");
                                break;
                            } 
                        } catch (NumberFormatException e) {
                            isValidRange = false;
                            System.out.println("Please enter a valid group index");
                            break;
                        }
                    }
                    
                    if (isValidRange == true) {
                        int subsIndex = 1;
                        // unsubscribe to group given by its index in the front
                        while (subsIndex < cmdTokens.size()) {
                            u_handler_ag(Integer.parseInt(cmdTokens.get(subsIndex)), allGroups);
                            subsIndex++;
                        }
                        output_to_server.writeObject((Object)(formatCMD("ag u" + user)));
                        output_to_server.flush();
						input_from_server.readObject();
                    }
                    
                } else {
                    System.out.println("Please enter at least 1 group number.\r\nCommand format example: s 1 2 3");
                }
                
            } else if (cmd.equals("n")) {
                
                // Edge case: if N is 15 (has displayed all group names by command "ag 15"), then "n" will be used to exit ag mode
                if (n == 15)
                    isLastGroup = true;
                
                // Determines if all groups have been displayed. isLastGroup is set to false 
                // in the handler method while "start index" reaches the size of all group names
                if (isLastGroup == true) {
                    state = State.LOGGED_IN;
                    printMainMenuHeader();
                    break;
                }
//                if(validateSubCMD()) {
                    startPrintN = endPrintN;
                    endPrintN += n;
                    
                    n_handler_ag(allGroups, subscribedGroups, startPrintN, endPrintN);
//                    
//                } else {
//                    System.out.println("Invalid sub cmd");
//                }
            } else {
                System.out.println("ERROR: NO SUCH COMMAND");
                printSubCMDMenu_AG();
            }
        } while(true);
    } /* end of ag_mode*/
    
    
    /**
     * Enables sub commands available under "sg"
     */
    private static void sg_mode(ArrayList response, int count,int subCmd) throws IOException {
        System.out.println("################################");
        System.out.println("#       subscribed groups      #");
        System.out.println("################################");
        // update current state as "sg"
        state = State.IN_SG;
        
        do { // listen for user commands 
            printPrompt();
            command = user_input_scn.nextLine();
            cmdTokens = tokenizeCMD(command);
            String cmd = cmdTokens.get(0);
            
            if(cmd.equals("q")) {
                printMainMenuHeader();
                state = State.LOGGED_IN;
                break;
            } else if(cmd.equals("n")){
                    //sg_handler(cmd);
                System.out.println("response Size"+response.size());
                System.out.println("count "+ count);
                if(response.size()!=(count)) {
                    subCmd = Math.min(response.size()-count,subCmd);
                    for (int i = 0; i < subCmd; i++) {
                        System.out.println((count + 1) + ".   " + ((ArrayList) ((ArrayList) response).get(count)).get(1) + "  " + ((ArrayList) ((ArrayList) response).get(count)).get(0));
                        count++;
                    }
                }else{
                    System.out.println("reach end of the list");
                }
            }else if(cmd.equals("u")){
                String subbCmd = cmdTokens.get(1);
                System.out.println("You've successfully unsubscribed " + subbCmd);
                output_to_server.writeObject((Object)(formatCMD("sg u "+subbCmd)));
                output_to_server.flush();


            }else {
                System.out.println("ERROR: NO SUCH COMMAND");
                printSubCMDMenu_SG();
            }

        } while(true);
    } /* end of sg_mode */
    
    /**
     * Enables sub commands available under "rg"
     */
    private static void rg_mode(ArrayList response, int count , int subCmd) throws IOException, ClassNotFoundException {
        
        /* if server returns null, i.e. no such group exist, 
           then give feedback to user
        */ 
        if(response == null) {
            System.out.println("<< ERROR: Group: \"" + cmdTokens.get(1) 
                             + "\" NOT FOUND IN RECORD");
            return;
        }
        // now we have a group, we can run queries on it 
       // DiscussionGroup group = (DiscussionGroup)response;
        
        // max number of post to be displayed at a time
        int N;
        if(cmdTokens.size() > 2) {
            N = Integer.parseInt(cmdTokens.get(2));
        } else {
            N = Constants.N;
        }
        
        System.out.println("################################");
        System.out.println("#          read groups         #");
        System.out.println("################################");
        // update current state as "rg"
        state = State.IN_RG;
        
        updatePrompt();
        do { // listen for user commands 
            printPrompt();
            command = user_input_scn.nextLine();
            cmdTokens = tokenizeCMD(command);
            String cmd = cmdTokens.get(0);

            if(cmd.equals("q")) {
                printMainMenuHeader();
                state = State.LOGGED_IN;
                break;
            }

            //TODO: the subcommand can be a number as an id, if it is a number, 
            // a sub sub command interface should be displayed
            else if(cmd.equals("r") || cmd.equals("n") || cmd.equals("p")) {
               if(validateSubCMD()) {
                   if(cmd.equals("r")) {
                       //rrrrrrrrr
                       String subbCmd = cmdTokens.get(1);
                       ArrayList<String> range = new ArrayList<String>();
                       String commd = "rg " + cmd;
                       if (subbCmd.length() > 1) {
                           //a range
                           int min = Integer.parseInt(String.valueOf(subbCmd.charAt(0)));
                           int max = Integer.parseInt(String.valueOf(subbCmd.charAt(2)));
                           for (int i = min; i <= max; i++) {
                               range.add((String) ((ArrayList) ((ArrayList) response).get(i - 1)).get(2));
                               commd = commd + " " + range.get(i - 1);
                           }
                       } else {
                           range.add((String) ((ArrayList) ((ArrayList) response).get(Integer.parseInt(subbCmd))).get(2));
                           commd = commd + " " + range.get(0);
                       }
                       output_to_server.writeObject((Object) (formatCMD(commd)));
                       output_to_server.flush();
                   }
                    rg_handler(cmd, N);
               // } else {
                    //TODO:
               }else if(cmd.equals("p")){
                   System.out.println(">>Subject: ");
                   command = user_input_scn.nextLine();
                   String subject = command+" $";
                   System.out.println(">>Content(end with space$): ");
                   command = user_input_scn.nextLine();
                   String content = command;
                   System.out.println(subject+content);
                   output_to_server.writeObject((Object)(formatCMD("rg p "+subject+" "+content+" "+((ArrayList)(response.get(0))).get(3))));
                   output_to_server.flush();
                   Object input = input_from_server.readObject();


               }else if(cmd.equals("n")){
                   //sg_handler(cmd);
                   System.out.println("response Size"+response.size());
                   System.out.println("count "+ count);
                   if(response.size()!=(count)) {
                       subCmd = Math.min(response.size()-count,subCmd);
                       for (int i = 0; i < subCmd; i++) {
                           if(((ArrayList) ((ArrayList) response).get(i)).size()==5){
                               System.out.println((i + 1) + "." + ((ArrayList) ((ArrayList) response).get(i)).get(4)+"   "+((ArrayList) ((ArrayList) response).get(i)).get(0) + "  " + ((ArrayList) ((ArrayList) response).get(i)).get(1));
                           }else {
                               System.out.println((i + 1) + ".      " + ((ArrayList) ((ArrayList) response).get(i)).get(0) + "  " + ((ArrayList) ((ArrayList) response).get(i)).get(1));
                           }
                           count++;
                       }
                   }else{
                       System.out.println("reach end of the list");
                   }

               }
            } else {
                try {
                    int postid = Integer.parseInt(cmd);
                    //TODO: print id sub sub command header


                    String subbCmd = cmdTokens.get(0);
                    output_to_server.writeObject((Object)(formatCMD("rg "+subbCmd+" "+((ArrayList)(response.get(Integer.parseInt(cmd)))).get(2))));
                    output_to_server.flush();
                    Object imput = input_from_server.readObject();
                    System.out.println("Group: "+((ArrayList)((ArrayList)imput).get(0)).get(0));
                    System.out.println("Subject: "+((ArrayList)((ArrayList)imput).get(0)).get(1));
                    System.out.println("Author: "+((ArrayList)((ArrayList)imput).get(0)).get(2));
                    System.out.println("Date: "+((ArrayList)((ArrayList)imput).get(0)).get(3));
                    System.out.println(((ArrayList)((ArrayList)imput).get(0)).get(4));

                    //loint postid = Integer.parseInt(cmd);
                    id_handler_rg(postid, N);






                    //id_handler_rg(postid, N);
                } catch(NumberFormatException nfe) {
                    System.out.println("ERROR: NO SUCH COMMAND");
                    printSubCMDMenu_RG();
                }
            }
        } while(true);
    } /* end of rg_mode */
    
    /**
     * Tokenizes command 
     * @param command
     * @return tokens
     */
    public static ArrayList<String> tokenizeCMD(String command) {
        ArrayList<String> tokens = new ArrayList<>();
        for(String token : command.split(" ")) {
            tokens.add(token);
        }
        return tokens;
    }
    
    /**
     * Formats command to include all info needed by server 
     * Format: State, command(as array list), user object
     * @param command
     * @return a formatted client request
     */
    public static ArrayList<Object> formatCMD(String command) {
        ArrayList<Object> formattedCMD = new ArrayList<>();
        ArrayList<String> cmdTokens = tokenizeCMD(command);
        
        // not work
//        User parseUser = new User();
//        parseUser = user;
        
        // work
//        User parseUser = new User(user.getId());
//        formattedCMD.add(parseUser);

        formattedCMD.add(state);
        formattedCMD.add(cmdTokens);
        formattedCMD.add(user);

        
        return formattedCMD;
    }

    /**
     * 
     * @param response server response - the user object
     */
    private static void handleLoginResponse() {
        state = State.LOGGED_IN;
        /*TODO: store user info locally (if user data already exists, 
        overwrite it)*/
        user = (User)response;
        dataManager.saveUserData(user);

        System.out.println("<< Logged successfully into server");
    }
    
    /***********************************************
                      ag
     ***********************************************/
    /**
     * Takes "s" "u" or "n" as argument, performs operations pertaining to each command
     * @param subCMD 
     */
    private static void ag_handler() {
        //TODO
       
    }
    
    /**
     * lists the next N discussion groups
     * @param allGroups all group names
     * @param startPrintNum start printing on this index
     * @param endPrintN number of prints
     */
    static void n_handler_ag(ArrayList<String> allGroups, ArrayList<String> subscribedGroups, int startPrintN, int endPrintN) {
        
        while (startPrintN < endPrintN && startPrintN < allGroups.size()) {
            // If subscribed group list contains current group, add (s) in front, else leave it empty
            if (subscribedGroups.contains(allGroups.get(startPrintN)))
                System.out.println("(" + (startPrintN + 1) + ") (s) " + allGroups.get(startPrintN));
            else
                System.out.println("(" + (startPrintN + 1) + ") ( ) " + allGroups.get(startPrintN));
            startPrintN++;
            
            // If all groups are displayed, the program exits from the ag command mode
            if(startPrintN >= allGroups.size()) {
                isLastGroup = true;     // return true, so that user exits from ag mode
                break;
            }
        }
        
    }
    
    /**
     * - subscribe to groups
     */
    static void s_handler_ag(int subsIndex, ArrayList<String> allGroups) {
        // Adds to subscribed group list
        user.subscribeGroup(allGroups.get(--subsIndex));
        System.out.println("You've successfully subscribed to " + allGroups.get(subsIndex));
        // Saves to local user file
        // dataManager = new DataManager();
        dataManager.saveUserData(user);
    }

    /**
     *	unsubscribe a group
     */
    static void u_handler_ag(int subsIndex, ArrayList<String> allGroups) {
        // Removes group at subsIndex
        user.unsubscribeGroup(allGroups.get(--subsIndex));
        System.out.println("You've successfully unsubscribed to " + allGroups.get(subsIndex));
        // Saves to local user file
        // dataManager = new DataManager();
        dataManager.saveUserData(user);
    
    }


    
    /***********************************************
                      sg
     ***********************************************/
    private static void sg_handler(String command) {
        //TODO:
    }
    /**
     * marks posts in range of [h,t] as read 
     * @param h head of range
     * @param t tail of range
     */
    static void r_handler_sg(int h, int t) {
    
    }

    /**
     *	lists the next N discussion groups
     */
    static void n_handler_sg(int N) {
        //TODO:
    }
    
    /***********************************************
                      rg
     ***********************************************/
    private static void rg_handler(String command, int N) {
        //TODO: 
        if(command.equals("r")){
            // TODO: use cmdTokens to get h and t for 
            // lizi tong xue you need to parse the 
            // r_handler_rg(h, t);
            
        } else if(command.equals("n")) {
            //TODO: 
            n_handler_rg(N);
            
        } else if(command.equals("p")) {
            /*TODO: 
                The client program prompts the user for a line denoting the post subject, 
                and then the content of the post, until some special character sequence, 
                such as “\n.\n” – a dot by itself on a line, which denotes the end of post, 
                is entered by the user. The post is then submitted to the server. 
                Afterwards, a new list of N posts should be displayed, 
                including the newly submitted post which is shown as unread.
            */
            p_handler_rg();
        }
        
    }

    /**
     * lists the next N posts.
     * If all posts are displayed, the program exits from the rg command mode
     */
    static void n_id_handler_rg(int N) {
        //TODO:
    }

    /**
     * would quit displaying the post content.
     * The list of posts before opening the post is shown
     * again with the post just opened marked as read.
     */
    private static void id_handler_rg(int postid, int N) {
        // set state, update prompt 
        // update current state as "rg"
        state = State.IN_RG_ID;
        
        updatePrompt();
        do { // listen for user commands 
            printPrompt();
            command = user_input_scn.nextLine();
            if(command.equals("q")) {
                System.out.println("################################");
                System.out.println("#          read groups         #");
                System.out.println("################################");
                state = State.IN_RG;
                break;
            } 
            else if(command.equals("n")) {
                // display at most N more lines of the post content
                n_id_handler_rg(N);
            } else {
                System.out.println("ERROR: NO SUCH COMMAND");
                // print list of available commands: q and n 
                printSubCMDMenu_RG_ID();
            }
        } while(true);
        
    }


    /**
     * marks post in range of h to t as read
     */
    static void r_handler_rg(int h, int t) {
        /*TODO: check range, the commands that trigger this method 
                can be of the form "r NUMBER", or "r NUMBER-NUMBER"
                
        
                for each post in range h to t
                    readPost()
        */
        
        
    }

    /**
     *	lists the next N discussion groups
     */
    static void n_handler_rg(int N) {
        //TODO: lists the next N discussion groups
    }
    
    /**
     *	post to the group
     */
    static void p_handler_rg() {
        //TODO: construct a post, send it to server
        
    }

    
    /**
     * Checks each command's usage to validate if the command is valid.
     * @param command
     * @return true if valid, false otherwise
     */
    private static boolean isCMDValid(String command) {
        
        String cmd = cmdTokens.get(0);
        if((cmd.equals(Constants.AG)) || (cmd.equals(Constants.SG))
        || (cmd.equals(Constants.RG)) || (cmd.equals(Constants.HELP))
        || (cmd.equals(Constants.LOGOUT) || cmd.equals(Constants.LOGIN)) ) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Further validates command (check illegal arguments)
     * @return 
     */
    private static boolean validateCMD() {
        String cmd = cmdTokens.get(0);
        if(cmd.equals(Constants.LOGIN)) {
            if(state.equals(State.LOGGED_IN)) {
                System.out.println("<< Error: Already logged in with user: " + user.getId());
                return false;
            } else if(cmdTokens.get(1).length() > 9) {
                // if the second argument is not in 0 - 999999999, return  false 
                System.out.println("<< Error: Invalid ID");
                return false;
            } else { // if the id argument is not valid digits, return false
                try {
                    Integer.parseInt(cmdTokens.get(1));
                    return true;
                } catch(NumberFormatException nfe) {
                    System.out.println("<< Error: Invalid ID");
                    return false;
                }
            }
        } else if(cmd.equals(Constants.AG) || cmd.equals(Constants.SG)) {
            if(cmdTokens.size() > 1) {
                try {
                    if(Integer.parseInt(cmdTokens.get(1)) > 0) {
                        return true;
                    } else {
                        System.out.println("<< Error: Invalid number");
                        return false;
                    }
                } catch(NumberFormatException nfe) {
                    System.out.println("<< Error: Invalid number");
                    return false;
                }
            } else {
                return true;
            }
        } else if(cmd.equals(Constants.RG)) {
            if(cmdTokens.size() > 1) {
                if(cmdTokens.size() > 2) { //meaning if there is a supposed number argument
                    try {
                        if(Integer.parseInt(cmdTokens.get(2)) > 0) {
                            return true;
                        } else {
                            System.out.println("<< Error: Invalid number");
                            return false;
                        }
                    } catch(NumberFormatException nfe) {
                        System.out.println("<< Error: Invalid number");
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                System.out.println("Usage: rg gname [N]");
                return false;
            }
        } else {
            return false;
        }
        
    }
    
    /**
     * Validates sub commands
     * @return 
     */
    private static boolean validateSubCMD() {
        String cmd = cmdTokens.get(0);
        if(((cmd.equals("s") || cmd.equals("u")) && state.equals(State.IN_AG))
          || (cmd.equals("u") && state.equals(State.IN_SG))) {
            // the "s" sub command has 1 or more arguments
            if(cmdTokens.size() == 1) {
                if(cmd.equals("s"))
                    System.out.println("Usage: s HEAD [TAIL]");
                else
                    System.out.println("Usage: u HEAD [TAIL]");
                return false;
            } else {
                boolean isValid = true;
                for(int i = 1; i < cmdTokens.size(); i++) {
                    try {
                        if(Integer.parseInt(cmdTokens.get(i)) > 0) {
                            
                        } else {
                            System.out.println("<< Error: \"" + cmdTokens.get(i) + "\" is not a valid number");
                            isValid = false;
                            break;
                        }
                    } catch(NumberFormatException nfe) {
                        System.out.println("<< Error: \"" + cmdTokens.get(i) + "\" is not a valid number");
                        isValid = false;
                        break;
                    }
                }
                return isValid;
            }
        } else if((cmd.equals("r") && state.equals(State.IN_RG))) {
            /* r can have a single number as argument 
               or a number range in the form of NUM-NUM
            */
            if(cmdTokens.size() == 1) {
                return false;
            } else {
                try {
                    if(Integer.parseInt(cmdTokens.get(1)) > 0) {
                        return true;
                    } else {    
                        System.out.println("<< Error: Invalid number");
                        return false;
                    }
                } catch(NumberFormatException nfe) {
                    // check range 
                    // split cmd on "-", check left and right
                    
                    ArrayList<String> numberRage = new ArrayList<>();
                    for(String token : command.split("-")) {
                        numberRage.add(token);
                    }
                    
                    try {
                        if(Integer.parseInt(numberRage.get(0)) > 0 && Integer.parseInt(numberRage.get(1)) > 0) {
                            return true;
                        } else {
                            System.out.println("<< Error: Invalid number(s)");
                            return false;
                        }
                    } catch(NumberFormatException e) {
                        System.out.println("<< Error: Invalid number(s)");
                        return false;
                    }
                }
            }
        } else {
            /* should never be able to get to here */
            return false;
        }
    }
    
    /**
     * Updates prompt for each command
     */
    private static void updatePrompt() {
        String name;
        String mode;
        /* if user is not in ag, rg, sg mode or their sub mode, then prompt
           should be of the form: */
        if(state.equals(State.NOT_LOGGED_IN)) {
            name = "GUEST";
            mode = "Main Menu";
        } else {
            name = user.getId();
            if(state.equals(State.IN_AG)) {
                mode = "ag";
            } else if(state.equals(State.IN_SG)) {
                mode = "sg";
            } else if(state.equals(State.IN_RG)){
                mode = "rg";
            } else if(state.equals(State.IN_RG_ID)) {
                mode = "rg-[id]";
            } 
            // if(state.equals(State.LOGGED_IN)) {
            else {
                name = user.getId();
                mode = "Main Menu";
            }
        }        
        prompt = mode + ": " + name + "@" + hostmachine + " >> ";        
    }
    
    private static void printPrompt() {
        updatePrompt();
        System.out.print(prompt);
    }
    
    private static void printMainMenuHeader() {
        System.out.println("################################");
        System.out.println("#          main menu           #");
        System.out.println("################################");
    }    
    
    /**
     * Prints help menu
     */
    public static void printHelpMenu() {
        // TODO: format help menu, add sub commands
        System.out.println("###############################################");
        System.out.println("############### HELP MENU #####################");
        System.out.println("###############################################");
        System.out.println("# help                print this menu         #");
        System.out.println("# login USERID        log in with user id     #");
        System.out.println("# ag [N]              show all groups         #");
        System.out.println("# sg [N]              show subscribed groups  #");
        System.out.println("# rg gname [N]        read groups             #");
        System.out.println("# logout         log out and exit the program #");
        System.out.println("###############################################");
    }

    /**
     * Prints ag's sub commands' menu
     */
    private static void printSubCMDMenu_AG() {
        System.out.println("s HEAD [TAIL] – subscribe to groups in range of HEAD and TAIL (TAIL is optional)\n"
                         + "u HEAD [TAIL] – unsubscribe\n"
                         + "n             – lists the next N discussion groups\n" 
                         + "q             – exits from the ag command\n");
    }
    
    /**
     * Prints sg's sub commands' menu
     */
    private static void printSubCMDMenu_SG() {
        System.out.println("u HEAD [TAIL] – unsubscribe\n"
                         + "n             – lists the next N discussion groups\n" 
                         + "q             – exits from the ag command\n");
    }

    /**
     * Prints rg's sub commands' menu
     */
    private static void printSubCMDMenu_RG() {
        System.out.println("[id]          - display the post denoted by id\n" 
                         + "u HEAD [TAIL] – unsubscribe\n"
                         + "n             – lists the next N discussion groups\n" 
                         + "q             – exits from the ag command\n");
    }
        /**
     * Prints sg's sub commands' menu
     */
    private static void printSubCMDMenu_RG_ID() {
        System.out.println("n             – lists the next N discussion groups\n" 
                         + "q             – exits from the ag command\n");
    }

}
