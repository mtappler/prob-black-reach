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
package at.tugraz.alergia.pta;

import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.Step;

public class PrefixTreeAcceptor<S extends Step> {

	// all strings start with an output symbol
	private OutputSymbol rootSymbol = null;
	private PTANode<S> root = null;
	
	public PTANode<S> getRoot() {
		return root;
	}
	public void setRoot(PTANode<S> root) {
		this.root = root;
	}
	private void init(PTANodeFactory<S> factory){
		root = factory.create(rootSymbol, null);
	}
	public void add(PTANodeFactory<S> factory,FiniteString<S> string){
		if(rootSymbol == null){
			rootSymbol = string.getInitialOutput();
			init(factory);
		} else if(!rootSymbol.equals(string.getInitialOutput())){
			throw new UnsupportedOperationException("Cannot learn from strings with differing initial outputs");
		}
		add(factory,root,string);
	}
	private void add(PTANodeFactory<S> factory,PTANode<S> current, FiniteString<S> string) {
		if(string.size() == 0){
			return;
		}
		else {
			PTANode<S> succNode = current.addSuccessor(factory,string.firstSymbol());
			// TODO check whether performance penalty off copying in suffix can be tolerated
			add(factory,succNode,string.suffix(1));
		}
	}
}
