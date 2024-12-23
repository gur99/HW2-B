import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class RupinClient {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		Socket rpSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			rpSocket = new Socket("127.0.0.1", 4445);
			out = new PrintWriter(rpSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(rpSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: your host.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: your host.");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromServer;
		String fromUser;

		while ((fromServer = in.readLine()) != null) {
			System.out.println("Server: " + fromServer);
			if (fromServer.equals("Thanks"))
				break;

			fromUser = stdIn.readLine();
			if (fromUser != null) {
				System.out.println("Client: " + fromUser);
				out.println(fromUser);
			}
		}
		out.close();
		in.close();
		stdIn.close();
		rpSocket.close();

	}
}
