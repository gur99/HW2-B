import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class KnockKnockServer {

	public void startKnockKnockServer() {
		ServerSocket serverSocket = null;
		int clientCounter = 1;

		try {
			serverSocket = new ServerSocket(4444);
			System.out.println("SERVER listening on PORT 4444");
		} catch (IOException e) {
			System.err.println("Could not listen on port: 4444.");
			System.exit(1);
		}

		while (true) {

//			Socket clientSocket = null;

			try {
				Socket connection = serverSocket.accept();
				System.out.println("Client connected" + clientCounter++);
				ConnectionHandler handler = new ConnectionHandler(connection);
				new Thread(handler).start();
//			clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.exit(1);
			}

		}

	}

	public static class ConnectionHandler implements Runnable {

		private Socket connection;

		public ConnectionHandler(Socket connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine, outputLine;
				KnockKnockProtocol kkp = new KnockKnockProtocol();
				outputLine = kkp.processInput(null);
				out.println(outputLine);
				while ((inputLine = in.readLine()) != null) {
					if (inputLine.equals("q")) {
						break;
					}
					outputLine = kkp.processInput(inputLine);
					out.println(outputLine);
				}
				out.close();
				in.close();
				connection.close();

			} catch (IOException ioe) {
				System.err.println("problem connecting to server");
			} finally {
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
