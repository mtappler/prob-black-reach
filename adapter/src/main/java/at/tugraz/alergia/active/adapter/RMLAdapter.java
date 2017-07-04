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
package at.tugraz.alergia.active.adapter;

import java.io.File;

public class RMLAdapter implements Adapter {

	// due to horrible usability of the PLASMA Lab library, RML support is temporarily discontinued
	public void readRMLFile(String name, File rmlFile){
//		RMLModelFactory mf = new RMLModelFactory();
//		RMLModel model = (RMLModel) mf.createAbstractModel(name, rmlFile);
//		model.clean();
//		PlasmaSystem sys = new  PlasmaSystem(model, null);
//		Random rnd = new  Random();
//		model.createInitialState(sys, 0, rnd );
//		InterfaceState state = model.newPath();
//		InterfaceState state1 = model.simulate();
//		InterfaceState state2 = model.simulate();
//		InterfaceState state3 = model.simulate();
//		
//		System.out.println(model.checkForErrors());
	}

	@Override
	public void init(long seed) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String reset() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String execute(String input) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	public static void main(String[] args){
//		PluginManager pm = PluginManagerFactory.createPluginManager();
//		pm.addPluginsFrom(ClassURI.CLASSPATH);
//		RMLAdapter adapter = new RMLAdapter();
//		adapter.readRMLFile("slot-machine", new File("src/main/resources/slot_machine.prism"));
//	}
}
