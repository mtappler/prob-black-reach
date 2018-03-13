package at.tugraz.alergia.active.eval.gridworld;

public class Coordinate {
	// (0,0) in top left corner
	public final int x;
	public final int y;
	public Coordinate(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	public Coordinate move(Direction direction){
		switch (direction) {
		case North:
			return new Coordinate(x, y-1);
		case South:
			return new Coordinate(x, y+1);
		case West:
			return new Coordinate(x-1, y);
		case East:
			return new Coordinate(x+1, y);
		default:
			return null;
		}
	}
	
}