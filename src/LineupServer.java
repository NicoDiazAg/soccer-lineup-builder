//Author: Nicolas Diaz-Aguilar & Vicente Lyon
//Server for Soccer Lineup Builder App.
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class LineupServer {
	private static final int LISTENING_PORT = 35007;
	private static List<Player> players = new ArrayList<>();

	public static void main(String[] args) {
		//Load all players from XML once server starts
		loadPlayersFromXML();

		try (ServerSocket serverSocket = new ServerSocket(LISTENING_PORT)) {
			System.out.println("Soccer Lineup Builder Server listening on port: " + LISTENING_PORT);

			while (true) {
				Socket coachConnection = serverSocket.accept();
				System.out.println("New coach connected -> IP Address: " + coachConnection.getInetAddress());

				//Create and start a new thread for each coach
				LineupThread coachThread = new LineupThread(coachConnection);
				coachThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Use existing PlayerXMLReader to load players from XML file.
	private static void loadPlayersFromXML() {
		players = new ArrayList<>(PlayerXMLReader.readFile("SoccerPlayers.xml").values());
	}
}