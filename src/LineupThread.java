//Authors: Nicolas Diaz-Aguilar & Vicente Lyon
//Soccer Lineup Builder Server Thread - Handles actions from both coaches to and from the server.
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LineupThread extends Thread {
	private final Socket coachSocket;
	private BufferedReader incoming;
	private PrintWriter outgoing;
	private static final ConcurrentHashMap<String, Player> serverPlayers = new ConcurrentHashMap<>();
	private static List<LineupThread> activeCoaches = Collections.synchronizedList(new ArrayList<>());
	private List<String> pendingLineupData = new ArrayList<>();
	private boolean isRunning = true;
	private String coachName;

	public LineupThread(Socket coachSocket) {
		this.coachSocket = coachSocket;
		loadInitialPlayers();
	}

	@Override
	public void run() {
		try {
			setupStreams();
			synchronized(activeCoaches) {
				activeCoaches.add(this);
			}

			String coachNameLine = incoming.readLine();
			if (coachNameLine != null && coachNameLine.equals("Coach name:")) {
				coachName = incoming.readLine();
				System.out.println("Coach connected: " + coachName);
			}

			processCoachCommands();
		} catch (IOException e) {
			System.out.println("Session error: " + e.getMessage());
		} finally {
			cleanup();
		}
	}

	//Loads the initial players for each thread.
	private void loadInitialPlayers() {
		synchronized(serverPlayers) {
			if (serverPlayers.isEmpty()) {
				try {
					HashMap<String, Player> loadedPlayers = PlayerXMLReader.readFile("SoccerPlayers.xml");
					serverPlayers.putAll(loadedPlayers);
				} catch (Exception e) {
					System.err.println("Error loading initial players: " + e.getMessage());
				}
			}
		}
	}

	//Setup streams.
	private void setupStreams() throws IOException {
		incoming = new BufferedReader(new InputStreamReader(coachSocket.getInputStream()));
		outgoing = new PrintWriter(coachSocket.getOutputStream(), true);
	}

	//Process commands.
	private void processCoachCommands() {
		try {
			String command;
			while (isRunning && (command = incoming.readLine()) != null) {
				handleCommand(command);
			}
		} catch (IOException e) {
			System.out.println("Error processing commands: " + e.getMessage());
		}
	}

	//Handle coach commands.
	private void handleCommand(String command) throws IOException {
		System.out.println("Received command from: " + coachName + ": " + command);

		switch (command) {
		case "GET_PLAYERS":
			sendPlayerList();
			break;
		case "ADD_PLAYER":
			handleAddPlayer();
			break;
		case "GET_ACTIVE_COACHES":
			sendActiveCoaches();
			break;
		case "SEND_LINEUP_TO":
			handleLineupSend();
			break;
		case "ACCEPT_LINEUP":
			handleLineupAccept();
			break;
		case "DECLINE_LINEUP":
			handleLineupDecline();
			break;
		case "REMOVE_PLAYER":
			handleRemovePlayer();
			break;
		case "QUIT":
			isRunning = false;
			break;
		}
	}

	//Sends player list.
	private void sendPlayerList() {
		try {
			outgoing.println("BEGIN_PLAYER_LIST");
			for (Player player : serverPlayers.values()) {
				outgoing.println(player.getNumber());
				outgoing.println(player.getName());
				outgoing.println(player.getPosition());
			}
			outgoing.println("END_PLAYER_LIST");
			outgoing.flush();
		} catch (Exception e) {
			System.err.println("Error sending player list: " + e.getMessage());
		}
	}

	//Adds a player from the list to the lineup.
	private void handleAddPlayer() throws IOException {
		String playerNumber = incoming.readLine();
		Player player = serverPlayers.get(playerNumber);
		if (player != null) {
			outgoing.println("SUCCESS");
			outgoing.println(player.getNumber());
			outgoing.println(player.getName());
			outgoing.println(player.getPosition());
			notifyOtherCoaches("PLAYERS_UPDATED");
		} else {
			outgoing.println("ERROR: Player not found");
		}
	}

	//Removes a player from the lineup.
	private void handleRemovePlayer() throws IOException {
		String playerNumber = incoming.readLine();
		if (serverPlayers.containsKey(playerNumber)) {
			outgoing.println("SUCCESS");
			notifyOtherCoaches("PLAYERS_UPDATED");
		} else {
			outgoing.println("ERROR: Player not found");
		}
	}

	//Notifies other coach commands applied.
	private void notifyOtherCoaches(String message) {
		synchronized(activeCoaches) {
			for (LineupThread coach : activeCoaches) {
				if (coach != this) {
					coach.outgoing.println(message);
				}
			}
		}
	}

	//Sends the list of active coaches using the app.
	private void sendActiveCoaches() {
		outgoing.println("ACTIVE_COACHES");
		synchronized(activeCoaches) {
			for (LineupThread coach : activeCoaches) {
				if (coach != this) {
					outgoing.println(coach.coachName);
				}
			}
		}
		outgoing.println("END_COACHES");
	}

	//Handles lineup offerings from coaches.
	private void handleLineupSend() throws IOException {
		String targetCoach = incoming.readLine();
		synchronized(activeCoaches) {
			for (LineupThread coach : activeCoaches) {
				if (coach.coachName.equals(targetCoach)) {
					coach.outgoing.println("LINEUP_OFFER");
					coach.outgoing.println(this.coachName);
					
					coach.pendingLineupData.clear();
					String line;
					while (!(line = incoming.readLine()).equals("END_LINEUP")) {
						coach.pendingLineupData.add(line);
					}
					break;
				}
			}
		}
	}
	
	//Handles lineup acceptance by sending a String that accepts it.
	private void handleLineupAccept() throws IOException {
		outgoing.println("LINEUP_RECEIVED");
		for (String data : pendingLineupData) {
			outgoing.println(data);
		}
		outgoing.println("END_LINEUP");
		outgoing.flush();
		pendingLineupData.clear();
	}
	
	//Handles lineup denial by sending a String that declines it.
	private void handleLineupDecline() {
		outgoing.println("LINEUP_DECLINED");
	}

	//Closes sockets.
	private void cleanup() {
		try {
			synchronized(activeCoaches) {
				activeCoaches.remove(this);
				for(LineupThread coach : activeCoaches) {
					coach.outgoing.println("COACH_DISCONNECTED");
					coach.outgoing.println(this.coachName);
				}
			}
			
			if (incoming != null) incoming.close();
			if (outgoing != null) outgoing.close();
			if (coachSocket != null) coachSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}