import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RuppinServer {

	private ArrayList<Client> clientState = new ArrayList<>();
	private ServerSocket serverSocket = null;

	public void startRuppinServer() {

		try {
			serverSocket = new ServerSocket(4445);
			System.out.println("SERVER listening on port: 4445");
		} catch (IOException e) {
			System.err.println("Could not listen on port: 4445.");
			System.exit(1);
		}

		int clientCounter = 1;

		while (true) {
//			Socket clientSocket = null;

			try {
				Socket connection = serverSocket.accept();
				System.out.println("Client connected " + clientCounter++);
				RuppinProtocol protocol = new RuppinProtocol();
				ConnectionHandler handler = new ConnectionHandler(connection, protocol);
				new Thread(handler).start();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.exit(1);
			}
		}
	}

	// Adds a new user to the clientState list
	public synchronized boolean addUser(String username, String password, boolean isStudent, boolean isHappy) {
		if (checkUser(username)) {
			return false; // Username already exists
		}
		Client newClient = new Client(username, password, isStudent, isHappy);
		clientState.add(newClient);
		return true;
	}

	// Checks if a user with the given username exists
	public synchronized boolean checkUser(String username) {
		for (int i = 0; i < clientState.size(); i++) {
			if (clientState.get(i).getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	// Validates the password for an existing user
	public synchronized boolean checkPassword(String username, String password) {
		for (int i = 0; i < clientState.size(); i++) {
			Client client = clientState.get(i);
			if (client.getUsername().equals(username) && client.getPassword().equals(password)) {
				return true;
			}
		}
		return false;
	}

	// Updates the state of an existing user
	public synchronized boolean updateUserState(String username, boolean isStudent, boolean isHappy) {
		for (int i = 0; i < clientState.size(); i++) {
			Client client = clientState.get(i);
			if (client.getUsername().equals(username)) {
				client.setIsStudent(isStudent);
				client.setIsHappy(isHappy);
				return true;
			}
		}
		return false;
	}

	public static class ConnectionHandler implements Runnable {
		private RuppinServer server;
		private Socket connection;
		private RuppinProtocol protocol;

		public ConnectionHandler(Socket connection, RuppinProtocol protocol) {
			this.connection = connection;
			this.protocol = protocol;
		}

		@Override
		public void run() {
			try {
				PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String inputLine, outputLine;
				outputLine = protocol.processInput(null);
				out.println(outputLine);

				String username = null;
				boolean isNewUser = false;
				boolean isStudent = false;
				boolean isHappy = false;

				while ((inputLine = in.readLine()) != null) {
					outputLine = protocol.processInput(inputLine);
					out.println(outputLine);
					if (outputLine.equals("Thanks")) {
						break;
					}
					if (outputLine.equals("Choose your username:")) {
						username = inputLine;
					} else if (outputLine.equals("Name OK. Choose your password :")) {
						String password = inputLine;
						if (server.addUser(username, password, false, false)) {
							isNewUser = true;
						} else {
							out.println("Username already exists.");
							break;
						}
					} else if (outputLine.equals("Password OK. Are you a student at Ruppin? (y/n)")) {
						isStudent = inputLine.equalsIgnoreCase("y");
					} else if (outputLine.equals("Are you Happy? (y/n)")) {
						isHappy = inputLine.equalsIgnoreCase("y");
						if (isNewUser) {
							server.updateUserState(username, isStudent, isHappy);
						}
						out.println("Thanks");
						break;
					} else if (outputLine.startsWith("Last time you gave me the following information:")) {
						// Handle user updates
						if (inputLine.equalsIgnoreCase("n")) {
							out.println("Thanks");
							break;
						} else if (inputLine.equalsIgnoreCase("y")) {
							// Logic to update user details
						}
					}

				}

				out.close();
				in.close();
				connection.close();

			} catch (IOException ioe) {
				System.err.println("Problem connecting to server.");
			}
		}
	}
}
