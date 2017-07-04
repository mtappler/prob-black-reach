/*******************************************************************************
 * prob-black-reach
 * Copyright (C) 2017 TU Graz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package at.tugraz.alergia.data;

import java.util.Optional;

// one of OutputSymbol, (InputSymbol,OutputSymbol), (TimeDelay, OutputSymbol) <- this one should be 
// (time, output) according to the paper, but let's try it this way, to make presentation uniform (maybe add some 
// dummysymbol at the end if necessary)
public interface Step {
	OutputSymbol getOutputSymbol();
	
	// not strictly equal but same labels
	boolean sameAs(Step other);
	
	default Optional<String> additionalStepLabel(){
		return Optional.empty();
	}
}
