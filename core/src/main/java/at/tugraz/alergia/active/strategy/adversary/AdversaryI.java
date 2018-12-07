package at.tugraz.alergia.active.strategy.adversary;

import java.util.Optional;

public interface AdversaryI {

	void reset();

	void executeStep(String input, String output);

	Optional<String> optimalInput();

}