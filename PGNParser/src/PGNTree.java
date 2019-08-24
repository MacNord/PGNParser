import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class PGNTree implements Comparable<PGNTree>{

	public PGNTree parent = null;

	/**
	 * all child nodes are put here, if the games continues with no variation this
	 * list contains still the one and only child element, all children are equally
	 * loved :)
	 */
	public LinkedList<PGNTree> children = new LinkedList<PGNTree>();
	
	private Integer number;
	
	private Boolean white;
	
	private String halfMove;
	
	private Integer level;
	
	private String attribute = "";
	
	private String comment = "";
	
	Boolean lastOfLevel = false;
	
	private PGNTree(Integer number, Boolean whitheOrBlack, String halfMove, Integer level, String comment) {
		this.number = number;
		this.white = whitheOrBlack;
		this.halfMove = halfMove;
		this.level = level;
		this.comment = comment;
	}
	
	public PGNTree createChildNode(Integer number, Boolean whitheOrBlack, String halfMove, Integer level,
			String comment) {
		

		PGNTree newNode = new PGNTree(number, whitheOrBlack, halfMove, level, comment);
		newNode.setParent(this);
		
		this.children.add(newNode);

		return newNode;

	}
	
	public static PGNTree createRootNode() {
		PGNTree newNode = new PGNTree(0, true, ".", 0, "I am root!");
		return newNode;
	}
	
	
	public Integer getParentNumber() {

		if (this.parent != null) {
			return getParent().getNumber();
		} else {
			return 0;
		}
	}
	
	/**
	 * @return
	 */
	public Integer getColumn() {
		return (this.level*2) + (this.isWhite() ? 0 : 1);
	}


	/**
	 * Promote to first Item of list, continue with parent until root
	 */
	public void promote(PGNTree itemToPromote) {

		PGNTree parent = itemToPromote.getParent();

		if (parent != null) {
			LinkedList<PGNTree> variations = parent.getChildren();
			variations.removeFirstOccurrence(this);
			variations.addFirst(this);
			promote(parent);
		}
	}
	
	public LinkedList<PGNTree> toFlatList (LinkedList<PGNTree> toFlat) {
		
		//parent fist
		toFlat.addLast(this);
		
		// then the children
		for (PGNTree tree : this.getChildren()) {
			tree.toFlatList(toFlat);
		}
		return toFlat;
	}

	public PGNTree getParent() {
		return parent;
	}

	public void setParent(PGNTree parent) {
		this.parent = parent;
	}

	public LinkedList<PGNTree> getChildren() {
		return children;
	}

	public void setChildren(LinkedList<PGNTree> children) {
		this.children = children;
	}

	@Override
	public int hashCode() {
		return Objects.hash(halfMove, level, number, white);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PGNTree))
			return false;
		PGNTree other = (PGNTree) obj;
		return Objects.equals(halfMove, other.halfMove) && level == other.level && number == other.number
				&& white == other.white;
	}

	@Override
	public String toString() {
		return "PGNTree [number=" + number + ", white=" + white
				+ ", halfMove=" + halfMove + ", level=" + level + ", attribute=" + attribute + ", comment=" + comment
				+ ", lastOfLevel=" + lastOfLevel + "]";
	}


	/**
	 * @return the number
	 */
	public Integer getNumber() {
		return number;
	}


	/**
	 * @param number the number to set
	 */
	public void setNumber(Integer number) {
		this.number = number;
	}


	/**
	 * @return the white
	 */
	public Boolean isWhite() {
		return white;
	}


	/**
	 * @param white the white to set
	 */
	public void setWhite(Boolean white) {
		this.white = white;
	}


	/**
	 * @return the halfMove
	 */
	public String getHalfMove() {
		return halfMove;
	}


	/**
	 * @param halfMove the halfMove to set
	 */
	public void setHalfMove(String halfMove) {
		this.halfMove = halfMove;
	}


	/**
	 * @return the level
	 */
	public Integer getLevel() {
		return level;
	}


	/**
	 * @param level the level to set
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}


	/**
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}


	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}


	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}


	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}


	/**
	 * @return the lastOfLevel
	 */
	public Boolean isLastOfLevel() {
		return lastOfLevel;
	}


	/**
	 * @param lastOfLevel the lastOfLevel to set
	 */
	public void setLastOfLevel(Boolean lastOfLevel) {
		this.lastOfLevel = lastOfLevel;
	}


	@Override
	public int compareTo(PGNTree o) {

		Integer numberCompare = Integer.valueOf(this.getNumber()).compareTo(Integer.valueOf(o.getNumber()));
		Integer numberParentCompare = Integer.valueOf(this.getParentNumber())
				.compareTo(Integer.valueOf(o.getParentNumber()));
		Integer columnCompare = Integer.valueOf(this.getColumn()).compareTo(Integer.valueOf(o.getColumn()));

		if (0 != numberCompare) {
			return numberCompare;
		} else if (0 != numberParentCompare) {
			return numberParentCompare;
		} else {
			return columnCompare;
		}
	}
	
	public String getPosition() {
		return getNumber() + "." + getParentNumber() + "." + getColumn(); 
	}

	
	/**
	 * Splits comments 
	 * @param maxLenght
	 * @return
	 */
	public ArrayList<String> getStructuredComments(Integer maxLenght) {

		String[] commentToken = this.comment.replace("{", " ").replace("}", " ").split(" ");

		StringBuilder commentLine = new StringBuilder();
		ArrayList<String> all = new ArrayList<String>();

		for (Integer i = 0; i < commentToken.length; i++) {
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


}
