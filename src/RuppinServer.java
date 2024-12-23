import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class RuppinServer {

	private ArrayList<Client> clientState = new ArrayList<>();
	private ServerSocket serverSocket = null;

	public void startRuppinServer() {
		loadClientsFromFile();
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
				RuppinProtocol protocol = new RuppinProtocol(this);
				ConnectionHandler handler = new ConnectionHandler(connection, protocol);
//				ConnectionHandler handler = new ConnectionHandler(connection);
				new Thread(handler).start();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.exit(1);
			}
		}
	}

	public ArrayList<Client> getClientState() {
		return clientState;
	}

	// Adds a new user to the clientState list
	public synchronized boolean addUser(String username, String password, boolean isStudent, boolean isHappy) {
		if (checkUser(username)) {
			return false; // Username already exists
		}
		Client newClient = new Client(username, password, isStudent, isHappy);
		clientState.add(newClient);

		// Check if the number of users is divisible by 3 and save backup
		if (clientState.size() % 3 == 0) {
			saveBackupToFile();
		}
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

	public synchronized boolean changePassword(String username, String oldPassword, String newPassword) {
		for (Client client : clientState) {
			if (client.getUsername().equals(username) && (client.getPassword().equals(oldPassword))
					&& !(newPassword.equals(oldPassword))) {
				client.setPassword(newPassword);
				return true; // Password changed successfully
			}
		}
		return false; // Old password is incorrect or user not found
	}

	// Saves the current clientState to a backup CSV file
	private void saveBackupToFile() {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String fileName = "backup_" + timestamp + ".csv";

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			for (Client client : clientState) {
				String line = String.join(",", client.getUsername(), client.getPassword(),
						String.valueOf(client.getIsStudent()), String.valueOf(client.getIsHappy()));
				writer.write(line);
				writer.newLine();
			}
			System.out.println("Backup saved to " + fileName);
		} catch (IOException e) {
			System.err.println("Error saving backup: " + e.getMessage());
		}
	}

	// Updates the state of an existing user
//	public synchronized boolean updateUserState(String username, boolean isStudent, boolean isHappy) {
//		for (int i = 0; i < clientState.size(); i++) {
//			Client client = clientState.get(i);
//			if (client.getUsername().equals(username)) {
//				client.setIsStudent(isStudent);
//				client.setIsHappy(isHappy);
//				return true;
//			}
//		}
//		return false;
//	}
	// Loads clients from the most recent backup file
	private void loadClientsFromFile() {
		try {
			// Find the most recent backup file
			File dir = new File(".");
			File[] files = dir.listFiles((d, name) -> name.startsWith("backup_") && name.endsWith(".csv"));
			if (files == null || files.length == 0) {
				System.out.println("No backup file found. Starting with an empty client list.");
				return;
			}

			Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
			File mostRecentFile = files[0];

			System.out.println("Loading clients from " + mostRecentFile.getName());
			try (BufferedReader reader = new BufferedReader(new FileReader(mostRecentFile))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split(",");
					if (parts.length == 4) {
						String username = parts[0];
						String password = parts[1];
						boolean isStudent = Boolean.parseBoolean(parts[2]);
						boolean isHappy = Boolean.parseBoolean(parts[3]);
						clientState.add(new Client(username, password, isStudent, isHappy));
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error loading clients from file: " + e.getMessage());
		}
	}

	public static class ConnectionHandler implements Runnable {
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

				while ((inputLine = in.readLine()) != null) {
					outputLine = protocol.processInput(inputLine);
					out.println(outputLine);
					if (outputLine.equals("Thanks")) {
						break;
					}

				}
				out.close();
				in.close();
				connection.close();

			} catch (IOException ioe) {
				System.err.println("Problem connecting to server.");
			}

			finally {
				try {
					if (connection != null)
						connection.close();
				} catch (IOException e) {
					System.err.println("Error closing client socket: " + e.getMessage());
				}
			}
		}
	}
}
