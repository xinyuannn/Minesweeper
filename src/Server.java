
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.*;

public class Server implements Runnable {

  // Number a client
  private int clientNo = 0;
  private Connection conn;
  private PreparedStatement saveGameByName, loadGameByName, insertScoreByName, queryScoreByName;
  private Statement s;
  
  public Server() {
	  try {
			conn = DriverManager.getConnection("jdbc:sqlite:minesweeper.db");
			saveGameByName = conn.prepareStatement("INSERT OR REPLACE INTO games (name, game, timeleft, minesleft) VALUES (?, ?, ?, ?)");
			loadGameByName = conn.prepareStatement("Select game, timeleft, minesleft from games WHERE name = ?");
			insertScoreByName = conn.prepareStatement("Insert Into scores (name, score) Values (?, ?)");
			queryScoreByName = conn.prepareStatement("Select score from scores WHERE name = ? ORDER BY score DESC LIMIT 5");
			
		} catch (SQLException e) {
			System.err.println("Connection error: " + e);
			System.exit(1);
	  }
	  Thread t = new Thread(this);
	  t.start();
  }

  public void run() {
	  try {
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(8000);
    
        while (true) {
          // Listen for a new connection request
          Socket socket = serverSocket.accept();
    
          // Increment clientNo
          clientNo++;
          
          // Create and start a new thread for the connection
          new Thread(new HandleAClient(socket, clientNo)).start();
        }
      }
      catch(IOException ex) {
        System.err.println(ex);
      }
	    
  }
  
  // Define the thread class for handling new connection
  class HandleAClient implements Runnable {
    private Socket socket; // A connected socket
    private int clientNum;
    
    /** Construct a thread */
    public HandleAClient(Socket socket, int clientNum) {
      this.socket = socket;
      this.clientNum = clientNum;
    }

    /** Run a thread */
    public void run() {
      try {
        // Create data input and output streams
        ObjectInputStream inputFromClient = new ObjectInputStream(
          socket.getInputStream());
        ObjectOutputStream outputToClient = new ObjectOutputStream(
          socket.getOutputStream());

        // Continuously serve the client
        while (true) {
          try {
			s = (Statement) inputFromClient.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          	if (s.getType().equals("saveGameByName")) {
          		try {
          			PreparedStatement stmt = saveGameByName;
          			stmt.setString(1, s.getName());
    				stmt.setString(2, s.getGame());
    				stmt.setInt(3, s.gettimeLeft());
    				stmt.setInt(4, s.getminesLeft());
					stmt.executeUpdate();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
          	} else if (s.getType().equals("loadGameByName")) {
          		try {
          			PreparedStatement stmt = loadGameByName;
          			stmt.setString(1, s.getName());
          			ResultSet rset = stmt.executeQuery();
          			outputToClient.writeObject(new Statement(null, rset.getString("game"), rset.getInt("timeLeft"), rset.getInt("minesLeft"), null));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
          	} else if (s.getType().equals("insertScoreByName")) {
          		try {
          			PreparedStatement stmt = insertScoreByName;
          			stmt.setString(1, s.getName());
    				stmt.setInt(2, s.gettimeLeft());
					stmt.execute();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
          	} else if (s.getType().equals("queryScoreByName")) {
          		try {
          			PreparedStatement stmt = queryScoreByName;
          			stmt.setString(1, s.getName());
          			ResultSet rset = stmt.executeQuery();
          			int i = 1;
          			String scores = "";
        			while (rset.next()) {
        				scores += Integer.toString(i) + ": " + Integer.toString(rset.getInt("score")) + "\n";
        				i++;
        			}
          			outputToClient.writeObject(new Statement(scores));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
          	}
        }
      }
      catch(IOException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  /**
   * The main method is only needed for the IDE with limited
   * JavaFX support. Not needed for running from the command line.
   */
  public static void main(String[] args) {
    Server mts = new Server();
  }
}