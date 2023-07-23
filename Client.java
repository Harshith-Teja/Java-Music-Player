import java.io.*;
import java.net.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

import java.util.*;
import java.util.Timer;

import javax.sound.sampled.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

public class ChatClient extends JFrame implements ActionListener, ListSelectionListener {
    String uname, selectedUserListValue;
    PrintWriter pw;
    BufferedReader br;
    public JTextArea taMessages;
    JList<String> usersList;
    DefaultListModel<String> listModel;

    JTextField tfInput;
    JButton btnSend, btnExit;
    Socket client;



    JLabel musicPlayer,musicTable,musicClients,songName,selectASong;
    JButton play,pause,previous,next;
    JButton openDirectory;
    JPanel mainPanel,mplayerPanel,mplayerPanelUp,mplayerPanelCenter,mplayerPanelDown, mTablePanel, clientDetailsPanel;
    Border greenBorder, blueBorder, blackBorder;
    JProgressBar songBar;
    Timer timer;//pgbar timer
    TimerTask task;//pgbar timer task

    //song related declarations
    File file;
    AudioInputStream audioStream;
    Clip clip;

    //prv, nxt song
    File directory;
    File[] filesInTheDirectory;
    ArrayList<File> songs;
    int songNumber; //tracks song number

    File song;
    JFileChooser j;


    public ChatClient(String uname, String servername) throws Exception
    {
        super("Connected as: " + uname);  // set title for the JFrame

        client = new Socket(InetAddress.getByName(servername),4444); //requests for a connection
        this.uname = uname;
        br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        pw = new PrintWriter(client.getOutputStream(), true);
        pw.println(uname);  // send name to server


        //bring up the chat interface
        buildInterface();

        pw.println("playlist");
        pw.println(songs.size());
        for (File value : songs)
            pw.println(value);


        new MessagesThread().start();  // create thread that listens for messages
    }

    public void buildInterface() {


        //GUI

        //retrieving songs from the user
        songs = new ArrayList<File>();
        directory = new File("D:\\Songs(.wav files)");
        filesInTheDirectory = directory.listFiles();
        if(filesInTheDirectory != null) //adding all the user's song to songs arraylist
        {
            for(File files: filesInTheDirectory)
                songs.add(files);
           // songs.addAll(Arrays.asList(filesInTheDirectory));
        }

        //Panels and Labels
        //music player panel

        musicPlayer = new JLabel("MUSIC PLAYER",SwingConstants.CENTER); //MUSIC PLAYER label
        musicPlayer.setForeground(Color.GREEN);
        musicPlayer.setFont(new Font(null,Font.BOLD,30));

        mplayerPanelUp = new JPanel(new BorderLayout()); //top panel in music player panel
        mplayerPanelUp.setBackground(Color.BLACK);
        mplayerPanelUp.add(musicPlayer,"North");
        add(mplayerPanelUp);

        //label and progress bar for center panel
        songName = new JLabel("Song Name",SwingConstants.CENTER); //song name label
        songName.setForeground(Color.CYAN);
        songName.setFont(new Font(null,Font.BOLD,30));

        songBar = new JProgressBar(); //progress bar
        songBar.setValue(0);
        songBar.setBackground(Color.GREEN);
        songBar.setForeground(Color.LIGHT_GRAY);
        songBar.setStringPainted(true);

        mplayerPanelCenter = new JPanel(); //center panel in music player panel
        mplayerPanelCenter.setLayout(new BoxLayout(mplayerPanelCenter,BoxLayout.X_AXIS));
        mplayerPanelCenter.setBackground(Color.BLACK);
        mplayerPanelCenter.add(Box.createHorizontalGlue());
        mplayerPanelCenter.add(songName);
        mplayerPanelCenter.add(Box.createHorizontalGlue());
        mplayerPanelCenter.add(songBar);
        mplayerPanelCenter.add(Box.createHorizontalGlue());


        //Buttons for bottom of music player panel

        play = new JButton("play");
        pause = new JButton("pause");
        previous = new JButton("previous");
        next = new JButton("Next");

        mplayerPanelDown = new JPanel(); //bottom panel in music player panel
        mplayerPanelDown.add(previous);
        mplayerPanelDown.add(play);
        mplayerPanelDown.add(pause);
        mplayerPanelDown.add(next);

        mplayerPanelDown.setLayout(new GridLayout(1,4)); //all buttons get displayed in a single row

        play.addActionListener(this); //adding functionalities to the button
        pause.addActionListener(this);
        previous.addActionListener(this);
        next.addActionListener(this);


        greenBorder = BorderFactory.createLineBorder(Color.GREEN,5);

        mplayerPanel = new JPanel(new BorderLayout()); //main music player panel
        mplayerPanel.setBounds(10,10,500,500);
        mplayerPanel.setBorder(greenBorder);
        mplayerPanel.add(mplayerPanelUp,BorderLayout.NORTH);
        mplayerPanel.add(mplayerPanelDown,BorderLayout.SOUTH);
        mplayerPanel.add(mplayerPanelCenter,BorderLayout.CENTER);
        add(mplayerPanel);


        //Music Table Panel
        musicTable = new JLabel("MUSIC TABLE",SwingConstants.CENTER); //MUSIC TABLE label
        musicTable.setForeground(Color.BLUE);
        musicTable.setFont(new Font(null,Font.BOLD,30));

        openDirectory = new JButton("Open Songs List"); //open directory button
        openDirectory.addActionListener(this);

        selectASong = new JLabel("Select a song you would like to play",SwingConstants.CENTER); //select a song label
        selectASong.setForeground(Color.BLUE);
        selectASong.setFont(new Font(null,Font.BOLD,25));

        blueBorder = BorderFactory.createLineBorder(Color.BLUE,5);

        mTablePanel = new JPanel(new BorderLayout()); //Music Table panel
        mTablePanel.setBounds(530,10,500,500);
        mTablePanel.setBackground(Color.yellow);
        mTablePanel.setBorder(blueBorder);
        mTablePanel.add(musicTable,BorderLayout.NORTH);
        mTablePanel.add(selectASong,BorderLayout.CENTER);
        mTablePanel.add(openDirectory,BorderLayout.SOUTH);
        add(mTablePanel);


        //Music Clients Panel
        musicClients = new JLabel("MUSIC CLIENTS",SwingConstants.CENTER); //MUSIC CLIENTS label
        musicClients.setFont(new Font(null,Font.BOLD,30));

        blackBorder = BorderFactory.createLineBorder(Color.BLACK,5);

        clientDetailsPanel = new JPanel(new BorderLayout()); //Main client details panel
        clientDetailsPanel.setBounds(1050,10,450,500);
        clientDetailsPanel.setBackground(Color.CYAN);
        clientDetailsPanel.setBorder(blackBorder);
        clientDetailsPanel.add(musicClients,"North");


        //Chat application GUI - adding it to the clientDetailsPanel
        btnSend = new JButton("Send");
        btnExit = new JButton("Exit");

        //chat area
        taMessages = new JTextArea();
        taMessages.setRows(25);//28
        taMessages.setColumns(29);//33
        taMessages.setEditable(false);

        //online users list
        usersList = new JList<>();
        listModel = new DefaultListModel<>();
        usersList.setModel(listModel);


        //Scroll panel (chat area and online users list)
        JScrollPane chatPanel = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane onlineUsersPanel = new JScrollPane(usersList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        //top panel (chatPanel, onlineUsersPanel)
        JSplitPane tp = new JSplitPane();
        tp.setBackground(Color.CYAN);
        tp.setLeftComponent(onlineUsersPanel);
        tp.setRightComponent(chatPanel);
        clientDetailsPanel.add(tp, "Center");

        tfInput = new JTextField(30); //user input field

        //bottom panel (input field, send and exit)
        JPanel bp = new JPanel(new FlowLayout());
        bp.setBackground(Color.CYAN);
        bp.add(tfInput);
        bp.add(btnSend);
        bp.add(btnExit);
        clientDetailsPanel.add(bp, "South");


        btnSend.addActionListener(this);
        tfInput.addActionListener(this);//allow user to press Enter key in order to send message
        btnExit.addActionListener(this);

        usersList.addListSelectionListener(this); //allows users to click on any value on JList

        add(clientDetailsPanel);

        setLayout(null);
        setTitle("MUSIC PLAYER");
        setSize(1550,800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == btnExit) {
            pw.println("!end");  // send end to server so that server know about the termination
            System.exit(0);
        }
        else if(evt.getSource() == play)
        {
           clip.start(); //song starts playing
        }
        else if(evt.getSource() == pause)
        {
          clip.stop(); //song stops playing
        }
        else if(evt.getSource() == previous) //navigating to previous song
        {
            if(songNumber > 0)
            {
                songNumber--;
                clip.stop();
                try
                {
                    audioStream = AudioSystem.getAudioInputStream(songs.get(songNumber));
                    clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.start();
                    songName.setText(songs.get(songNumber).getName());
                }catch(Exception e)
                {
                    out.println(e);
                }

            }
            else
            {
                songNumber = songs.size() - 1;
                clip.stop();
                try
                {
                    audioStream = AudioSystem.getAudioInputStream(songs.get(songNumber));
                    clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.start();
                    songName.setText(songs.get(songNumber).getName());
                }catch(Exception e)
                {
                    out.println(e);
                }
            }

        }
        else if(evt.getSource() == next) //navigating to next song
        {
            if(songNumber < songs.size() - 1)
            {
                songNumber++;
                clip.stop();
                try
                {
                    audioStream = AudioSystem.getAudioInputStream(songs.get(songNumber));
                    clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.start();
                    songName.setText(songs.get(songNumber).getName());
                }catch(Exception e)
                {
                    out.println(e);
                }

            }
            else
            {
                songNumber = 0;
                clip.stop();
                try
                {
                    audioStream = AudioSystem.getAudioInputStream(songs.get(songNumber));
                    clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.start();
                    songName.setText(songs.get(songNumber).getName());
                }catch(Exception e)
                {
                    out.println(e);
                }
            }
        }
        else if(evt.getSource() == openDirectory) //pops up select a song directory
        {
            j = new JFileChooser("D:\\Songs(.wav files)" , FileSystemView.getFileSystemView());
            j.showDialog(mTablePanel,"Play the song");

            song = j.getSelectedFile();
            songNumber = songs.indexOf(j.getSelectedFile());
            songName.setText(j.getSelectedFile().getName());

            try{
                file = new File(String.valueOf(song));
                audioStream = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

                beginTimer();
            }catch(Exception e)
            {
                out.println(e);
            }

        }
        else if(selectedUserListValue.equals("Group Chat")) //if grp chat is selected, it sends a msg to the server, to activate grp chat
        {
            pw.println(tfInput.getText());
        }
        else{
            // sends private message to server
            initiatePrivateChat(selectedUserListValue);
            pw.println(tfInput.getText());
        }
    }


    public static void main(String[] args) {

        // take username from user
        String name = JOptionPane.showInputDialog(null, "Enter your name: ", "Username", JOptionPane.PLAIN_MESSAGE);
       // String servername = "localhost";
        String servername = "172.22.62.147";
        try {
            new ChatClient(name, servername);
        } catch (Exception ex) {
            out.println("Unable to connect to server.\nError: " + ex.getMessage());
        }

    } // end of main


    public void beginTimer() //begins timer in the progress bar
    {
        timer = new Timer();
        songBar.setValue(100);
        task = new TimerTask() {

            public void run() {
             try //in case of division by zero
             {
                 songBar.setValue((int)(100*clip.getMicrosecondPosition()/clip.getMicrosecondLength()));
                 songBar.setString((int)(clip.getMicrosecondPosition()/(1e6*60))+":"+(int)((clip.getMicrosecondPosition()/1e6)%60)+" / "+(int)(clip.getMicrosecondLength()/(1e6*60))+":"+(int)((clip.getMicrosecondLength()/1e6)%60));
             }catch(ArithmeticException e)
             {
                 out.println(e);
             }

            }

        };
        timer.scheduleAtFixedRate(task,1000,1000);

    }

    public void initiatePrivateChat(String str) //initiate private chat
    {
        int temp = str.length()-9; //getSelectedValue returns name with percentage
        str = str.substring(0,temp); //converting 'Kailash : 88.88%' to 'Kailash'

        pw.println("pvtMsg"); //indication that msgs are private
        pw.println(str);

    }

    @Override
    public void valueChanged(ListSelectionEvent e) { //JList selected value

            if(usersList.isSelectionEmpty())
                selectedUserListValue = "Group Chat";
            else
                selectedUserListValue = usersList.getSelectedValue();

    }


    // inner class for Messages Thread
    class MessagesThread extends Thread {

        @Override
        public void run() {
            String line;
            int usersSize;
            try {

                while(true) {
                    line = br.readLine();
                    if(line.equals("Users List")) //if the incoming data is users list, all the users data must be displayed only on userlist textarea
                    {
                        listModel.removeAllElements(); //clears the previous list

                        usersSize = Integer.parseInt(br.readLine()); //getting usersSize
                        for(int i=0 ; i<usersSize ; i++)
                        {
                            line = br.readLine(); //getting the name of each user
                            listModel.addElement(line);   //adding names to the JList

                        }
                        listModel.addElement("Group Chat"); //adding Group Chat option at the end of the JList

                    }
                    else {
                        taMessages.append(line + "\n");
                        taMessages.setCaretPosition(taMessages.getDocument().getLength());//auto scroll to last message
                    }

                } // end of while
            } catch (Exception ex) {
                out.println("nconnected"+ex);
            }
        }
    }
} //  end of client
