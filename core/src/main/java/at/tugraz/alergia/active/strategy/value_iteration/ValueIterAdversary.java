package at.tugraz.alergia.active.strategy.value_iteration;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.active.strategy.adversary.AdversaryI;
import at.tugraz.alergia.active.strategy.value_iteration.ReachPropertyComposedMDP.State;
import at.tugraz.alergia.data.InputSymbol;

public class ValueIterAdversary implements AdversaryI{


	private ReachPropertyComposedMDP boundedPropertyMDP;
	private Map<State, InputSymbol> sigma;
	private State currentState = null;

	public ValueIterAdversary(ReachPropertyComposedMDP boundedPropertyMDP, Map<State, InputSymbol> sigma) {
		this.boundedPropertyMDP = boundedPropertyMDP;
		this.sigma = sigma;
	}

	@Override
	public void reset() {
		currentState = boundedPropertyMDP.getInitial();		
	}

	@Override
	public void executeStep(String input, String output) {
		if(currentState == null)
			return;
		Set<Pair<Double, State>> transitions = currentState.getTransitions(input);
		
		for(Pair<Double, State> t : transitions){
			if(t.getRight().getLabel().equals(output)){
				currentState = t.getRight();
				return;
			}
		}
		currentState = null;
	}

	@Override
	public Optional<String> optimalInput() {
		return Optional.ofNullable(sigma.get(currentState)).map(InputSymbol::stringRepresentation);
	}

}
