import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int protocolSelection;
		Scanner scanner = new Scanner(System.in);
		try {
			System.out.println("Please enter preferred protocol: 1 or 2");
			protocolSelection = Integer.parseInt(scanner.nextLine());
			if (protocolSelection == 1) {
				KnockKnockServer server = new KnockKnockServer();
				server.startKnockKnockServer();
			} else if (protocolSelection == 2) {
				RuppinServer server = new RuppinServer();
				server.startRuppinServer();
			} else {
				System.out.println("Only 1 or 2");
				System.out.println("Exiting program");
			}

		} catch (NumberFormatException e) {
			System.out.println("Input is not a valid integer number ");
			return;
		} finally {
			scanner.close();
		}

	}
}
