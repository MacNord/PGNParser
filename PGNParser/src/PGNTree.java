import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.event.ListSelectionEvent;

public class PGNTree implements Comparable<PGNTree> {

	/**
	 * who am I = way to get here..
	 */
	public String path = "";

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

	Boolean begin = false;
	
	Location loc;

	private PGNTree(Integer number, Boolean whitheOrBlack, String halfMove, Integer level, String comment) {
		this.number = number;
		this.white = whitheOrBlack;
		this.halfMove = halfMove;
		this.level = level;
		this.comment = comment;
	}

	public static PGNTree createChildNode(PGNTree parent, Integer number, Boolean whitheOrBlack, String halfMove,
			Integer level, String comment) {

		PGNTree newNode = new PGNTree(number, whitheOrBlack, halfMove, level, comment);
		newNode.setParent(parent);

		parent.children.add(newNode);

		newNode.setPath(parent.getPath() + (parent.children.size() - 1));

		return newNode;

	}

	public static PGNTree createRootNode() {
		PGNTree newNode = new PGNTree(0, true, ".", 0, "I am root!");

		newNode.setPath("0");

		return newNode;
	}

	public Integer getParentNumber() {

		if (this.parent != null) {
			return getParent().getNumber();
		} else {
			return 0;
		}
	}

	public int getHalfMoveNumber() {
		return ((this.number - 1) * 2) + (this.isWhite() ? 0 : 1);
	}

	/**
	 * @return
	 */
	public Integer getColumn() {
		return (this.level * 2) + (this.isWhite() ? 0 : 1);
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

	public LinkedList<PGNTree> toFlatList(LinkedList<PGNTree> toFlat) {

		// parent fist
		toFlat.addLast(this);

		// then the children
		for (PGNTree tree : this.getChildren()) {
			tree.toFlatList(toFlat);
		}
		return toFlat;
	}

	/**
	 * 
	 * @param root
	 * @return
	 */
	public int maxWith(PGNTree root) {

		int currentWith = 0;
		for (int i = 0; i < root.getChildren().size(); i++) {
			PGNTree child = root.getChildren().get(i);
			currentWith += (i + child.maxWith(child));
		}
		return currentWith;
	}

	/**
	 * 
	 * @param root
	 * @param dept
	 * @return
	 */

	public int maxDepth(PGNTree root) {

		if (root == null || root.getChildren().isEmpty()) {
			return 0;
		} else {
			int currentDept = 0;
			for (int i = 0; i < root.getChildren().size(); i++) {
				PGNTree child = root.getChildren().get(i);
				currentDept = Math.max(currentDept, child.maxDepth(child));
			}
			return currentDept + 1;
		}
	}

	public boolean fillMatrix(TreeMap<Location, PGNTree> locations, int x, int y, PGNTree... children) {

		int offset = 0;

		for (int i = 0; i < children.length; i++) {
			

			PGNTree child = children[i];
			boolean childWorked = false;

			System.out
					.println("dealing with " + child.getNumber() + ". " + child.getHalfMove() + "/" + child.getPath());

			Location loc = new Location(x + i + offset, y);
			

			PGNTree existing = locations.get(loc);

			if (existing == null) {
				child.setLoc(loc);
				locations.put(loc, child);
				System.out.println("puting on loc " + child.getNumber() + ". " + child.getHalfMove() + "/"
						+ child.getPath() + loc);
			} else {

				if ("5. Ng4/00000000002"
						.equals(child.getNumber() + ". " + child.getHalfMove() + "/" + child.getPath())) {
					System.out.println("why not?");
				}
				
				
				if (child.getParent() == null) {
					System.out.println("no parent " + child.getHalfMove() + " " + child.getNumber());
					System.exit(0);
				}
				
				if (child.getParent().getLoc() == null) {
					System.out.println("no lock " + child.getHalfMove() + " " + child.getNumber());
					System.exit(0);
				}
				
				if (child.getParent().getLoc().getX() == loc.getX()) {
					return false;
				} else {
					// root, make it work
					
					while (!childWorked) {
						// move x more until it works...
						offset++;
						System.out.println("Trying with location x " + (x + i + offset) + " y " + y);
						childWorked = child.fillMatrix(locations, x + i + offset, y, child);
					}
					// go on with next child
					continue;
				}
			}

			// one level down
			PGNTree[] array = child.getChildren().stream().toArray(PGNTree[]::new);
			childWorked = child.fillMatrix(locations, x + i, y + 1, array);

			if (!childWorked) {
				
				child.setLoc(null);
				locations.remove(loc);
				
				if (child.getParent() == null) {
					System.out.println("hat no parent " + child.getHalfMove() + " " + child.getNumber());
					System.exit(0);
				}

				if (child.getParent().getLoc().getX() == loc.getX()) {

					return false;
				} else {
					// root, make it work
					//j = 0;
					while (!childWorked) {
						// move x more until it works...
						offset++;
						System.out.println("Trying with location x " + (x + i + offset) + " y " + y);
						childWorked = child.fillMatrix(locations, x + i + offset, y, child);
					}
				}
			}
		}
		// must have worked at that point for all children
		return true;
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
		return "PGNTree [number=" + number + ", white=" + white + ", halfMove=" + halfMove + ", level=" + level
				+ ", attribute=" + attribute + ", comment=" + comment + ", lastOfLevel=" + lastOfLevel + "]";
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
		return this.comment.replace("{", " ").replace("}", " ");
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

		Integer halfMoveCompare = Integer.valueOf(this.getHalfMoveNumber())
				.compareTo(Integer.valueOf(o.getHalfMoveNumber()));

		if (0 != halfMoveCompare) {
			return halfMoveCompare;
		} else {

			if (this.getPath().length() < o.getPath().length()) {
				return -1;
			} else if (this.getPath().length() > o.getPath().length()) {
				return 1;
			} else {
				// same index length, compare lexical
				return this.getPath().compareTo(o.getPath());
			}

		}
	}

//	/**
//	 * 
//	 * @return
//	 */
//	public String getPosition() {
//		return getNumber() + "." + getParentNumber() + "." + getColumn(); 
//	}

	/**
	 * Splits comments
	 * 
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

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the loc
	 */
	public Location getLoc() {
		return loc;
	}

	/**
	 * @param loc the loc to set
	 */
	public void setLoc(Location loc) {
		this.loc = loc;
	}

}
