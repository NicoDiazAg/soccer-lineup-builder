//Author: Vicente Lyon
//Basic scene class serves as a template for GUI scenes.
import java.io.PrintWriter;
import java.net.Socket;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SceneBasic {
	protected Scene scene;
	protected VBox root = new VBox(10);

	public SceneBasic(String titleText) {
		Label title = new Label(titleText);
		title.setFont(new Font(24));
		root.getChildren().add(title);
		root.setAlignment(Pos.TOP_CENTER);
		root.setPadding(new Insets(20));
		scene = new Scene(root);
	}

	//Getter for scene.
	public Scene getScene() {
		return scene;
	}

	//Disconnects and sets login scene.
	protected void disconnect() {
		try {
			System.out.println("Disconnecting...");
			Socket connection = SceneManager.getSocket();
			if (connection != null) {
				PrintWriter outgoing = new PrintWriter(connection.getOutputStream());
				outgoing.println("QUIT");
				outgoing.flush();
				SceneManager.setSocket(null);
			}
			SceneManager.setScene(SceneManager.SceneType.login);
		} catch (Exception e) {
			System.out.println("Disconnect error: " + e);
		}
	}
}
