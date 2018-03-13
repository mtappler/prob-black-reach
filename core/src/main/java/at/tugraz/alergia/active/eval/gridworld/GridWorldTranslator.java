package at.tugraz.alergia.active.eval.gridworld;

import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.util.export.RMLExporterMDP;
import static at.tugraz.alergia.active.eval.gridworld.Terrain.*;

import org.apache.commons.lang3.tuple.Pair;

public class GridWorldTranslator {

	RMLExporterMDP mdpExporter = null;

	public GridWorldTranslator(String worldName) {
		mdpExporter = new RMLExporterMDP("gridworld_" + worldName);
	}

	public MarkovChain<InputOutputStep> transform(GridWorld world) {
		return null;
	}

	public String transformToRML(GridWorld world, int stepBound) {
		StringBuilder sb = new StringBuilder();
		mdpExporter.appendLine(sb, mdpExporter.modelType());
		mdpExporter.appendStepBountConstant(sb, stepBound);
		mdpExporter.appendLine(sb, "module " + mdpExporter.getModelName());
		mdpExporter.appendLine(sb, String.format("  x : [0..%d] init %d;", world.width() - 1, world.getStart().x));
		mdpExporter.appendLine(sb, String.format("  y : [0..%d] init %d;", world.height() - 1, world.getStart().y));
		mdpExporter.appendLine(sb, String.format("  output : [1..%d] init %d;", Terrain.values().length,
				world.get(world.getStart()).getValue()));
		for (int x = 0; x < world.width(); x++) {
			for (int y = 0; y < world.height(); y++) {
				appendActionsForPosition(new Coordinate(x, y), world, sb);
			}
		}
		mdpExporter.appendLine(sb, "endmodule");
		appendStepCounter(sb);
		for (Terrain t : Terrain.values()) {
			mdpExporter.appendLine(sb,
					String.format("label \"%s\" = output=%d;", t.toString().toLowerCase(), t.getValue()));
		}
		mdpExporter.appendLine(sb,
				String.format("label \"goal\" = x=%d & y=%d;", world.getGoal().x, world.getGoal().y));
		return sb.toString();
	}

	private void appendStepCounter(StringBuilder sb) {
		mdpExporter.appendLine(sb, "module StepCounter");
		mdpExporter.appendLine(sb, "  steps : [0..BOUND] init 0; ");
		for (Direction d : Direction.values())
			mdpExporter.appendLine(sb, "  [" + d.toString() + "] true -> (steps'=min(BOUND,steps + 1));");
		mdpExporter.appendLine(sb, "endmodule");
	}

	private void appendActionsForPosition(Coordinate position, GridWorld world, StringBuilder sb) {
		for (Direction d : Direction.values())
			appendActionsForDirection(d, position, world, sb);
		sb.append(System.lineSeparator());
	}

	private void appendActionsForDirection(Direction d, Coordinate position, GridWorld world, StringBuilder sb) {
		Coordinate newPosition = position.move(d);
		Terrain output = null;
		String actionAndCondition = String.format("  [%s] (x=%d) & (y=%d) -> ", d.toString(), position.x, position.y);
		if (isWall(world, newPosition)) {
			output = Wall;
			newPosition = position;
			String update = updateString(newPosition, output);
			mdpExporter.appendLine(sb, actionAndCondition + update + ";");
		} else {
			String update = null;
			switch (world.get(newPosition)) {
			case Concrete:
				output = Concrete;
				update = updateString(newPosition, output);
				mdpExporter.appendLine(sb, actionAndCondition + update + ";");
				break;
			case Sand:
			case Gravel:
			case Pavement:
			case Mud:
			case Grass:
				Pair<Direction, Direction> otherDirections = d.orthogonalDirection();
				Coordinate alternativePosition1 = newPosition.move(otherDirections.getLeft());
				Coordinate alternativePosition2 = newPosition.move(otherDirections.getRight());
				output = world.get(newPosition);
				if (isWall(world, alternativePosition1) && isWall(world, alternativePosition2)) {
					update = updateString(newPosition, output);
					mdpExporter.appendLine(sb, actionAndCondition + update + ";");
				} else if (!isWall(world, alternativePosition1) && isWall(world, alternativePosition2)) {
					Terrain altOutput = world.get(alternativePosition1);
					double errorProbability = output.errorProbability();
					update = updateString(newPosition, output);
					String altUpdate = updateString(alternativePosition1, altOutput);
					mdpExporter.appendLine(sb, actionAndCondition + String.format("%.3f : %s + %.3f : %s",
							1 - errorProbability, update, errorProbability, altUpdate) + ";");
				} else if (isWall(world, alternativePosition1) && !isWall(world, alternativePosition2)) {
					Terrain altOutput = world.get(alternativePosition2);
					double errorProbability = output.errorProbability();
					update = updateString(newPosition, output);
					String altUpdate = updateString(alternativePosition2, altOutput);
					mdpExporter.appendLine(sb, actionAndCondition + String.format("%.3f : %s + %.3f : %s",
							1 - errorProbability, update, errorProbability, altUpdate) + ";");
				} else {
					Terrain altOutput1 = world.get(alternativePosition1);
					Terrain altOutput2 = world.get(alternativePosition2);
					double errorProbability = output.errorProbability();
					update = updateString(newPosition, output);
					String altUpdate1 = updateString(alternativePosition1, altOutput1);
					String altUpdate2 = updateString(alternativePosition2, altOutput2);
					mdpExporter
							.appendLine(sb,
									actionAndCondition + String.format("%.3f : %s + %.3f : %s + %.3f : %s",
											1 - errorProbability, update, errorProbability / 2, altUpdate1,
											errorProbability / 2, altUpdate2) + ";");
				}
				break;
			case Wall:
				throw new Error("Should not happen, should be in then branch of if");
				// output = Wall;
				// newPosition = position;
				// update = updateString(newPosition,output);
				// mdpExporter.appendLine(sb,actionAndCondition + update +";");
			default:
				break;
			}
		}
	}

	private String updateString(Coordinate newPosition, Terrain output) {
		return String.format("(x'=%d) & (y'=%d) & (output'=%d)", newPosition.x, newPosition.y, output.getValue());
	}

	private boolean isWall(GridWorld world, Coordinate position) {
		return position.x < 0 || position.y < 0 || position.x >= world.width() || position.y >= world.height()
				|| world.get(position) == Wall;
	}

}
