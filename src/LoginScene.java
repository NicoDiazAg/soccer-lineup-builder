//Author: Vicente Lyon
//Login Scene

import java.io.PrintWriter;
import java.net.Socket;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LoginScene extends SceneBasic {
	private TextField nameField;
	private TextField serverField;
	private TextField portField;
	private Label statusLabel;

	public LoginScene() {
		super("Soccer Lineup Builder");

		//For server input
		HBox serverBox = new HBox(10);
		serverBox.setAlignment(Pos.CENTER);
		serverField = new TextField("localhost");
		serverField.setPromptText("Server address");
		serverBox.getChildren().addAll(new Label("Server:"), serverField);

		//For port input
		HBox portBox = new HBox(10);
		portBox.setAlignment(Pos.CENTER);
		portField = new TextField("35007");
		portField.setPromptText("Port number");
		portBox.getChildren().addAll(new Label("Port:"), portField);

		//Coach name input
		HBox nameBox = new HBox(10);
		nameBox.setAlignment(Pos.CENTER);
		nameField = new TextField();
		nameField.setPromptText("Enter coach name");
		nameBox.getChildren().addAll(new Label("Name:"), nameField);

		//Status label for feedback
		statusLabel = new Label("");
		statusLabel.setStyle("-fx-text-fill: red;");

		//Connect button
		Button connectButton = new Button("Connect");
		connectButton.setMinWidth(200);
		connectButton.setOnAction(e -> connect());

		root.getChildren().addAll(serverBox, portBox, nameBox, statusLabel, connectButton);
	}

	//Establishes connection for coach.
	private void connect() {
		try {
			String server = serverField.getText();
			int port = Integer.parseInt(portField.getText());
			String coachName = nameField.getText().trim();

			if (coachName.isEmpty()) {
				statusLabel.setText("Please enter a coach name!");
				return;
			}
			Socket socket = new Socket(server, port);
			SceneManager.setSocket(socket);
			SceneManager.setCoachName(coachName);

			PrintWriter out = SceneManager.getOutgoing();
			out.println("Coach name:");
			out.println(coachName);
			out.flush();

			SceneManager.setScene(SceneManager.SceneType.lineup);

		} catch (Exception e) {
			statusLabel.setText("Connection failed: " + e.getMessage());
			System.out.println("Connection error: " + e);
		}
	}
}
