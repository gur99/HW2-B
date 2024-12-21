
public class Client {
	private String username;
	private String password;
	private Boolean isStudent;
	private Boolean isHappy;

	public Client(String username, String password, Boolean isStudent, Boolean isHappy) {
		setUsername(username); // Use the setter to enforce validation
		setPassword(password); // Use the setter to enforce validation
		this.isHappy = isHappy;
		this.isStudent = isStudent;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) throws IllegalArgumentException {
		if (username == null || username.isEmpty() || username.isBlank() || username.contains(" ")) {
			throw new IllegalArgumentException("username cannot be null or empty or contain any spaces.");
		}
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) throws IllegalArgumentException {
		if (password.length() < 9) {
			throw new IllegalArgumentException("Password must be at least 9 characters long.");
		}

		// Check for at least one uppercase letter
		boolean hasUppercase = false;
		for (int i = 0; i < password.length(); i++) {
			if (Character.isUpperCase(password.charAt(i))) {
				hasUppercase = true;
				break;
			}
		}
		if (!hasUppercase) {
			throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
		}

		// Check for at least one lowercase letter
		boolean hasLowercase = false;
		for (int i = 0; i < password.length(); i++) {
			if (Character.isLowerCase(password.charAt(i))) {
				hasLowercase = true;
				break;
			}
		}
		if (!hasLowercase) {
			throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
		}

		// Check for at least one number
		boolean hasDigit = false;
		for (int i = 0; i < password.length(); i++) {
			if (Character.isDigit(password.charAt(i))) {
				hasDigit = true;
				break;
			}
		}
		if (!hasDigit) {
			throw new IllegalArgumentException("Password must contain at least one number.");
		}

		// If all conditions are met, set the password
		this.password = password;
	}

	public Boolean getIsHappy() {
		return isHappy;
	}

	public void setIsHappy(Boolean isHappy) {

		this.isHappy = isHappy;
	}

	public Boolean getIsStudent() {
		return isStudent;
	}

	public void setIsStudent(Boolean isStudent) {
		this.isStudent = isStudent;
	}

	public String toString() {
		String str = "";
		return str += "UserName: " + username + "is Happy: " + isHappy + "is Student: " + isStudent;
	}

}
