
public class PGNLine {
	
	
	HalfMove[] currentLine;
	boolean[] openColums;
	
	StringBuilder commentOfLine  = new StringBuilder();
	
	public PGNLine(int maxColumn, boolean[] openColums) {
		this.currentLine = new HalfMove[maxColumn];
		this.openColums = openColums;
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
