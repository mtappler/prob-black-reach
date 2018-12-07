package at.tugraz.alergia.active.strategy.value_iteration;

import java.util.Optional;
import java.util.Set;

import at.tugraz.alergia.active.strategy.adversary.AdversaryI;
import at.tugraz.alergia.data.InputSymbol;

public class RandomisedAdversary implements AdversaryI{


	public RandomisedAdversary() {
	}

	@Override
	public void reset() {
		
	}

	@Override
	public void executeStep(String input, String output) {
		
	}

	@Override
	public Optional<String> optimalInput() {
		return Optional.empty();
	}

}
