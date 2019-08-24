import java.util.ArrayList;

public class HalfMove {
	
	private int number;
	
	private boolean white;
	
	private String halfMove;
	
	private int level;
	
	/**
	 * comments after a move like + # !! ?? !? ?! ! ?
	 */
	private String attribute = "";
	
	private String comment = "";
	
	boolean lastOfLevel = false;
	
	

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

		// line feed ?
		if (((pervious == null) || (pervious.getNumber() != this.getNumber()) || (pervious.level != this.level))) {
			output.append("\n");
			// number always here..
			output.append(String.format("%5s", number + ". "));
		}

		// indentation
		for (int i = 0; i < (3 * level); i++) {
			output.append(String.format("%5s", "  "));
		}

		output.append(String.format("%5s", halfMove));

		output.append(comment);

		return output.toString();
	}

	/**
	 * @return
	 */
	public int getColumn() {
		return (this.level*2) + (this.isWhite() ? 0 : 1);
	}
	
	public int getHalfMoveNumber() {
		return ((this.number-1) * 2) + (this.isWhite() ? 0 : 1);
	}
	


	@Override
	public String toString() {
		return " halfMove=" + halfMove + ", level=" + level	+ " pos " + getColumn();
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	/**
	 * Splits comments 
	 * @param maxLenght
	 * @return
	 */
	public ArrayList<String> getStructuredComments(int maxLenght) {

		String[] commentToken = this.comment.replace("{", " ").replace("}", " ").split(" ");

		StringBuilder commentLine = new StringBuilder();
		ArrayList<String> all = new ArrayList<String>();

		for (int i = 0; i < commentToken.length; i++) {
			String word = commentToken[i];

			if (!word.isEmpty()) {
				if ((commentLine.length() + word.length() + 1) > maxLenght) {
					all.add(commentLine.toString());
					commentLine.setLength(0);
				}
				commentLine.append(word);
				commentLine.append(" ");
			}
		}
		// last line
		all.add(commentLine.toString());
		
		return all;
	}

	public boolean isLastOfLevel() {
		return lastOfLevel;
	}

	public void setLastOfLevel(boolean lastOfLevel) {
		this.lastOfLevel = lastOfLevel;
	}
}


