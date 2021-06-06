import java.io.Serializable;

public class Statement implements Serializable {
	private String type;
	private String name;
	private String game;
	private int timeLeft;
	private int minesLeft;
	private String scores;
	
	public Statement(String name, int timeLeft, String type) {
		this.name = name;
		this.timeLeft = timeLeft;
		this.type = type;
	}
	
	public Statement(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public Statement(String name, String game, int timeLeft, int minesLeft, String type) {
		this.name = name;
		this.game = game;
		this.timeLeft = timeLeft;
		this.minesLeft = minesLeft;
		this.type = type;
	}
	
	public Statement(String scores) {
		this.scores = scores;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public int gettimeLeft() {
		return this.timeLeft;
	}
	
	public String getGame() {
		return this.game;
	}
	
	public int getminesLeft() {
		return this.minesLeft;
	}
	
	public String getScores() {
		return this.scores;
	}
}
