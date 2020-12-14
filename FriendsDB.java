import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.Calendar;
import java.sql.*;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.swing.*; 
import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FriendsDB {

  private Connection connection = null;

  static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

  public FriendsDB(String u_name, String pass){
    // constructs arguments for connection
    System.out.print("Connecting to database...");
    try{
      String url = "jdbc:postgresql://localhost:5432/postgres";
      //String url = "jdbc:postgresql://localhost/5432/Mug";
      String username = u_name;
      String password = pass;
     // obtain a physical connection
      this.connection = DriverManager.getConnection(url, username, password);
      System.out.println("Done");
    }catch (Exception e){
       System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
       System.out.println("Make sure you started postgres on this machine");
       System.exit(-1);
    }//end catch
  }//end 

  public int executeUpdate (String sql) throws SQLException {
  // creates a statement object
    Statement stmt = this.connection.createStatement ();

    // issues the update instruction
    int val =stmt.executeUpdate (sql);

    // close the instruction
    stmt.close ();
    return val;
  }//end executeUpdate
  public ResultSet executeQuery (String query) throws SQLException {
    // creates a statement object
    Statement stmt = this.connection.createStatement ();

    // issues the query instruction
    ResultSet rs = stmt.executeQuery (query);
    return rs;
  }//end executeQuery

   public void cleanup(){
      try{
         if (this.connection != null){
            this.connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

  public static void main (String[] args) {
    if (args.length != 2) {
       System.err.println ("Need username and password");
       return;
    }//end if

    FriendsDB esql = null;
    try{
       // instantiate the DBProject object and creates a physical
       // connection.
       String username = args[0];
       String password = args[1];
       esql = new FriendsDB (username, password);
       
       test(esql);  //used for testing
    
    }catch(Exception e) {
         System.err.println (e.getMessage ());
    }  


       // make sure to cleanup the created table and close the connection.
	  try{
  	  if(esql != null) {
  	     esql.cleanup ();
  	     System.out.println("\nExiting...");
         System.exit(0);
  	  }//end if
  	}catch (Exception e) {
  	  // ignored.
  	}//end catch
 }//end main
     
  //Used to get Date in addPhoto
  public static String getToday() {
    Date todayDate = Calendar.getInstance().getTime();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    String todayString = formatter.format(todayDate);
    return todayString;
  }   

  public static int getuserid(FriendsDB esql){ //helper function for addUser
    try {
      String query = "SELECT useridcnt FROM Userid;";
      ResultSet rs = esql.executeQuery(query);
      if(rs.next()){
      int user_id = rs.getInt(1);
      return user_id;
      }
    }catch(Exception e) {
      System.err.println(e.getMessage());
        
    } 
    return -1;
  }

  public static int getphotoid(FriendsDB esql){ //helper function for addPhoto
    try {
      String query = "SELECT photoidcnt FROM Photoid;";
      ResultSet rs = esql.executeQuery(query);
      if(rs.next()){
      int photo_id = rs.getInt(1);
      return photo_id;
      }
    }catch(Exception e) {
      System.err.println(e.getMessage());
        
    } 
    return -1;
  }

    //Adding a user
  public static void addUser(FriendsDB esql, String user_name, String email, String pw){
    try {
         
          int user_id = getuserid(esql);
          String query = "INSERT INTO Users VALUES (" + user_id + ", \'" + user_name + "\', \'" + email + "\', \'" + pw + "\', " +
           0 + ", " + 0 + ");\nUPDATE userid \nSET useridcnt = useridcnt + 1;";
          esql.executeQuery(query);
      }catch(Exception e) {
         String errormsg = null;
         if(e.getMessage().contains("duplicate")){
          errormsg = "\nThis username is already taken!";
         System.err.println(errormsg);  
       }else if(e.getMessage().contains("long")){
        errormsg = "\nUsername and password cannot exceed 30 characters";
        System.err.println(errormsg);  
        }else{
        System.out.println("\nAccount created successfully!");
      } 
    }
  }
  //end addUser

  //Adding a photo
  public static void addPhoto(FriendsDB esql, int user_id, String filename, String title, String caption){
    String today = getToday();
    try {
      int photo_id = getphotoid(esql);
      
      File file = new File(filename);
      FileInputStream fis = new FileInputStream(file);
      Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","230138jl");
      PreparedStatement ps = conn.prepareStatement("INSERT INTO Photos VALUES (?, ?, ?, ?, ?, ?, ?);");
      ps.setInt(1, photo_id);
      ps.setInt(2, user_id);
      ps.setString(3, file.getName());
      ps.setBinaryStream(4, fis, (int)file.length());
      ps.setString(5, title);
      ps.setString(6, caption);
      ps.setString(7, today);
      ps.executeUpdate();
      ps.close();
      fis.close();
      initializeStats(esql, photo_id);
      initUserTags(esql, photo_id);
      initPhotoTags(esql, photo_id);
      esql.executeUpdate("UPDATE Photoid \nSET photoidcnt = photoidcnt + 1");
      System.out.print("\nPhoto uploaded! Your Photo ID is: " + photo_id +"\n");
    }catch(Exception e) {
      System.err.println("\nThe system could not find the file " + filename + ". Make sure the file is stored in the same directory as the java executable.");   
    } 
  }//end addPhoto

  public static void initUserTags(FriendsDB esql, int photo_id) {
    
    try{
      String arr = "\'{}\'";
      esql.executeUpdate("INSERT INTO User_tag VALUES (" + photo_id + ", " + arr + ")");
    }catch(Exception e) {
      System.err.println(e.getMessage()); 
    }

  }

  public static void initPhotoTags(FriendsDB esql , int photo_id) {
    
    try{
      String arr = "\'{}\'";
      esql.executeUpdate("INSERT INTO Photo_tag VALUES (" + photo_id + ", " + arr + ")");
    }catch(Exception e) {
      System.err.println(e.getMessage()); 
    }

  }

  public static boolean canAddPhoto(FriendsDB esql, String filename){ //checks to see if photo user is trying to upload is found
    try {
      int photo_id = getphotoid(esql);
      
      File file = new File(filename);
      FileInputStream fis = new FileInputStream(file);
      fis.close();
      return true;
    }catch(Exception e) {
      System.err.println("\nThe system could not find the file " + filename + ". \nMake sure the file is stored in the same directory as the java executable.");   
      return false;
    }
  }

  //Adding tag
  public static void addTags(FriendsDB esql, int photo_id, ArrayList<String> tags){
   String tag;
    for(int i = 0; i < tags.size(); ++i){
      tag = tags.get(i);
      try {
        
        if(inPhotoTags(esql, photo_id, tag)){
          System.err.println("You have already tagged this photo using the tag " + "\"" + tag + "\""); 
        }else{
          String query = "UPDATE Photo_tag SET tag = array_append (tag, \'" + tag + "\') WHERE photo_id = " + photo_id;
          esql.executeUpdate(query);
          System.out.print("Photo tag " + "\"" + tag + "\"" + " added!\n");
        }
        
      } catch(Exception e) {
         System.err.println(e.getMessage());   
      } 
    }
  }//end addTag

  //Adding user tag
  public static void addUserTags(FriendsDB esql, int photo_id, ArrayList<String> utags){
    String username;
    for(int i = 0; i < utags.size(); ++i){
      username = utags.get(i);
      try {
        if(username2id(esql, username) == -1) {
          System.err.println("Username \"" + username + "\" does not exist");
        }else if(inUserTags(esql, photo_id, username)){
          System.err.println("You have already tagged " + username + " to this photo"); 
        }else{
          String query = "UPDATE User_tag SET user_name = array_append (user_name, \'" + username + "\') WHERE photo_id = " + photo_id;
          esql.executeUpdate(query);
          System.out.print("You have tagged " + username + "!\n");
        }
        
      } catch(Exception e) {
         System.err.println(e.getMessage());   
      } 
    }
  }//end addUserTag

  public static boolean inPhotoTags(FriendsDB esql, int photo_id, String tag) { //return true if username is found in photo_tag array
     try {
        ResultSet rs = esql.executeQuery("SELECT * FROM Photo_tag WHERE \'" + tag + "\' = ANY(tag) AND photo_id = " + photo_id);
        if(!rs.isBeforeFirst()){
          return false;
        }
        return true;
      } catch(Exception e) {
        System.err.println(e.getMessage());
        return true;
      }
  }

  public static boolean inUserTags(FriendsDB esql, int photo_id, String username) { //return true if username is found in user_tag array
     try {
        ResultSet rs = esql.executeQuery("SELECT * FROM User_tag WHERE \'" + username + "\' = ANY(user_name) AND photo_id = " + photo_id);
        if(!rs.isBeforeFirst()){
          return false;
        }
        return true;
      } catch(Exception e) {
        System.err.println(e.getMessage());
        return true;
      }
  }

  //Adding Stats
  public static void initializeStats(FriendsDB esql, int photo_id){
    try {
          String query = "INSERT INTO Photo_stats VALUES (" + photo_id + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 0 + ");";
      esql.executeUpdate(query);
    }catch(Exception e) {
      System.err.println(e.getMessage());   
    } 
  }//end addStats

  public static void addLike(FriendsDB esql, int photo_id){
    try {
      String query ="UPDATE photo_stats SET num_likes = num_Likes + 1 WHERE photo_id = " + photo_id;
      //query = query + ";\nUPDATE Photo_stats\nSET num_views = num_views + 1\nWHERE photo_id = " + photo_id + ";";
      esql.executeUpdate(query);     
    }catch(Exception e) {
      System.err.println(e.getMessage());   
    } 
  }//end addLike

  public static void addDislike(FriendsDB esql, int photo_id){
    try {
      String query ="UPDATE photo_stats SET num_dislikes = num_dislikes + 1 WHERE photo_id = " + photo_id ;
      //query = query + ";\nUPDATE Photo_stats\nSET num_views = num_views + 1\nWHERE photo_id = " + photo_id + ";";
      esql.executeUpdate(query);     
    }catch(Exception e) {
      System.err.println(e.getMessage());   
    } 
  }//end addDislike

  //Adding follows
  public static boolean addFollows(FriendsDB esql, int user_id, int following_id){
      try {
            String query = "INSERT INTO Follows VALUES(" + user_id + ", " + following_id + ");\nUPDATE Users\nSET num_following = num_following + 1\nWHERE user_id = " + user_id + ";\nUPDATE Users\nSET num_followers = num_followers + 1\nWHERE user_id =" + following_id + ";";
             esql.executeUpdate(query);
             return true;
      }catch(Exception e) {
        if(e.getMessage().contains("duplicate")){
          System.err.println("\nYou are already following this user");  
          return false;
        }else{
          System.err.println(e.getMessage());  
          return false;
        }
         
      } 
  }//end addFollows

  //Adding comments
  public static void addComments(FriendsDB esql, int photo_id, int commenter_id, String comment){
      try {
            String query = "INSERT INTO Comments VALUES(" + photo_id + ", " + commenter_id + ", \'" + comment + "\');\nUPDATE Photo_stats\nSET num_comments = num_comments + 1\nWHERE photo_id = " + photo_id;
            query = query + ";\nUPDATE Photo_stats\nSET num_views = num_views + 1\nWHERE photo_id = " + photo_id + ";";
        esql.executeUpdate(query);
      }catch(Exception e) {
        System.err.println(e.getMessage());   
      } 
    }
  //end comments


  //find photos through tags
  public static void searchPhotoTag(FriendsDB esql, String tag){
      try {
        ResultSet rs = esql.executeQuery("SELECT photo_id FROM Photo_tag WHERE \'" + tag + "\' = ANY(tag)");
        if(!rs.isBeforeFirst()){
          System.out.println("\nNo photos tagged with " + tag);
          return;
        }

      System.out.println("\nPhoto ID(s) found: ");
        while(rs.next()){
          int photo_id = rs.getInt(1);
          System.out.print(photo_id + "\n");
        }

      } catch(Exception e) {
        System.err.println(e.getMessage());
      }
  }
  //end searchPhotoTag

  //search photos by likes, get top 100
  public static void searchPhotoLikes(FriendsDB esql){
    try {
      ResultSet rs = esql.executeQuery("SELECT photo_id, num_likes FROM Photo_stats ORDER BY num_likes DESC LIMIT 100");
      if(!rs.isBeforeFirst()){
          System.out.println("\nNo photos in database. Upload a photo first");
          return;
        }
      System.out.print("\n");
      while(rs.next()){
        int photo_id = rs.getInt(1);
        int num_likes = rs.getInt(2);
        System.out.print("Photo ID: " + photo_id + " Likes: " + num_likes +"\n");
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }
  //end searchLikes

  //search Photos by Dislikes, get top 100
  public static void searchPhotoDislikes(FriendsDB esql){
    try {
      ResultSet rs = esql.executeQuery("SELECT photo_id, num_dislikes FROM Photo_stats ORDER BY num_dislikes DESC LIMIT 100");
       if(!rs.isBeforeFirst()){
          System.out.println("\nNo photos in database. Upload a photo first");
          return;
        }
      System.out.print("\n");
      while(rs.next()){
        int photo_id = rs.getInt(1);
        int num_dislikes = rs.getInt(2);
        System.out.print("Photo ID: " + photo_id + " Dislikes: " + num_dislikes +"\n");
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }
  //end searchPhotoDislikes

  //Search photos by date
  public static void searchPhotoDate(FriendsDB esql, String day){  //day must have format "YYYY-MM_DD"
    try {
      
      ResultSet rs = esql.executeQuery("SELECT photo_id FROM Photos WHERE day = \'" + day + "\' LIMIT 10");
      if(!rs.isBeforeFirst()){
          System.out.println("\nNo photos uploaded on " + day);
          return;
        }

      System.out.println("\nPhoto ID(s) found: ");
      while(rs.next()){
        int photo_id = rs.getInt(1);
        System.out.print(photo_id + "\n");
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }
  //end searchPhotoDate

  //Search photos by user
  public static void searchPhotoUser(FriendsDB esql, String user_name){  
    try {
     
      ResultSet rs = esql.executeQuery("SELECT photo_id FROM Photos WHERE user_id IN (SELECT user_id FROM Users WHERE user_name = \'" + user_name + "\')" );
      if(!rs.isBeforeFirst()){
          System.out.println("\nNo photos uploaded by " + user_name);
          return;
        }

      System.out.println("\nPhoto ID(s) found: ");
      while(rs.next()){
        int photo_id = rs.getInt(1);
        System.out.print(photo_id + "\n");
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }
  //end searchPhotoUser

   //search most popular users
  public static void searchPopularUsers(FriendsDB esql){
    try {
      //display top 100 popular users 
      ResultSet rs = esql.executeQuery("SELECT user_name, num_followers FROM Users ORDER BY num_followers DESC LIMIT 100");
      if(!rs.isBeforeFirst()){
          System.out.println("\nNo users in database. Add some users first");
          return;
        }
      System.out.println("\nUsers with most followers: \n");
      while(rs.next()){
        String user_name = rs.getString(1);
        int num_followers = rs.getInt(2);
        System.out.print(user_name + " - " + num_followers + " followers\n");
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }
  //end searchPopularUsers

   public static void newsFeed(FriendsDB esql, int user_id){

    try {
      //photos from all the people the user follows ordered by likes
      String query = "SELECT title, Users.user_name, Photo_stats.num_likes, day, caption, img, Photos.photo_id, Photo_stats.num_dislikes, Photo_stats.num_views, Photo_tag.tag, User_tag.user_name FROM Photos";
      query = query + " JOIN Users ON Photos.user_id = Users.user_id JOIN Photo_stats ON Photos.photo_id = Photo_stats.photo_id JOIN Photo_tag ON Photos.photo_id = Photo_tag.photo_id JOIN User_tag ON Photos.photo_id = User_tag.photo_id";  //JOIN Photo_tag ON Photos.photo_id = Photo_tag.photo_id JOIN User_tag ON Photos.photo_id = User_tag.photo_id
      query = query + " WHERE Photos.user_id IN (SELECT following_id FROM Follows WHERE user_id = " + user_id + ")ORDER BY num_likes, num_views;";
      int cnt = 0;

      ArrayList<Integer> a = new ArrayList<Integer>();
      ResultSet rs = esql.executeQuery(query);
      if(!rs.isBeforeFirst()){
          System.out.println("\nYou have nothing in your newsfeed. Try following some users first!");
          return;
        }
      while(rs.next()){
        cnt++;
        String title = rs.getString(1);
        String username = rs.getString(2);
        int num_likes = rs.getInt(3);
        String date = rs.getString(4);
        String caption = rs.getString(5);
        byte[] img = rs.getBytes(6);
        a.add(rs.getInt(7));
        int num_dislikes = rs.getInt(8);
        int num_views = rs.getInt(9);
        Array p = rs.getArray(10);
        Array u = rs.getArray(11); 
        String[] ptagarr = (String[])p.getArray();  
        String[] utagarr = (String[])u.getArray();     
        JFrame frame = new JFrame("NEWS FEED");
        String ptag = "Tags: ";
        String utag = "Users tagged: ";
        for(int i =0; i < ptagarr.length; ++i){
          ptag = ptag + ptagarr[i] + " ";
        }
        for(int i =0; i < utagarr.length; ++i){
          utag = utag + utagarr[i] + " ";
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(img);
        BufferedImage bImg = ImageIO.read(bis);
        ImageIcon imageIcon = new ImageIcon(bImg);
        JLabel jLabel = new JLabel(imageIcon, SwingConstants.CENTER);
        JLabel titletext = new JLabel("<html>" + username + "<br/>" + title + "</html>");
        JLabel captiontext = new JLabel(caption);
        JLabel stats = new JLabel("Date: " + date + " Likes: " + num_likes + " Dislikes: " + num_dislikes + " View Count: " + num_views);
        JLabel tagstext = new JLabel("<html>" + ptag + "<br/>" + utag + "</html>");
        tagstext.setFont(tagstext.getFont().deriveFont(40.0f));
        titletext.setFont (titletext.getFont().deriveFont (80.0f));
        captiontext.setFont (captiontext.getFont ().deriveFont (40.0f));
        stats.setFont(stats.getFont().deriveFont(20.0f));
        frame.getContentPane().add(jLabel, BorderLayout.CENTER);
        frame.add(captiontext, BorderLayout.LINE_START);
        frame.add(titletext, BorderLayout.PAGE_START);
        frame.add(stats, BorderLayout.SOUTH);
        frame.add(tagstext, BorderLayout.EAST);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
          
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

          
      }

      for(int i =0; i<cnt; ++i){
        addViewCnt(esql, a.get(i));
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }


 public static void addViewCnt(FriendsDB esql, int id){ //helper function for newsFeed
    try {
      
     esql.executeUpdate("UPDATE Photo_stats\nSET num_views = num_views + 1\nWHERE Photo_stats.photo_id = " + id + ";");

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }




  //find users based on phototags
  public static void searchUserOnTag(FriendsDB esql, String tag){
    try {
      
       ResultSet rs = esql.executeQuery("SELECT user_name FROM Users WHERE user_id IN (SELECT user_id FROM Photos WHERE photo_id IN (SELECT photo_id FROM Photo_tag WHERE \'" + tag + "\' = ANY(tag)))");
       
       if(!rs.isBeforeFirst()){
          System.out.println("\nNo users with photos tagged with " + tag);
          return;
        }
      System.out.print("\nUsername results: " + "\n");
      
      while(rs.next()){
        String user_name = rs.getString(1);
        
        System.out.print(user_name + "\n");
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }
   public static void searchUserOnTitle(FriendsDB esql, String title){
    try {
      //find users based on photo title
      
       ResultSet rs = esql.executeQuery("SELECT user_name FROM Users WHERE user_id IN (SELECT user_id FROM Photos WHERE title = \'" + title + "\')");
      
       if(!rs.isBeforeFirst()){
          System.out.println("\nNo users with photo titled " + title);
          return;
        }

      System.out.print("\nUsername results: " + "\n");

      while(rs.next()){
        String user_name = rs.getString(1);
        
        System.out.print(user_name + "\n");
      }

    } catch(Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public static void viewPhoto(FriendsDB esql, int photo_id){ 
      try {

        //pass a photo_id to view the img.
        ResultSet rs = esql.executeQuery("SELECT img, photo_id FROM Photos WHERE photo_id = " + photo_id + ";");
        if(!rs.isBeforeFirst()){
          System.out.println("\nPhoto ID " + photo_id + " does not exist");
        }

        while(rs.next()){
          byte[] img = rs.getBytes(1);
          int pid = rs.getInt(2);
          
          JFrame frame = new JFrame("Photo ID: " + pid);
          
          ByteArrayInputStream bis = new ByteArrayInputStream(img);
          BufferedImage bImg = ImageIO.read(bis);
          
          ImageIcon imageIcon = new ImageIcon(bImg);
          
          JLabel jLabel = new JLabel(imageIcon, SwingConstants.CENTER);
          
          frame.getContentPane().add(jLabel, BorderLayout.CENTER);
          
          frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
          
          frame.pack();
          frame.setLocationRelativeTo(null);
          frame.setVisible(true);
         

        }
       rs.close();
       esql.executeUpdate("UPDATE Photo_stats\nSET num_views = num_views + 1\nWHERE photo_id = " + photo_id + ";");

      } catch(Exception e) {
        System.err.println(e.getMessage());
      }
  }

   public static void downloadPhoto(FriendsDB esql, int photo_id, String save_as) { 
      try {

        //pass a photo_id to download the img.
        ResultSet rs = esql.executeQuery("SELECT img FROM Photos WHERE photo_id = " + photo_id + ";");
        if(!rs.isBeforeFirst()){
          System.out.println("photo id " + photo_id + " does not exist");
        }
        String suffix = "null";
        if(save_as.contains(".jpg")){
          suffix = "jpg";
        }else if(save_as.contains(".png")){
          suffix = "png";
        }
        while(rs.next()){
          byte[] img = rs.getBytes(1);

          ByteArrayInputStream bis = new ByteArrayInputStream(img);
          BufferedImage bImg = ImageIO.read(bis);
          ImageIO.write(bImg, suffix, new File(save_as));
        }
      rs.close();

      } catch(Exception e) {
        System.err.println(e.getMessage());
      }
  }

  public static int verifyLogin(FriendsDB esql, String username, String pw) { //verify username and password are correct and return the corresponding user_id

    try{
        int currID = -1;
        ResultSet rs = esql.executeQuery("SELECT user_id FROM users WHERE user_name = \'" + username + "\' AND password = \'" + pw + "\'");
        if(!rs.isBeforeFirst()){
           System.out.println("\nWrong username or password. Please try again.");
           return -1;
         }
         while(rs.next()){
          currID = rs.getInt(1);
          
         }
        return currID;
      }catch(Exception e) {
        System.err.println(e.getMessage());
        return -1;
      }
    
  }

  public static int username2id(FriendsDB esql, String username) { //verify username exists and return the corresponding user_id

    try{
        int userID = -1;
        ResultSet rs = esql.executeQuery("SELECT user_id FROM users WHERE user_name = \'" + username + "\'");
        if(!rs.isBeforeFirst()){
           return -1;
         }
         while(rs.next()){
          userID = rs.getInt(1);
          
         }
        return userID;
      }catch(Exception e) {
        System.err.println(e.getMessage());
        return -1;
      }
    
  }

   public static int isUsersPhoto(FriendsDB esql, int user_id, int photo_id) { //checks if photo exists and user is the owner of a photo

    try{
        int userID = 0;
        ResultSet rs = esql.executeQuery("SELECT user_id FROM Photos WHERE photo_id = \'" + photo_id + "\'");
        if(!rs.isBeforeFirst()){
           return -1;
         }
         while(rs.next()){
          userID = rs.getInt(1);
          
         }
         if(userID == user_id){
          return 1;
         }
          else {
            return -2;
          }
      }catch(Exception e) {
        System.err.println(e.getMessage());
        return -3;
      }  
  }

  public static int doesPhotoExist(FriendsDB esql, int photo_id) { //checks if photo exists 

    try{
        
        ResultSet rs = esql.executeQuery("SELECT * FROM Photos WHERE photo_id = \'" + photo_id + "\'");
        if(!rs.isBeforeFirst()){
           return -1;
         }
        return 1;
      }catch(Exception e) {
        System.err.println(e.getMessage());
        return -1;
      }
    
  }
  public static boolean isNumeric(String str) { 
    try {  
      Double.parseDouble(str);  
      return true;
     } catch(NumberFormatException e){  
        return false;  
    }  
}
//yyyy-mm-dd
//&& (str.charAt(4) == '-')
 public static boolean isValidDate(String str) { 
  if(str.length() != 10){
    return false;
  }
    if(isNumeric(str.substring(0,4))){
      if(isNumeric(str.substring(5,7))){
        if (isNumeric(str.substring(8,10))) {
          if((str.charAt(4) == '-') && str.charAt(7) == '-'){
          return true;
          }
        }
      }

    }  
      return false;
    
}

public static boolean isEmailValid(String email) {
   String regex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"; //regex from https://howtodoinjava.com/java/regex/java-regex-validate-email-address/
   Pattern pattern = Pattern.compile(regex);
   Matcher matcher = pattern.matcher(email);
   return matcher.matches();
}

 public static void discover(FriendsDB esql, int user_id) { //list id, title, username of most recent photos 
    try{        
        String query = "SELECT Photo_id, title, Users.user_name, day FROM Photos JOIN Users on Photos.user_id = Users.user_id WHERE NOT Photos.user_id = " + user_id + " ORDER BY to_date(day, \'YYYY-MM-DD\') ASC, title ASC LIMIT 50";
        ResultSet rs = esql.executeQuery(query);
        if(!rs.isBeforeFirst()){
            System.out.println("\nNo photos to discover");
         }else{

          while(rs.next()){
           int photo_id = rs.getInt(1);
           String title = rs.getString(2);
           String username = rs.getString(3);
           String day = rs.getString(4);
           System.out.println("\nPhoto ID: " + photo_id);
           System.out.println("Title: " + title);
           System.out.println("Username: " + username);
           System.out.println("Date: " + day);

          }

         }
           
      }catch(Exception e) {
        System.err.println(e.getMessage());    
        }
      }
 //INNER JOIN Photos on Users.user_id = Photos.user_id WHERE Users.user_id = " + user_id + "GROUP BY photo_id
 public static void myProfile(FriendsDB esql, int user_id) { //show stats of current user
    try{        
        String username = "null";
        int num_following = 0;
        int num_followers = 0;
        int cnt = 0;
        String query = "SELECT user_name, num_followers, num_following FROM Users WHERE user_id = " + user_id;
        ResultSet rs = esql.executeQuery(query);
       

        while(rs.next()){
         username = rs.getString(1);
         //int cnt = rs.getInt(2);
         num_followers = rs.getInt(2);
         num_following = rs.getInt(3);
         

        }
          cnt = countPosts(esql, user_id);
          System.out.println("\nUsername: " + username + "    userID: " + user_id + "    Posts: "  + cnt + "    Followers: " + num_followers + "    Following: " + num_following);
          System.out.print("\nMy Photo IDs: ");
          postIDs(esql, user_id);
          
      }catch(Exception e) {
        System.err.println(e.getMessage());    
        }
      }

public static int countPosts(FriendsDB esql, int user_id) { //count number of photos uploaded by a user
  try{
    int cnt = 0;
    String query = "SELECT COUNT(*) FROM Photos WHERE user_id = " + user_id + "GROUP BY user_id";
    ResultSet rs = esql.executeQuery(query);
    while(rs.next()){
        
         cnt = rs.getInt(1);
         return cnt;
       }
  }catch(Exception e) {
        System.err.println(e.getMessage());
        return 0;    
        }
        return 0;

}

public static void postIDs(FriendsDB esql, int user_id) { //print post ids of a user
  try{
    int id = 0;
    ArrayList<Integer> a = new ArrayList<Integer>();
    String query = "SELECT photo_id FROM Photos WHERE user_id = " + user_id;
    ResultSet rs = esql.executeQuery(query);
    while(rs.next()){
         id = rs.getInt(1);
         a.add(id);
       }
      for(int i = 0; i < a.size(); ++i){
            System.out.print(a.get(i) + " ");
          }
       System.out.print("\n");   
  }catch(Exception e) {
        System.err.println(e.getMessage());
            
        }
        
      }

  //TESTING 

  public static void test(FriendsDB esql){
  	try{
  		boolean keepon = true;
      boolean login = true;
      boolean dontExit = true;
      boolean moveon = false;
      Scanner input = new Scanner(System.in);
      int user_id;
      String username;
      int photo_id;
      int following_id;
      String day;
      String comment;
      String caption;
      String tag;
      String title;
      String filename;
      String save_as;
      String pw;
      int currID = -1;
      String str;
      String email;
      String currUser = null;
      String verifypw;
      int num;
      int val;
        while(dontExit) {

          while(login){

            //implement login here
            System.out.println("\nWelcome to Friends! Create an account or Login to get started.");
            System.out.println("-------------------");
            System.out.println("1. Create account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            switch(readChoice()) {
              case 1:
                System.out.print("Enter username(3-30 characters): ");
                username = input.nextLine();
                System.out.print("Enter email: ");
                email = input.nextLine();
                System.out.print("Enter password(4-100 characters): ");
                pw = input.nextLine();
                pw = pw.replaceAll("'","''");
                System.out.print("Verify password: ");
                verifypw = input.nextLine();
                verifypw = verifypw.replaceAll("'","''");
                username = username.replaceAll(" ", "");
                if(username.length() < 3) {
                  System.out.print("\nUsername must be at least 3 characters in length, excluding whitespaces, failed to create account\n");
                }else if(username.length() >30) {
                  System.out.print("\nUsername may not exceed 30 characters in length, excluding whitespaces, failed to create account\n");
                }else if (!isEmailValid(email)){
                  System.out.print("\nInvalid email address, failed to create account\n");
                }
                else if (!pw.equals(verifypw)) {
                   System.out.print("\nPasswords do not match, failed to create account\n");
                }else if(pw.length() < 4){
                  System.out.print("\nPassword must be at least 4 characters long, failed to create account\n");
                }
                else{
                   username = username.replaceAll("'","''");      
                   addUser(esql, username, email, pw); 
                }
                break;
              case 2: 
                System.out.print("Enter username: ");
                username = input.nextLine();
                System.out.print("Enter password: ");
                pw = input.nextLine();
                username = username.replaceAll("'","''");
                
                pw = pw.replaceAll("'","''");
                currID = verifyLogin(esql, username, pw);
                if (currID != -1) {
                  System.out.println("\nLogin successful");
                  currUser = username;
                  login = false;
                  keepon = true;
                }
                break;
              case 3:
                login = false;
                dontExit = false;
                keepon = false;
                break;
                   
              default : System.out.println("\nUnrecognized choice!"); break;
            }
          }
          while(keepon) {

      			System.out.println("\nWelcome " + currUser);
      			System.out.println("-------------------");               
      			System.out.println("1. Upload a photo");          
            System.out.println("2. View photo");
            System.out.println("3. Download photo");
            System.out.println("4. Add a tag to a photo");
      			System.out.println("5. Tag a user to a photo");       
      			System.out.println("6. Add a like to photo");     
            System.out.println("7. Add a dislike to photo");  
      			System.out.println("8. Follow a user");      
      			System.out.println("9. Add a comment");       
      			System.out.println("10. Search username by photo tag");        
            System.out.println("11. Search username by photo title"); 
      			System.out.println("12. Search photo likes");    
      			System.out.println("13. Search photo dislikes");  
      			System.out.println("14. Search photo by date");   
      			System.out.println("15. Search photo by user"); 
            System.out.println("16. Search photo by photo tag"); 
            System.out.println("17. Most popular users");    
            System.out.println("18. Display news feed"); 
            System.out.println("19. Discover photos"); 
            System.out.println("20. View my profile"); 
            System.out.println("21. Sign Out"); 
      			System.out.println("22. EXIT");

            switch (readChoice()){
    		   
    			    case 1: 
                System.out.print("\nEnter file name of photo: ");
                user_id = currID;
                filename = input.nextLine();
                if(!(filename.endsWith(".jpg") ^ filename.endsWith(".png"))){
                    System.out.print("\nFile must be a jpg or png file\n");
                }else if(!canAddPhoto(esql, filename)){
                  break;
                }else{
                  System.out.print("\nEnter title (optional): ");
                  title = input.nextLine();
                  System.out.print("\nEnter caption (optional): ");
                  caption = input.nextLine();
                  title = title.replaceAll("'","''"); //avoid unterminated string literal error
                  caption = caption.replaceAll("'","''");
                  addPhoto(esql, user_id, filename, title, caption); 
                }
              
                
                break;
              case 2: 
                System.out.print("\nEnter photo ID: \n");
                str = input.nextLine();
                if(!isNumeric(str)){
                  System.out.print("\nPhoto ID must be a number\n");
                }else {
                   photo_id = Integer.parseInt(str);
                   viewPhoto(esql, photo_id); 
                }
                break;
              case 3: 
                System.out.print("\nEnter photo ID: ");
                str = input.nextLine();
                if(!isNumeric(str)){
                  System.out.print("\nPhoto ID must be a number\n");
                }else {
                  photo_id = Integer.parseInt(str);
                  if(doesPhotoExist(esql, photo_id) == 1){
                     System.out.print("\nSave file as: ");
                     save_as = input.nextLine();
                     if(!(save_as.endsWith(".jpg") ^ save_as.endsWith(".png"))){
                        System.out.print("\nFile must be saved as jpg or png\n");
                      }else{
                        downloadPhoto(esql, photo_id, save_as);
                        System.out.print("\nPhoto downloaded! Please check the directory where you ran the java executable\n");
                      }
                  }else{
                    System.out.print("\nPhoto ID does not exist\n");
                  }
                }
                break;
              
              case 4:
                
                System.out.print("\nEnter photo ID: ");
                str = input.nextLine();
                if(!isNumeric(str)){
                    System.out.print("\nPhoto ID must be a number\n");
                  }else {
                    photo_id = Integer.parseInt(str);
                    val = isUsersPhoto(esql, currID, photo_id);
                     if (val== -1) {
                        System.out.println("\nPhoto ID does not exist");
                      }else if (val == -2){
                        System.out.println("\nYou may not add photo tags to a photo you did not upload");
                      }else{
                      
                        ArrayList<String> tags = new ArrayList<String>();
                        System.out.println("\nEnter tag(s): ");
                        str = input.nextLine();
                        if(str.trim().length() > 0){

                          while(str.trim().length() > 0) {
                          
                          tags.add(str);
                          str = input.nextLine();
                          
                          }
                          addTags(esql, photo_id, tags); 
                        }
                        else{
                          System.out.print("No tags were added\n");
                        }
                    }
                  }
                break;
                
              case 5:
                System.out.print("\nEnter photo ID: ");
                str = input.nextLine();
                if(!isNumeric(str)){
                    System.out.print("\nPhoto ID must be a number\n");
                  }else {
                    photo_id = Integer.parseInt(str);
                    val = isUsersPhoto(esql, currID, photo_id);
                     if (val== -1) {
                        System.out.println("\nPhoto ID does not exist");
                      }else if (val == -2){
                        System.out.println("\nYou may not tag users to a photo you did not upload");
                      }else{
                      
                        ArrayList<String> utags = new ArrayList<String>();
                        System.out.println("\nEnter username(s): ");
                        str = input.nextLine();
                        if(str.trim().length() > 0){

                          while(str.trim().length() > 0) {
                          
                          utags.add(str);
                          str = input.nextLine();
                          
                          }
                          addUserTags(esql, photo_id, utags); 
                        }
                        else{
                          System.out.print("No users were tagged\n");
                        }
                    }
                  }
                break;
      			  
              case 6: 
                System.out.print("\nEnter Photo ID: "); 
                str = input.nextLine();
                if(!isNumeric(str)){
                  System.out.print("\nPhoto ID must be a number\n");
                }else{
                    photo_id = Integer.parseInt(str);
                  if(doesPhotoExist(esql, photo_id) == 1){
                    addLike(esql, photo_id); 
                    System.out.print("\nLike added!\n");
                  }else {
                    System.out.print("\nPhoto ID does not exist\n");
                  }
                }
                break;
              case 7: 
                System.out.print("\nEnter Photo ID: "); 
                str = input.nextLine();
                if(!isNumeric(str)){
                  System.out.print("\nPhoto ID must be a number\n");
                }else{
                    photo_id = Integer.parseInt(str);
                  if(doesPhotoExist(esql, photo_id) == 1){
                    addDislike(esql, photo_id); 
                    System.out.print("\nDislike added!\n");
                  }else {
                    System.out.print("\nPhoto ID does not exist\n");
                  }
                }
                break;
      			  case 8: 
                System.out.print("\nAdd a user to follow: ");
                user_id = currID;
                username = input.nextLine();
                if(username.equals(currUser)) {
                  System.out.println("\nYou cannot follow yourself");
                 
                }else{
                
                  following_id = username2id(esql, username);
                  if(following_id == -1){
                    System.out.println("\nUsername " + "\"" + username + "\"" + " does not exist");
                    break;
                  }else{
                    if(addFollows(esql, user_id, following_id)) {
                      System.out.print("\nYou are now following " + username + "!\n");
                    }
                  }
                }
                break;
      			  case 9: 
                System.out.print("\nEnter photo ID: ");
                str = input.nextLine();
                if(!isNumeric(str)){
                  System.out.print("\nPhoto ID must be a number\n");
                }else{
                  photo_id = Integer.parseInt(str);
                  user_id = currID;
                  if(doesPhotoExist(esql, photo_id) == 1){
                    System.out.print("\nEnter comment: ");
                    comment = input.nextLine();
                    if(comment.length() > 300) {
                      System.out.println("\nComment too long!");
                    }else{
                      addComments(esql, photo_id, user_id, comment);
                      System.out.println("\nComment added!");
                    }
                    
                  }else{
                    System.out.print("\nPhoto ID does not exist\n");
                  }                  
                }
                break;
      		    case 10:
                System.out.print("\nEnter tag: "); 
                tag = input.nextLine();
                searchUserOnTag(esql, tag);   
                break;
              case 11:
                System.out.print("\nEnter title: "); 
                title = input.nextLine();
                searchUserOnTitle(esql, title);   
                break;  		   
              case 12: 
                searchPhotoLikes(esql); 
                break;
      	  	  case 13: 
                searchPhotoDislikes(esql); 
                break;
      			  case 14: 
                System.out.print("\nEnter date (yyyy-mm-dd): ");   
                day = input.nextLine();
                if(isValidDate(day)) {
                   searchPhotoDate(esql, day);
                 }else{
                  System.out.print("\nDate must be entered in yyyy-mm-dd format. Please check your formatting.\n");
                 }
                
                break;
      			  case 15: 
                System.out.print("\nEnter username: "); 
                username = input.nextLine();
                if(username2id(esql, username) == -1){
                  System.out.print("\nUsername " + "\"" + username + "\"" + " does not exist\n");
                }else{
                  searchPhotoUser(esql, username);
                }         
                break;
              case 16: 
                System.out.print("\nEnter tag name: "); 
                tag = input.nextLine();
                searchPhotoTag(esql, tag); 
                break;
              case 17:
                searchPopularUsers(esql);
                break;
              case 18: 
                System.out.print("\nDisplaying News Feed...\n");
                user_id = currID;
                newsFeed(esql, user_id); 
                break;
              case 19: 
                System.out.print("\nDiscover photos: sorted by date, then alphabetically by title\n");
                user_id = currID;
                discover(esql, user_id); 
                break;
              case 20: 
                System.out.print("\nMy profile\n");
                myProfile(esql, currID); 
                break;
              case 21: 
                System.out.print("\nLogging out...\n");
                login = true;
                keepon = false;
                dontExit = true;
                break;
      			  case 22: 
                keepon = false;
                dontExit = false; 
                login = false;
                break;
    			    default : System.out.println("Unrecognized choice!\n"); break;
            }//end switch
          }//end while
      }
        
  	}
  	catch(Exception e){

  	}
  }

   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("\nPlease make a selection: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

}

