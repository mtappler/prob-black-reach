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
package at.tugraz.alergia.active.adapter.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import at.tugraz.alergia.active.Property;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.adapter.prism_matrix_export.MatrixExportAdapter;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;

public class TCPAdapterServer {

	private static String usageString = "usage 'java -jar adapter.jar model-file property-file property-index port [seed]";
	private Adapter adapter = null;
	private ServerSocket socket = null;
	private Property property;

	public TCPAdapterServer(Adapter adapter, int port, Property property) {
		super();
		this.adapter = adapter;
		this.port = port;
		this.property = property;
	}

	private int port;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	public void init() throws IOException {
		System.out.println("Starting...");
		System.out.println("Learning for property: " + property.toString());
		socket = new ServerSocket(port);
		clientSocket = socket.accept();
		System.out.println("Connected...");

		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public void run() throws IOException {
		String inputLine = null;
		boolean initialReset = false;
		FiniteString<InputOutputStep> trace = null;
		while ((inputLine = in.readLine()) != null) {
			if (inputLine.equals("quit"))
				break;
			else if (inputLine.equals("reset")) {
				initialReset = true;
				String output = adapter.reset();
				trace = new FiniteString<>(new ArrayList<>(), new OutputSymbol(output));
				String response = output + "," + (property.evaluate(trace) ? 1 : 0);
				out.print(response);
				out.flush();
				System.out.println("response: " + response);
			} else if (initialReset) {
				try {
					String output = adapter.execute(inputLine);
					trace.append(new InputOutputStep(new InputSymbol(inputLine), new OutputSymbol(output)));
					String response = output + "," + (property.evaluate(trace) ? 1 : 0);
					property.evaluate(trace);
					out.print(response);
					out.flush();
					System.out.println("response: " + response);
				} catch (RuntimeException e) {
					if (e.getMessage().contains("No transition found")) {
						String response = "Not possible to execute " + inputLine + " -- illegal input?";
						System.out.println(response);
						out.println(response);
						out.flush();
					}
				}
			} else {
				String response = "You should reset the SUT before doing anything";
				System.out.println(response);
				out.println(response);
				out.flush();
			}
		}
	}

	public static void main(String args[]) throws Exception {
		String modelName = "../core/src/main/resources/slot_machine_step_count/slot_machine";
		String propertiesFile = "../core/src/main/resources/slot_machine_step_count/slot_machine_full.props";
		int selectedProperty = 40;
		int port = 8888;
		long seed = System.currentTimeMillis();

		if (args.length < 4) {
			System.out.println(usageString);
			return;
		}
		modelName = args[0];
		propertiesFile = args[1];
		String selectedPropertyString = args[2];
		String portString = args[3];
		String seedString = args.length > 4 ? args[4] : null;
		try {
			selectedProperty = Integer.parseInt(selectedPropertyString);
			port = Integer.parseInt(portString);
			seed = Integer.parseInt(seedString);
		} catch (NumberFormatException e) {
			System.out.println(e.getMessage());
			System.out.println(usageString);
		}

		Property property = new Property(propertiesFile, selectedProperty);
		Adapter adapter = new MatrixExportAdapter(modelName);

		adapter.init(seed);

		TCPAdapterServer server = new TCPAdapterServer(adapter, port, property);
		try {
			server.init();
			server.run();
		} catch (Exception e) {
			throw e;
		} finally {
			server.close();
		}
	}

	private void close() throws IOException {
		socket.close();
	}

}
