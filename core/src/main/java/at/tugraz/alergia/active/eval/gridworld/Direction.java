package at.tugraz.alergia.active.eval.gridworld;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.data.InputSymbol;

public enum Direction {
	North, South, West, East;

	public static InputSymbol[] asInputSymbols(){
		InputSymbol[] symbols = new InputSymbol[4];
		int i = 0;
		for(Direction dir : values()){
			symbols[i++] = new InputSymbol(dir.toString());
		}
		return symbols;
	}
	public Pair<Direction,Direction> orthogonalDirection() {
		switch(this){
		case North:
		case South:
			return Pair.of(East, West);
		case East:
		case West:
			return Pair.of(North, South);
		default:
			return null;
		}
	}
}
