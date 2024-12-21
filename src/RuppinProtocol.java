public class RuppinProtocol {

	private static final int WAITING = 0;
	private static final int NEW_USER = 1;
	private static final int GET_USERNAME = 2;
	private static final int GET_PASSWORD = 3;
	private static final int STUDENT_QUESTION = 4;
	private static final int HAPPY_QUESTION = 5;
	private static final int EXISTING_USER = 6;
	private static final int CONFIRM_PASSWORD = 7;
	private static final int UPDATE_STATE = 8;
	private static final int UPDATE_STUDENT_QUESTION = 9;
	private static final int UPDATE_HAPPY_QUESTION = 10;
	private static final int CHANGE_PASSWORD = 11;
	private static final int GET_NEW_PASSWORD = 12;
	private static final int THANKS = 13;

	private int state = WAITING;
	private String username;
	private String password;
	private boolean isStudent;
	private boolean isHappy;

	private RuppinServer server;

	public RuppinProtocol(RuppinServer server) {
		this.server = server;
	}

	public String processInput(String input) {
		String output = "";

		switch (state) {
		case WAITING:
			output = "New user? (y/n)";
			state = NEW_USER;
			break;

		case NEW_USER:
			if (input.equalsIgnoreCase("y")) {
				output = "Choose your username:";
				state = GET_USERNAME;
			} else if (input.equalsIgnoreCase("n")) {
				output = "Username:";
				state = EXISTING_USER;
			} else {
				output = "Invalid response. New user? (y/n)";
			}
			break;

		case GET_USERNAME:
			if (!server.checkUser(input)) {
				username = input;
				output = "Name OK. Choose your password:";
				state = GET_PASSWORD;
			} else {
				output = "Username already exists. Please choose a different username:";
			}
			break;

		case GET_PASSWORD:
			try {
				password = input;
				Client tempClient = new Client(username, password, false, false); // Validation occurs here
				output = "Password OK. Are you a student at Ruppin? (y/n)";
				state = STUDENT_QUESTION;
			} catch (IllegalArgumentException e) {
				output = "Invalid password: " + e.getMessage() + ". Choose your password:";
			}
			break;

		case STUDENT_QUESTION:
			if (input.equalsIgnoreCase("y")) {
				isStudent = true;
			} else if (input.equalsIgnoreCase("n")) {
				isStudent = false;
			} else {
				output = "Invalid response. Are you a student at Ruppin? (y/n)";
				return output;
			}
			output = "Are you Happy? (y/n)";
			state = HAPPY_QUESTION;
			break;

		case HAPPY_QUESTION:
			if (input.equalsIgnoreCase("y")) {
				isHappy = true;
			} else if (input.equalsIgnoreCase("n")) {
				isHappy = false;
			} else {
				output = "Invalid response. Are you Happy? (y/n)";
				return output;
			}
			server.addUser(username, password, isStudent, isHappy);
			output = "Thanks";
			state = THANKS;
			break;

//////////////////////////////////////////////////////
		case EXISTING_USER:
			username = input;
			if (server.checkUser(username)) {
				output = "Password:";
				state = CONFIRM_PASSWORD;
			} else {
				output = "Username not found. Please try again.";
				state = NEW_USER;
			}
			break;

		case CONFIRM_PASSWORD:
			if (server.checkPassword(username, input)) {
				Client client = findClient(username);
				output = "Last time you gave me the following information: " + "you are "
						+ (client.getIsStudent() ? "" : "not ") + "a student at Ruppin and you are "
						+ (client.getIsHappy() ? "Happy" : "not Happy") + ". Any changes since last time? (y/n)";
				state = UPDATE_STATE;
			} else {
				output = "Incorrect password. Please try again.";
			}
			break;

		case UPDATE_STATE:
			if (input.equalsIgnoreCase("n")) {
				output = "Thanks";
				state = THANKS;
			} else if (input.equalsIgnoreCase("y")) {
				output = "Are you a student at Ruppin? (y/n)";
				state = UPDATE_STUDENT_QUESTION;
			} else {
				output = "Invalid response. Any changes since last time? (y/n)";
			}
			break;
		case UPDATE_STUDENT_QUESTION:
			if (input.equalsIgnoreCase("y")) {
				findClient(username).setIsStudent(true);
			} else if (input.equalsIgnoreCase("n")) {
				findClient(username).setIsStudent(false);

			} else {
				output = "Invalid response. Are you a student at Ruppin? (y/n)";
				return output;
			}
			output = "Are you Happy? (y/n)";
			state = UPDATE_HAPPY_QUESTION;
			break;

		case UPDATE_HAPPY_QUESTION:
			if (input.equalsIgnoreCase("y")) {
				findClient(username).setIsHappy(true);

			} else if (input.equalsIgnoreCase("n")) {
				findClient(username).setIsHappy(false);
			} else {
				output = "Invalid response. Are you Happy? (y/n)";
				return output;
			}
			output = "Do you want to change your password? (y/n)";
			state = CHANGE_PASSWORD;
			break;

		case CHANGE_PASSWORD:
			if (input.equalsIgnoreCase("n")) {
				output = "Thanks";
				break;
			} else if (input.equalsIgnoreCase("y")) {
				output = "Choose your new password:";
				state = GET_NEW_PASSWORD;
			} else {
				output = "Invalid response. Do you want to change your password? (y/n)";
			}
			break;

		case GET_NEW_PASSWORD:
			// Validate and set the new password
			try {

				String newPassword = input;
				if (newPassword == null || newPassword.isEmpty()) {
					output = "Password cannot be empty. Choose your new password:";
				} else if (server.changePassword(username, findClient(username).getPassword(), newPassword)) {
					output = "Thanks"; // Successfully changed password
					state = THANKS;
				} else {
					output = "Password change failed. Choose your new password:";
				}
			} catch (IllegalArgumentException e) {
				output = "Illegal Password, Please try agian";
			}
			break;

		case THANKS:
			output = "Thanks";
			break;
		}

		return output;

	}

	private Client findClient(String username) {
		for (Client client : server.getClientState()) {
			if (client.getUsername().equals(username)) {
				return client;
			}
		}
		return null;
	}
}
