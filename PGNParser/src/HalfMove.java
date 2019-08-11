
public class HalfMove {
	
	private int number;
	
	private boolean white;
	
	private String halfMove;
	
	private int level;
	
	private String comment = "";

	public HalfMove(int number, boolean whitheOrBlack, String halfMove, int level, String comment) {
		this.number = number;
		this.white = whitheOrBlack;
		this.halfMove = halfMove;
		this.level = level;
		this.comment = comment;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isWhite() {
		return white;
	}

	public void setWhite(boolean white) {
		this.white = white;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getHalfMove() {
		return halfMove;
	}

	public void setHalfMove(String halfMove) {
		this.halfMove = halfMove;
	}
	
	

	public String toString(HalfMove pervious) {

		StringBuilder output = new StringBuilder();
		boolean levelUp = false;
		boolean levelDown = false;
		boolean previousItemExists = false;
		
		
		if (pervious != null) {
			levelUp = pervious.getLevel() < this.level;
			levelDown = pervious.getLevel() > this.level;
		} 
		
		// line feed ?
		if (((pervious == null) || (pervious.getNumber() != this.getNumber()) || (pervious.level != this.level))){
			output.append("\n");
			// number always here..
			output.append(String.format("%5s", number + ". "));
		} else {
			previousItemExists = true;
		}
		

		// indentation
		
		for (int i = 0; i < (3 * level); i++) {
			output.append(String.format("%5s", "  "));
		}


		output.append(String.format("%5s", halfMove));

		output.append( comment );

		return output.toString();
	}

	public int getPosition() {
		return (this.level*2) + (this.isWhite() ? 0 : 1);
	}
	
	


	@Override
	public String toString() {
		return " halfMove=" + halfMove + ", level=" + level	+ " pos " + getPosition();
	}
}


