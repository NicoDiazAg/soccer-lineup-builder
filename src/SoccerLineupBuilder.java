//Authors: Nicolas Diaz-Aguilar & Vicente Lyon
//Main class for running the Soccer Lineup Builder App.
import java.net.Socket;

import javafx.application.Application;
import javafx.stage.Stage;

public class SoccerLineupBuilder extends Application {
	private SceneManager sceneManager;
	private Socket connection;

	public static void main(String[] args) {
		SoccerLineupBuilder builder = new SoccerLineupBuilder();
		launch(args);
	}

	public SoccerLineupBuilder() {
		this.sceneManager = new SceneManager();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		sceneManager.setStage(primaryStage);
		primaryStage.setTitle("Soccer Lineup Builder");
		sceneManager.setScene(SceneManager.SceneType.login);
		primaryStage.show();
	}

	@Override
	public void stop() {
		SceneManager.cleanup();
	}
}
