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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FiniteString<S extends Step> {
	private List<S> content = null;
	private OutputSymbol initialOutput = null;
	// strings with inputs actually start with outputs but we ignore this here, as it implicitly handled elsewhere
	public FiniteString(List<S> content, OutputSymbol initialOutput){
		this.content = content;
		this.initialOutput = initialOutput;
	}
	public S get(int index){
		return content.get(index);
	}
	public S firstSymbol(){
		return get(0);
	}
	public int size(){
		return content.size();
	}
	public FiniteString<S> prefix(int index){
		List<S> result = new ArrayList<S>(content);
		if(index + 1 < result.size())
			result.subList(index + 1, result.size()).clear();
		return new FiniteString<S>(result,initialOutput);
	}
	
	public Set<FiniteString<S>> prefixes(){
		Set<FiniteString<S>> prefixes = new HashSet<FiniteString<S>>();
		for(int i = 0; i < content.size(); i++)
			prefixes.add(prefix(i));
		return prefixes;
	}
	
	public FiniteString<S> suffix(int index){
		List<S> result = new ArrayList<S>(content);
		OutputSymbol initialOutput = null;
		if(index > size())
			throw new IndexOutOfBoundsException("Index too large");
		
		if(index != 0 && index < result.size()){
			result.subList(0,index).clear();
			initialOutput = content.get(index - 1).getOutputSymbol();
		}
		else if(index != 0){
			result.clear();
			initialOutput = content.get(content.size()-1).getOutputSymbol();
		} 
		else {
			initialOutput = this.initialOutput;
		}
		return new FiniteString<S>(result,initialOutput);
	}
	public<T extends Step> FiniteString<T> map(Function<S,T> mapping){
		return new FiniteString<T>(content.stream().map(mapping).collect(Collectors.toList()),initialOutput);
	}
	
	public boolean prefixOf(FiniteString<S> string){
		if(string.content.size() < content.size() || 
				!initialOutput.equals(string.getInitialOutput()))
			return false;
		Iterator<S> stringIter = string.content.iterator();
		Iterator<S> prefixIter = content.iterator();
		while(prefixIter.hasNext()){
			if(!stringIter.next().equals(prefixIter.next()))
				return false;
		}
		return true;
	}
	public List<S> getContent() {
		return content;
	}
	@Override
	public String toString() {
		return String.join(",", content.stream().map(Object::toString).collect(Collectors.toList()));
	}
	public OutputSymbol getInitialOutput() {
		return initialOutput;
	}
	public void setInitialOutput(OutputSymbol initialOutput) {
		this.initialOutput = initialOutput;
	}
	public void append(S step) {
		content.add(step);
	}
	

}
