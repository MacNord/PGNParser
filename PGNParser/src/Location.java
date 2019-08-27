import java.util.Objects;

public class Location implements Comparable {
	
	int x;
	
	int y;
	


	public Location(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}


	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Location))
			return false;
		Location other = (Location) obj;
		return x == other.x && y == other.y;
	}


	@Override
	public String toString() {
		return "Location [x=" + x + ", y=" + y + "]";
	}


	@Override
	public int compareTo(Object o) {
		Location other = (Location) o;
		int xCompare = Integer.valueOf(x).compareTo(Integer.valueOf(other.x));
		int yCompare = Integer.valueOf(y).compareTo(Integer.valueOf(other.y));

		if (yCompare != 0) {
			return yCompare;
		} else {
			return xCompare;
		}

	}

}
