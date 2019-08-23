public class PGNLine {
	
	
	HalfMove[] currentLine;
	boolean[] openLevel;
	
	StringBuilder commentOfLine  = new StringBuilder();
	
	public PGNLine(int maxColumn, boolean[] openLevel) {
		this.currentLine = new HalfMove[maxColumn];
		this.openLevel = openLevel;
	}

	public HalfMove[] getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(HalfMove[] currentLine) {
		this.currentLine = currentLine;
	}

	public StringBuilder getCommentOfLine() {
		return commentOfLine;
	}

	public void setCommentOfLine(StringBuilder commentOfLine) {
		this.commentOfLine = commentOfLine;
	}

}
