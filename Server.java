import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import static java.lang.System.out;

public class ChatServer {
        Vector<String> users = new Vector<String>(); //users list
        Vector<HandleClient> clients = new Vector<HandleClient>(); //Connected clients
        String[][] playlist = new String[30][30]; //to store playlist's of users
        int[] playlistSizeArr = new int[30]; //to store each playlist size
        int index =0; //keeps track of index
  
        public void process() {
            try(ServerSocket server = new ServerSocket(4444)) { //waits for connection on specified port no
            out.println("Server Started...");
            while (true) {
            try{
                Socket client = server.accept(); //accept connection
                //add incoming client to connected clients vector.
                HandleClient c = new HandleClient(client);
                clients.add(c);
                }catch(Exception e)
                {
                out.println("can't accept");
                }
            
            } // end of while
            }catch(Exception e){
            out.println(e);
            }
        }
  
        public static void main(String... args){
              new ChatServer().process();
        } // end of main
  
        public void broadcast(String user, String message){ //broadcasts message to all connected users
            // send message to all connected users
            for (HandleClient c : clients) {
                  c.sendMessage(user, message);
                  c.updateOnlineUsers(c.name);
            }
        }
        /*
        * Inner class, it is responsible for handling incoming clients.
        * Each connected client will set its own thread.
        */
        class HandleClient extends Thread {
              String name = "",plist; // client name/username
              BufferedReader input; //gets input from client
              PrintWriter output; //sends output to client
          
              public HandleClient(Socket client) throws Exception {
                    // get input and output streams
                    input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    output = new PrintWriter(client.getOutputStream(), true);
                    // read name
                    name = input.readLine();
                    users.add(name); // adding unames to users vector
                    
                    plist = input.readLine();
                    if(plist.equals("playlist"))
                    getPlaylist();
                    broadcast(name, " Has connected!");
                    getOnlineUsers();
                    start();
              }
          
              public void sendMessage(String uname, String msg) { //sends grp message
                    output.println("[Grp Msg] " + uname + " : " + msg); //sending the grp msg to all clients
              }
          
              public void sendPvtMessage(String uname, String msg) { //sends private message
                    output.println("[PvtMsg] "+uname + " : " + msg); //sends the pvt msg to only targeted client
              }
              public void getOnlineUsers() { //get the list of online users
                    output.println("Users List"); //informing the client that server is sending users list
                    output.println(users.size()-1); //sending usersSize data
                    int skippedIndex = users.size()-1; //skipping user's index
                    for(int i=0 ; i<users.size()-1 ; i++)
                    output.println(users.get(i) + " : " + getOnlinePercentage(i,skippedIndex) + "%");
              }
              public void updateOnlineUsers(String uname) //update the list of online users
              {
                    output.println("Users List"); //informing the client that server is sending users list
                    output.println(users.size()-1); //sending usersSize data
                    int skippedIndex=0;
                    for(int i=0 ; i<users.size() ; i++)
                    {
                        if(users.get(i).equals(uname))
                        {
                        skippedIndex = i;
                        break;
                        }
                    }
                    for(int i=0 ; i<users.size() ; i++)
                    {
                        if(users.get(i).equals(uname))
                        {
                        continue; //user's own details are not needed
                        }
                        output.println(users.get(i) + " : " + getUpdatePercentage(i,skippedIndex) + "%");
                    }
              }
              public void getPlaylist() throws Exception //gets the playlist of the user
              {
                    String songs;
                    songs = input.readLine();
                    playlistSizeArr[index] = Integer.parseInt(songs); //songsSize[k]
                    while(index <users.size())
                    {
                      for(int j = 0; j< playlistSizeArr[index] ; j++)
                          playlist[index][j] = input.readLine();
                      
                      index++;
                    }
                    index--;
              }
              public String getOnlinePercentage(int i, int skippedIndex) //gets percentage match a user has with other user
              {
                  float percentage;
                  int count = 0;
                  int songsSize1 = playlistSizeArr[skippedIndex];
                  int songsSize2 = playlistSizeArr[i];
                  int m,p;
                  p = i;
                  m=skippedIndex;
                  for(int j=0 ; j<songsSize1 ; j++)
                  {
                    for(int n=0 ; n<songsSize2 ; n++)
                    {
                        if(playlist[m][j].equals(playlist[p][n])) //matching songs from each user
                              count++; //incrementing count if it matches
                    }
                  }
                  percentage = ((float)count/songsSize1)*100; //finding percentage according to main user
                  return String.format("%.2f",percentage); //returns roundup value of percentage
              }
              public String getUpdatePercentage(int i , int skippedIndex)
              {
                  float percentage;
                  int count = 0;
                  int songsSize1 = playlistSizeArr[skippedIndex];
                  int songsSize2 = playlistSizeArr[i];
                  int m,p;
                  p = i;
                  m=skippedIndex;
                  for(int j=0 ; j<songsSize1 ; j++)
                  {
                      for(int n=0 ; n<songsSize2 ; n++)
                      {
                          if(playlist[m][j].equals(playlist[p][n])) //matching songs from each user
                              count++; //incrementing count if it matches
                      }
                  }
                  percentage = ((float)count/songsSize1)*100; //finding percentage acc to main user
                  return String.format("%.2f",percentage);
              }
              public void run() { //starts the thread
                  String line;
                  try {
                      while (true) {
                          line = input.readLine();
                          if (line.equals("!end")) {
                              //notify all for user disconnection
                              broadcast(name, " Has disconnected!");
                              clients.remove(this);
                              users.remove(name);
                              break;
                          }
                          else if(line.equals("pvtMsg")) //if the msg is private
                          {
                              String targetUserName = input.readLine(); //reads name of that user to initiate
                              private chat
                              String msgToBeSent = "";
                              for(HandleClient c : clients)
                              {
                                  if(c.name.equals(targetUserName)) //if the name matches with any client name .equals(line)
                                  {
                                    msgToBeSent = input.readLine(); //the msg which needs to be sent to
                                    targeted user
                                    c.sendPvtMessage(name,msgToBeSent); //sends pvt msg only to that user
                                    break;
                                  }
                              }
                              String senderUserName = name;
                              for(HandleClient c : clients)
                              {
                                  if(c.name.equals(senderUserName)) //if the name matches with any client name .equals(line)
                                  {
                                    c.sendPvtMessage("You to "+ targetUserName,msgToBeSent); //sends what
                                    msg the user has sent to other's (user's own msg to user)
                                    break;
                                  }
                              }
                          }
                          else {
                                  broadcast(name, line); // method of outer class - send messages to all
                          }
                      } // end of while
                  } // try
                  catch (Exception ex) {
                        System.out.println(ex.getMessage());
                  }
              } // end of run()
        } // end of inner class
} // end of Server
