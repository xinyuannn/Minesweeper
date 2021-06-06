import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel{
	
	private final int numOfMines = 40;
    private final int numOfRows = 16;
    private final int numOfCols = 16;
	
	private int[] board;
	private Image[] img;
	private int surroundings[] = {-1, 1, -numOfCols, numOfCols, -numOfCols-1, numOfCols-1, -numOfCols+1, numOfCols+1};
	private JLabel status;
	private JLabel timeBar;
	private int minesLeft;
	private boolean playing;
	private Timer timer;
	private int timeLeft;
	ObjectOutputStream toServer = null;
	ObjectInputStream fromServer = null;
	Socket socket = null;
	
	public Board(JLabel status, JLabel timeBar) {
		this.status = status;
		this.timeBar = timeBar;
		addMouseListener(new MinesAdapter());
		initGame();
		initBoard();
	}
	
	private void initBoard() {
        setPreferredSize(new Dimension(numOfCols * 15 + 1, numOfRows * 15 + 1));
        img = new Image[13];
        for (int i = 0; i < 13; i++) {
            img[i] = (new ImageIcon("minesweepertiles/" + i + ".png")).getImage();
        }
    }
	
	@Override
	public void paintComponent(Graphics g) {
		int minesUncovered = 0;
		
		for (int i = 0; i < numOfRows; i++) {
			for (int j = 0; j < numOfCols; j++) {
				int cur = i * numOfCols + j;
				if (playing && board[cur] == 9) playing = false;
				if (!playing) {
					if (board[cur] > 19 && board[cur] != 29) board[cur] = -1;
					else if (board[cur] == 19) board[cur] = 9;
				}
				else if (board[cur] > 9 && board[cur] < 20) minesUncovered++;
				
				if (board[cur] == -1) g.drawImage(img[12], (j * 15), (i * 15), this);
				else if (board[cur] > 19) g.drawImage(img[11], (j * 15), (i * 15), this);
				else if (board[cur] > 9) g.drawImage(img[10], (j * 15), (i * 15), this);
				else g.drawImage(img[board[cur]], (j * 15), (i * 15), this);
			}
		}
		status.setText(Integer.toString(minesLeft));
		
		if (minesUncovered == 0 && playing) {
			playing = false;
			timer.stop();
			status.setText("Game win");
			try {
				socket = new Socket("localhost", 8000);
				toServer = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			JFrame frame = new JFrame("Message");
			String name = (String)JOptionPane.showInputDialog(frame,
					"Please enter your username to save the score:",
					"score",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					"Default User");
			try {
				toServer.writeObject(new Statement(name, timeLeft, "insertScoreByName"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if (!playing) {
			timer.stop();
			status.setText("Game lost");
		}
	}
	
	public void initGame() {
		if (playing) timer.stop();
		addTimer(1000, 1000);
		Random random = new Random();
		playing = true;
		board = new int[numOfRows * numOfCols];
		minesLeft = numOfMines;
		
		status.setText(Integer.toString(minesLeft));
		
		for (int i = 0; i < board.length; i++) board[i] = 10;
		
		int i = 0;
		while (i < numOfMines) {
			int pos = random.nextInt(numOfRows * numOfCols);
			if (board[pos] != 19) {
				board[pos] = 19;
				i++;
				for (int j : surroundings) {
					if ((pos + j) >= 0 && (pos + j < numOfRows * numOfCols) && (board[pos + j] != 19)) {
						if (!((j == 1 || j == -numOfCols+1 || j == numOfCols+1) && pos % 16 == 15) && !((j == -1 || j == -numOfCols-1 || j == numOfCols-1) && pos % 16 == 0)) board[pos + j] += 1;
					}
				}
			}
		}
	}
	
	public void saveGame() {
		try {
			socket = new Socket("localhost", 8000);
			toServer = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JFrame frame = new JFrame("Message");
		if (!playing) {
			JOptionPane.showMessageDialog(frame, "You cannot save a finished game!", "Save", JOptionPane.PLAIN_MESSAGE);
		} else {
			String name = (String)JOptionPane.showInputDialog(frame,
					"Please enter your username:",
					"Save",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					"Default User");
			String gameString = IntStream.of(board)
					.mapToObj(Integer::toString)
					.collect(Collectors.joining(","));
			try {
				toServer.writeObject(new Statement(name, gameString, timeLeft, minesLeft, "saveGameByName"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void loadGame() {
		try {
			socket = new Socket("localhost", 8000);
			toServer = new ObjectOutputStream(socket.getOutputStream());
			fromServer = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JFrame frame = new JFrame("Message");
		Statement s = null;
		String name = (String)JOptionPane.showInputDialog(frame,
				"Please enter your username:",
				"Load",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				"Default User");
		
		try {
			toServer.writeObject(new Statement(name, "loadGameByName"));
			try {
				s = (Statement) fromServer.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] game = s.getGame().split(",");
		timeLeft = s.gettimeLeft();
		minesLeft = s.getminesLeft();
		for (int i = 0; i < board.length; i++) {
			board[i] = Integer.parseInt(game[i]);
		}
		playing = true;
		timer.stop();
		addTimer(1000, timeLeft);
		repaint();
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getScores() {
		try {
			socket = new Socket("localhost", 8000);
			toServer = new ObjectOutputStream(socket.getOutputStream());
			fromServer = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JFrame frame = new JFrame("Message");
		Statement s = null;
		String name = (String)JOptionPane.showInputDialog(frame,
				"Please enter your username:",
				"Score",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				"Default User");
		
		try {
			toServer.writeObject(new Statement(name, "queryScoreByName"));
			try {
				s = (Statement) fromServer.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String scores = s.getScores();
		JOptionPane.showMessageDialog(frame, scores, "Scores", JOptionPane.PLAIN_MESSAGE);
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void expand(int cur) {
		for (int i : surroundings) {
			if (cur + i >= 0 && cur + i < numOfRows * numOfCols && board[cur + i] > 9 && !((i == 1 || i == -numOfCols+1 || i == numOfCols+1) && cur % 16 == 15) && !((i == -1 || i == -numOfCols-1 || i == numOfCols-1) && cur % 16 == 0)) {
				while (board[cur + i] > 9) board[cur + i] -= 10;
				if (board[cur + i] == 0) expand(cur + i);
			}
		}
	}
	
	private void addTimer(int t, long last){
		long startTime = System.nanoTime();
		timer = new Timer(t, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                long endTime = startTime + TimeUnit.SECONDS.toNanos(last);
                long time = System.nanoTime();

                if (time < endTime) {
                    timeLeft = (int) TimeUnit.NANOSECONDS.toSeconds((endTime - time));
                    timeBar.setText(Long.toString(timeLeft));
                } else {
                    ((Timer) e.getSource()).stop();
                    playing = false;
                    repaint();
                }
                revalidate();
                repaint();
            }
        });
        timer.setInitialDelay(0);
        timer.start();
	}
	
	private class MinesAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
        	
        	boolean changed = false;
        	
            int x = e.getX();
            int y = e.getY();

            int curCol = x / 15;
            int curRow = y / 15;

            if (!playing) {
            	initGame();
                repaint();
            }

            if ((x < numOfCols * 15) && (y < numOfRows * 15)) {

                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (board[curRow * numOfCols + curCol] > 9) {
                        changed = true;
                        if (board[curRow * numOfCols + curCol] <= 19) {
                            if (minesLeft > 0) {
                                board[(curRow * numOfCols) + curCol] += 10;
                                minesLeft--;
                                status.setText(Integer.toString(minesLeft));
                            } else {
                                status.setText("No marks left");
                            }
                        } else {
                            board[curRow * numOfCols + curCol] -= 10;
                            minesLeft++;
                            status.setText(Integer.toString(minesLeft));
                        }
                    }
                } else {
                    if ((board[curRow * numOfCols + curCol] > 9) && (board[curRow * numOfCols + curCol] < 20)) {
                    	changed = true;
                        board[curRow * numOfCols + curCol] -= 10;

                        if (board[curRow * numOfCols + curCol] == 9) {
                            playing = false;
                        }

                        if (board[curRow * numOfCols + curCol] == 0) {
                        	expand(curRow * numOfCols + curCol);
                        }
                    }
                }
                if (changed) {
                    repaint();
                }
            }
        }
    }
}
