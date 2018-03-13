package at.tugraz.alergia.active.eval.gridworld;

public enum Terrain {
	Concrete(1),Grass(2), Wall(3), Mud(4),
	Pavement(5),Gravel(6), Sand(7)
	;
	

    private final int value;

    Terrain(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

	public double errorProbability() {
		switch(this){
		case Pavement:
			return 0.05;
		case Gravel:
			return 0.15;
		case Sand:
			return 0.25; // different probability in PAC learning paper
		case Grass:
			return 0.2; // different probability in PAC learning paper
		case Mud:
			return 0.4;
		default:
			return 0;
		}
	}
}
