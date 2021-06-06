import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

public class Minesweeper extends JFrame{
	private JLabel status;
	private JLabel time;
	private Board board;
	
	public Minesweeper() {
		status = new JLabel("");
		time = new JLabel("", SwingConstants.CENTER);
		createMenus();
		add(time, BorderLayout.NORTH);
        add(status, BorderLayout.SOUTH);
        board = new Board(status, time);
        add(board);

        setResizable(false);
        pack();

        setTitle("Minesweeper");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setVisible(true);
	}
	
	private void createMenus() {
		JMenuItem newItem = new JMenuItem("New");
		JMenuItem openItem = new JMenuItem("Open");
		JMenuItem saveItem = new JMenuItem("Save");
		JMenuItem scoreItem = new JMenuItem("Scores");
		JMenuItem exitItem = new JMenuItem("Exit");
		JMenuBar menuBar = new JMenuBar();
		JMenu menus = new JMenu("File");
		
		newItem.addActionListener((e) -> board.initGame());
		openItem.addActionListener((e) -> board.loadGame());
		saveItem.addActionListener((e) -> board.saveGame());
		scoreItem.addActionListener((e) -> board.getScores());
		exitItem.addActionListener((e) -> System.exit(0));
		
		setJMenuBar(menuBar);
		menus.add(newItem);
		menus.add(openItem);
		menus.add(saveItem);
		menus.add(scoreItem);
		menus.add(exitItem);
		menuBar.add(menus);
	}
	
	public static void main(String[] args) {
		Minesweeper minesweeper = new Minesweeper();
    }
}
