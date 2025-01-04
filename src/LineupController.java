//Author: Vicente Lyon
//Class for handling lineup-related actions such as requesting the initial player list, adding and removing players from the field.
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

public class LineupController {
	private LineupScene lineupScene;
	private Socket socket;
	private PrintWriter outgoing;
	private BufferedReader incoming;
	private List<String> activeCoaches = new ArrayList<>();
	private List<Player> selectedPlayers = new ArrayList<>();
	private List<Player> players = new ArrayList<>();
	private static final int MAX_PLAYERS = 11;
	private String coachName;

	public LineupController(LineupScene lineupScene) {
		this.lineupScene = lineupScene;
		this.socket = SceneManager.getSocket();
		this.coachName = SceneManager.getCoachName();
		setupSocketCommunication();
		initializeController();
	}

	//Sets up communication with socket.
	private void setupSocketCommunication() {
		try {
			outgoing = SceneManager.getOutgoing();
			incoming = SceneManager.getIncoming();
			startPlayerUpdateListener();
		} catch (Exception e) {
			lineupScene.showAlert("Connection Error", "Failed to setup socket communication");
		}
	}

	//Initializes controller by requesting initial player list.
	private void initializeController() {
		requestInitialPlayerList();
	}

	//Requests initial player list.
	private void requestInitialPlayerList() {
		try {
			outgoing.println("GET_PLAYERS");
			outgoing.flush();
		} catch (Exception e) {
			lineupScene.showAlert("Network Error", "Failed to request player list: " + e.getMessage());
		}
	}

	//Listens for commands and executes their respective actions.
	private void startPlayerUpdateListener() {
		new Thread(() -> {
			try {
				String line;
				while ((line = incoming.readLine()) != null) {
					if (line.equals("PLAYERS_UPDATED")) {
						requestInitialPlayerList();
					} else if (line.equals("BEGIN_PLAYER_LIST")) {
						handlePlayerListReceived();
					} else if (line.equals("ACTIVE_COACHES")) {
						List<String> coaches = new ArrayList<>();
						while (!(line = incoming.readLine()).equals("END_COACHES")) {
							coaches.add(line);
						}
						handleActiveCoaches(coaches);
					} else if (line.equals("LINEUP_OFFER")) {
						String fromCoach = incoming.readLine();
						handleLineupOffer(fromCoach);
					} else if (line.equals("LINEUP_RECEIVED")) {
						selectedPlayers.clear();
						while (!(line = incoming.readLine()).equals("END_LINEUP")) {
							int number = Integer.parseInt(line);
							double x = Double.parseDouble(incoming.readLine());
							double y = Double.parseDouble(incoming.readLine());

							for (Player p : players) {
								if (p.getNumber() == number) {
									p.setX(x);
									p.setY(y);
									p.setPlayerColor(lineupScene.getTeamColor());
									p.setOnField(true, coachName);
									selectedPlayers.add(p);
									break;
								}
							}
						}
						Platform.runLater(() -> {
							lineupScene.redrawField();
							lineupScene.refreshTable();
						});
					} else if (line.equals("COACH_DISCONNECTED")) {
						String disconnectedCoach = incoming.readLine();
						activeCoaches.remove(disconnectedCoach);
						Platform.runLater(() -> {
							lineupScene.showAlert("Coach Disconnected", disconnectedCoach + " has left the session.");
						});
					}
				}
			} catch (IOException e) {
				Platform.runLater(() -> lineupScene.showAlert("Connection Error", "Lost connection to server"));
			}
		}).start();
	}

	//Handles receiving a lineup.
	private void handlePlayerListReceived() throws IOException {
		List<Player> receivedPlayers = new ArrayList<>();
		String line;
		while (!(line = incoming.readLine()).equals("END_PLAYER_LIST")) {
			int number = Integer.parseInt(line);
			String name = incoming.readLine();
			String position = incoming.readLine();

			Player newPlayer = new Player(number, name, position);

			for (Player existingPlayer : players) {
				if (existingPlayer.getNumber() == number) {
					newPlayer.setOnField(existingPlayer.isOnField(coachName), coachName);
					break;
				}
			}
			receivedPlayers.add(newPlayer);
		}
		this.players = receivedPlayers;
		Platform.runLater(() -> lineupScene.updatePlayerTable(receivedPlayers));
	}

	//Adds a player to the field by changing its parameters and signaling LineupScene.
	public void addPlayerToField(Player player) {
		if (selectedPlayers.size() >= MAX_PLAYERS) {
			lineupScene.showAlert("Lineup Full", "You can only select " + MAX_PLAYERS + " players");
			return;
		}

		if (selectedPlayers.contains(player)) {
			lineupScene.showAlert("Duplicate Player", "This player is already in the lineup");
			return;
		}

		//Set initial position to the center of the field
		player.setX(lineupScene.getFieldCanvas().getWidth() / 2 - 25);
		player.setY(lineupScene.getFieldCanvas().getHeight() / 2 - 25);

		//Set player color
		player.setPlayerColor(lineupScene.getTeamColor());

		selectedPlayers.add(player);
		player.setOnField(true, coachName);

		//Notify GUI to draw player
		lineupScene.drawPlayerOnField(player);
		lineupScene.refreshTable();

		//Send add player request to server
		outgoing.println("ADD_PLAYER");
		outgoing.println(player.getNumber());
	}

	//Removes a player from the field.
	public void removePlayerFromField(Player player) {
		selectedPlayers.remove(player);
		player.setOnField(false, coachName);

		//Redraw field without the removed player
		lineupScene.redrawField();
		lineupScene.refreshTable();

		//Send remove player request to server
		outgoing.println("REMOVE_PLAYER");
		outgoing.println(player.getNumber());
	}

	//Saves a roster into a .txt file that starts with the respective coach's name.
	public void saveRoster() {
		if (selectedPlayers.size() != MAX_PLAYERS) {
			lineupScene.showAlert("Incomplete Lineup", "You must select exactly " + MAX_PLAYERS + " players");
			return;
		}

		try (PrintWriter writer = new PrintWriter(coachName + "_roster.txt")) {
			for (Player player : selectedPlayers) {
				writer.println(String.format("%d,%s,%s", 
						player.getNumber(), 
						player.getName(), 
						player.getPosition()));
			}
			lineupScene.showAlert("Save Successful", "Roster saved to " + coachName + "_roster.txt");
		} catch (IOException e) {
			lineupScene.showAlert("Save Error", "Failed to save roster");
		}
	}

	//Gets the list of active coaches before sending a lineup.
	public void sendLineup() {
		if (selectedPlayers.size() == MAX_PLAYERS) {
			outgoing.println("GET_ACTIVE_COACHES");
		} else {
			lineupScene.showAlert("Incomplete lineup", "You need 11 players to send a lineup.");
		}
	}

	//Displays available (connected) coaches.
	public void handleActiveCoaches(List<String> coaches) {
		this.activeCoaches = new ArrayList<>(coaches);
		Platform.runLater(() -> {
			coaches.remove(coachName);

			if (coaches.isEmpty()) {
				lineupScene.showAlert("No Coaches Available", "There are no other coaches currently connected.");
				return;
			}

			ChoiceDialog<String> dialog = new ChoiceDialog<>(coaches.get(0), coaches);
			dialog.setTitle("Send Lineup");
			dialog.setHeaderText("Select a coach to send your lineup to:");
			dialog.setContentText("Coach:");
			dialog.showAndWait().ifPresent(coach -> {
				if (activeCoaches.contains(coach)) {
				outgoing.println("SEND_LINEUP_TO");
				outgoing.println(coach);
				for (Player player : selectedPlayers) {
					outgoing.println(player.getNumber());
					outgoing.println(player.getX());
					outgoing.println(player.getY());
				}
				outgoing.println("END_LINEUP");
				} else {
					lineupScene.showAlert("Coach Unavailable", "The selected coach is no longer connected.");
				}
			});
		});
	}

	//Handler for receiving a lineup offer.
	public void handleLineupOffer(String fromCoach) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Lineup Offer");
			alert.setHeaderText("Lineup offer from " + fromCoach);
			alert.setContentText("Accept this lineup?");
			alert.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					selectedPlayers.clear();
					lineupScene.redrawField();
					outgoing.println("ACCEPT_LINEUP");
				} else {
					outgoing.println("DECLINE_LINEUP");
				}
			});
		});
	}

	//Getter for selectedPlayers.
	public List<Player> getSelectedPlayers() {
		return selectedPlayers;
	}

	//Updates a player's position on the soccer field canvas.
	public void updatePlayerPosition(Player player, double x, double y) {
		player.setX(x);
		player.setY(y);
		lineupScene.redrawField();
	}

	//Disconnects by closing the socket and cleaning the SceneManager.
	public void disconnect() {
		outgoing.println("QUIT");
		try {
			socket.close();
			SceneManager.cleanup();
			SceneManager.setScene(SceneManager.SceneType.login);
		} catch (IOException e) {
			lineupScene.showAlert("Disconnect Error", "Error while disconnecting");
		}
	}
}
