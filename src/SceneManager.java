//Author: Vicente Lyon
//Manages all of the scenes that are shown in the Soccer Lineup Builder App.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import javafx.stage.Stage;

public class SceneManager {
	public static enum SceneType {login, lineup, playerList}
	private static HashMap<SceneType, SceneBasic> scenes = new HashMap<>();
	private static Socket connection;
	private static Stage stage;
	private static BufferedReader incoming;
	private static PrintWriter outgoing;
	private static String coachName;

	public SceneManager() {
		scenes.put(SceneType.login, new LoginScene());
	}
	
	//Getters and setters.

	public static void setSocket(Socket setConnection) throws IOException {
		connection = setConnection;
		if (connection != null) {
			incoming = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			outgoing = new PrintWriter(connection.getOutputStream(), true);
		}
	}

	public static BufferedReader getIncoming() {
		return incoming;
	}

	public static PrintWriter getOutgoing() {
		return outgoing;
	}

	public static Socket getSocket() {
		if (connection != null && connection.isClosed()) {
			connection = null;
			incoming = null;
			outgoing = null;
		}
		return connection;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public static void setCoachName(String name) {
		coachName = name;
	}

	public static String getCoachName() {
		return coachName;
	}

	//Setter for scene.
	public static void setScene(SceneType type) {
		try {
			System.out.println("Switching to scene: " + type);

			if (!scenes.containsKey(type)) {
				switch (type) {
				case lineup:
					scenes.put(type, new LineupScene());
					break;
				default:
					break;
				}
			}

			if (stage == null || scenes.get(type) == null) {
				System.out.println("ERROR: Stage or scene is null");
				return;
			}

			stage.setScene(scenes.get(type).getScene());

			//Set sizes based on scene type
			if(type == SceneType.lineup) {
				stage.setWidth(900);
				stage.setHeight(600);
			} else {
				stage.setWidth(300);
				stage.setHeight(300);
			}
		} catch (Exception e) {
			System.out.println("Scene switch error: " + e);
		}
	}

	//Getter for scene.
	public static SceneBasic getScene(SceneType type) {
		return scenes.get(type);
	}

	//Sets streams to null.
	public static void cleanup() {
		try {
			if (outgoing != null) {
				outgoing.println("QUIT");
				outgoing.flush();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			connection = null;
			incoming = null;
			outgoing = null;

			SceneType[] types = SceneType.values();
			for (SceneType type : types) {
				if (type != SceneType.login) {
					scenes.remove(type);
				}
			}
		}
	}
}
