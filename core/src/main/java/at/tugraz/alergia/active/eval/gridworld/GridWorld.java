package at.tugraz.alergia.active.eval.gridworld;
import static at.tugraz.alergia.active.eval.gridworld.Terrain.*;

public class GridWorld {
	private Terrain[][] world = null;
	private Coordinate goal = null;
	private Coordinate start = null;
	
	public GridWorld(Terrain[][] world, Coordinate goal, Coordinate start) {
		super();
		this.world = world;
		this.goal = goal;
		this.start = start;
	}

	public static GridWorld minimalInstance(){
		Terrain[][] world = new Terrain[][]{
			{Concrete,Grass},
			{Grass,Grass}
		};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(1, 1);
		return new GridWorld(world, goal, start);
	}
	

	public static GridWorld moderateInstance(){
		Terrain[][] world = new Terrain[][]{
			{Concrete,Concrete,Grass},
			{Mud,Concrete,Grass},
			{Mud,Mud,Grass}
		};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(2, 2);
		return new GridWorld(world, goal, start);
	}
	
	public static GridWorld wallsInstance(){
		Terrain[][] world = new Terrain[][]{
			{Concrete,Concrete,  Concrete,    Grass, Wall},
			{Wall,    Wall,      Grass,       Grass, Concrete },
			{Mud,     Mud,       Concrete, Grass, Concrete},
			{Mud,     Grass,     Concrete, Mud,   Wall},
			{Grass,   Concrete,  Mud,      Grass, Wall}
		};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(0, 4);
		return new GridWorld(world, goal, start);
	}
	
	public static GridWorld difficultWallsInstance(){
		Terrain[][] world = new Terrain[][]{
			{Concrete,Concrete,  Concrete,    Mud, Wall},
			{Wall,    Wall,      Wall,        Concrete,   Mud },
			{Sand,    Mud ,      Grass,       Concrete,  Grass},
			{Mud,     Grass,     Concrete,    Grass,   Wall},
			{Grass,   Sand,      Mud,         Sand,  Wall}
		};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(0, 4);
		return new GridWorld(world, goal, start);
	}
	

	public static GridWorld firstInstance(){
		Terrain[][] world = new Terrain[][]{
			{Concrete,Concrete,  Concrete,    Mud, Wall},
			{Wall,    Wall,      Wall,        Concrete,   Mud },
			{Sand,    Mud ,      Grass,       Concrete,  Grass},
			{Mud,     Grass,     Concrete,    Mud,   Wall},
			{Grass,   Sand,      Mud,         Grass,  Wall}
		};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(0, 4);
		return new GridWorld(world, goal, start);
	}
	public static GridWorld secondInstance(){
//		Terrain[][] world = new Terrain[][]{
//			{Grass,   Sand,      Mud,         Concrete, Sand,    Grass,    Mud,     Sand,     Concrete},
//			{Mud,     Grass,     Concrete,    Sand,     Grass,   Mud,      Concrete,Grass,    Mud},
//			{Concrete,Mud,       Sand,        Grass,    Mud,     Concrete, Grass,   Mud,      Sand},
//			{Sand,    Concrete,  Grass,       Mud,      Concrete,Grass,    Sand,    Concrete, Grass},
//			{Mud,     Grass,     Concrete,    Concrete, Grass,   Sand,     Mud,     Concrete, Concrete},
//			{Grass,   Sand,      Mud,         Grass,    Sand,    Concrete, Grass,   Mud,      Sand},
//			};
		Terrain[][] world = new Terrain[][]{
				{Concrete,Concrete,  Mud,         Concrete, Concrete,Grass,    Mud,     Sand},
				{Concrete,Grass,     Wall,        Wall,     Grass,   Mud,      Concrete,Grass},
				{Concrete,Mud,       Sand,        Grass,    Mud,     Concrete, Grass,   Wall},
				{Sand,    Concrete,  Concrete,    Concrete, Concrete,Grass,    Sand,    Concrete},
				{Mud,     Wall,      Concrete,    Concrete, Grass,   Sand,     Mud,     Concrete},
				{Grass,   Sand,      Mud,         Grass,    Wall,    Wall,     Grass,   Mud},
				};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(7, 5);
		return new GridWorld(world, goal, start);
	}
	
	
	public static GridWorld largeWallsInstance(){
		Terrain[][] world = new Terrain[][]{
			{Concrete,Concrete,  Concrete,    Pavement,  Wall,      Concrete, Concrete},
			{Wall,    Wall,      Wall,        Pavement, Grass,     Grass,     Wall },
			{Mud,     Mud,       Wall, 	      Pavement, Grass,     Mud,       Wall },
			{Mud,     Grass,     Wall,        Pavement, Concrete,  Wall,      Wall },
			{Mud,     Grass,     Grass,       Pavement, Mud,       Wall,     Wall },
			{Mud,     Grass   ,  Mud,         Grass,    Wall,      Wall,     Wall },
			{Mud,     Grass   ,  Mud,         Grass,    Grass,     Mud,      Mud  },
				};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(6, 6);
		return new GridWorld(world, goal, start);
	}
	public static GridWorld diffWalls3Instance(){
		Terrain[][] world = new Terrain[][]{
			{Mud,     Sand,      Sand,        Concrete, Wall,      Concrete  },
			{Wall,    Wall,      Wall,        Concrete, Grass,     Wall      },
			{Mud,     Mud,       Wall, 	      Concrete, Grass,     Mud        },
			{Mud,     Mud,       Sand,        Concrete, Concrete,  Wall       },
			{Mud,     Sand,      Sand,        Concrete, Grass,     Mud        },
			{Mud,     Sand   ,   Sand,        Concrete, Concrete,  Grass       },
				};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(5, 5);
		return new GridWorld(world, goal, start);
	}
	
	public static GridWorld diff3WoWallsInstance(){
		Terrain[][] world = new Terrain[][]{
			{Mud,     Sand,      Sand,        Concrete, Gravel,    Concrete  },
			{Gravel,  Gravel,    Gravel,      Concrete, Grass,     Gravel      },
			{Mud,     Mud,       Gravel,      Concrete, Grass,     Mud        },
			{Mud,     Mud,       Sand,        Concrete, Concrete,  Gravel       },
			{Mud,     Sand,      Sand,        Concrete, Grass,     Mud        },
			{Mud,     Sand   ,   Sand,        Concrete, Concrete,  Grass       },
				};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(5, 5);
		return new GridWorld(world, goal, start);
	}
	
	public static GridWorld finalWallsInstance(){
		Terrain[][] world = new Terrain[][]{
			{Mud,       Sand,      Mud,         Sand,     Wall,      Wall  },
			{Wall,      Wall,      Wall,        Grass   , Sand ,     Wall      },
			{Concrete,  Mud,       Wall, 	    Concrete, Mud,       Sand        },
			{Concrete,  Concrete,  Sand,        Mud,      Concrete,  Wall       },
			{Grass,     Sand,      Concrete,    Sand,     Grass,     Mud        },
			{Sand,      Grass   ,  Mud,         Concrete, Mud,       Grass       },
				};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(5, 5);
		return new GridWorld(world, goal, start);
	}
	
	public static GridWorld diffWalls2Instance(){
		Terrain[][] world = new Terrain[][]{
			{Mud,     Sand,      Sand,        Concrete, Wall,     Concrete,  Concrete},
			{Wall,    Wall,      Wall,        Concrete, Grass,     Grass,     Wall },
			{Mud,     Mud,       Wall, 	      Concrete, Grass,     Mud,       Wall },
			{Mud,     Mud,       Sand,        Concrete, Concrete,  Wall,      Wall },
			{Mud,     Sand,      Sand,        Concrete, Grass,     Mud,       Wall },
			{Mud,     Sand   ,   Sand,        Concrete, Concrete,  Grass,     Mud  },
				};
		Coordinate start = new Coordinate(0, 0);
		Coordinate goal = new Coordinate(6, 5);
		return new GridWorld(world, goal, start);
	}
	
	public static GridWorld pacPaperInstance(){
		Terrain[][] world = new Terrain[][]{
			{Pavement, Pavement, Pavement, Pavement, Wall  , Gravel, Gravel, Gravel},
			{Pavement, Pavement, Pavement, Pavement, Sand  , Sand  , Sand  , Sand  },
			{Pavement, Pavement, Pavement, Pavement, Wall  , Sand  , Sand  , Sand  },
			{Pavement, Pavement, Pavement, Pavement, Gravel, Wall  , Gravel, Gravel},
			{Sand    , Sand    , Sand    , Sand    , Gravel, Gravel, Gravel, Wall  },
			{Grass   , Grass   , Grass   , Wall    , Grass , Gravel, Gravel, Gravel},
			{Grass   , Grass   , Grass   , Grass   , Grass , Gravel, Wall  , Gravel},
			{Grass   , Wall    , Grass   , Wall    , Grass , Gravel, Gravel, Gravel},
			
		};
		Coordinate start = new Coordinate(7, 7);
		Coordinate goal = new Coordinate(0, 0);
		return new GridWorld(world, goal, start);
	}
	
	public static void main(String[] args){
		GridWorld minimalWorld = secondInstance();
		GridWorldTranslator translator = new GridWorldTranslator("second");
		System.out.println(translator.transformToRML(minimalWorld, 15));
	}

	public int width() {
		return world[0].length;
	}
	public int height() {
		return world.length;
	}

	public Terrain get(Coordinate position) {
		return world[position.y][position.x];
	}

	public Coordinate getGoal() {
		return goal;
	}

	public Coordinate getStart() {
		return start;
	}
}
