import java.util.ArrayList;

public class RuppinProtocol {
	private ArrayList<Client> clients = new ArrayList<>();
	private Client currentClient = null;

	private static final int WAITING = 0;
	private static final int NEW_USER = 1;
	private static final int EXISTING_USER = 2;
	private static final int VERIFY_PASSWORD = 3;
	private static final int MAIN_MENU = 4;
	private static final int UPDATE_DETAILS = 5;

	private int state = WAITING;

	public String processInput(String input) {
		String output = "";

		switch (state) {
		case WAITING:
			output = "New user? (y/n)";
			state = NEW_USER;
			break;

		case NEW_USER:
			if ("y".equalsIgnoreCase(input)) {
				output = "Choose your username:";
				state = EXISTING_USER;
			} else if ("n".equalsIgnoreCase(input)) {
				output = "username:";
				state = VERIFY_PASSWORD;
			} else {
				output = "Invalid input. Please type 'y' or 'n'.";
			}
			break;

		case EXISTING_USER:
			if (!checkUser(input)) {
				currentClient = new Client(input, null, false, false);
				clients.add(currentClient);
				output = "Name OK. Choose your password:";
			} else {
				output = "Name not OK. Username exists. Choose a different name:";
			}
			break;

		case VERIFY_PASSWORD:
			currentClient = getUser(input);
			if (currentClient != null) {
				output = "password:";
				state = MAIN_MENU;
			} else {
				output = "User not found. Try again.";
				state = NEW_USER;
			}
			break;

		case MAIN_MENU:
			if (input != null && currentClient.getPassword() != null && input.equals(currentClient.getPassword())) {
				output = "Last time you gave me the following information: " + "you are "
						+ (currentClient.getIsStudent() ? "a student at Ruppin" : "not a student at Ruppin")
						+ " and you are " + (currentClient.getIsHappy() ? "Happy" : "not Happy") + ". "
						+ "Any changes since last time? (y/n)";
				state = UPDATE_DETAILS;
			} else {
				output = "Invalid password. Try again.";
				state = VERIFY_PASSWORD;
			}
			break;

		case UPDATE_DETAILS:
			if ("y".equalsIgnoreCase(input)) {
				output = "Are you still a student at Ruppin? (y/n)";
			} else if ("n".equalsIgnoreCase(input)) {
				output = "Thanks";
				state = WAITING;
			} else {
				output = "Invalid input. Please type 'y' or 'n'.";
			}
			break;

		default:
			output = "Error: Invalid state.";
			break;
		}

		return output;
	}

	private boolean checkUser(String username) {
		for (Client client : clients) {
			if (client.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	private Client getUser(String username) {
		for (Client client : clients) {
			if (client.getUsername().equals(username)) {
				return client;
			}
		}
		return null;
	}
}
