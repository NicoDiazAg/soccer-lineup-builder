//Author: Nicolas Diaz-Aguilar
//Main GUI scene, has a LineupController to handle actions.
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.*;

public class LineupScene extends SceneBasic {
	private LineupController lineupController;
	private List<Player> players = new ArrayList<>();
	private List<Player> selectedPlayers = new ArrayList<>();
	private TableView<Player> playerTable;
	private Canvas fieldCanvas;
	private Player playerBeingDragged;
	private double prevDragX;
	private double prevDragY;
	private Color teamColor;
	private Button addPlayerButton, removePlayerButton, saveRosterButton, disconnectButton, sendLineupButton;

	public LineupScene() {
		super("Soccer Lineup Builder - " + SceneManager.getCoachName());
		root.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

		// Generate a random team color
		Color[] colors = {Color.RED, Color.BLUE, Color.BLUEVIOLET, Color.MAROON, Color.CHOCOLATE};
		teamColor = colors[(int) (Math.random() * colors.length)];

		// Initialize LineupController with this scene
		lineupController = new LineupController(this);

		setupGUI();
		setupDragAndDrop();
	}

	//Sets up the GUI.
	private void setupGUI() {
		//Create player table
		playerTable = new TableView<>();
		configurePlayerTableColumns();

		//Create field canvas
		fieldCanvas = new Canvas(300, 450);
		drawField();

		//Create buttons with updated action handlers
		createButtons();

		//Layout setup remains mostly the same
		VBox rightPanel = new VBox(15);
		rightPanel.setStyle("-fx-padding: 10;");
		rightPanel.getChildren().addAll(
				addPlayerButton, 
				removePlayerButton,
				sendLineupButton,
				saveRosterButton, 
				disconnectButton
				);

		HBox mainLayout = new HBox(20);
		mainLayout.setAlignment(Pos.CENTER);
		mainLayout.getChildren().addAll(
				playerTable, 
				fieldCanvas, 
				rightPanel
				);

		root.getChildren().add(mainLayout);

		//Setup table selection listener
		setupTableSelectionListener();
	}

	//Creates all necessary buttons for the GUI.
	private void createButtons() {
		addPlayerButton = new Button("Add Player to Field");
		addPlayerButton.setOnAction(e -> {
			Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
			if (selectedPlayer != null) {
				lineupController.addPlayerToField(selectedPlayer);
			}
		});

		removePlayerButton = new Button("Remove Player");
		removePlayerButton.setOnAction(e -> {
			Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
			if (selectedPlayer != null && selectedPlayer.isOnField(SceneManager.getCoachName())) {
				lineupController.removePlayerFromField(selectedPlayer);
			}
		});

		saveRosterButton = new Button("Save Roster");
		saveRosterButton.setOnAction(e -> lineupController.saveRoster());

		sendLineupButton = new Button("Send Lineup");
		sendLineupButton.setOnAction(e -> lineupController.sendLineup());

		disconnectButton = new Button("Disconnect");
		disconnectButton.setOnAction(e -> lineupController.disconnect());
	}

	//Configures columns in the player selector table.
	private void configurePlayerTableColumns() {
		TableColumn<Player, Integer> numberCol = new TableColumn<>("Number");
		numberCol.setCellValueFactory(cellData -> 
		new SimpleIntegerProperty(cellData.getValue().getNumber()).asObject());

		TableColumn<Player, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(cellData -> 
		new SimpleStringProperty(cellData.getValue().getName()));

		TableColumn<Player, String> positionCol = new TableColumn<>("Position");
		positionCol.setCellValueFactory(cellData -> 
		new SimpleStringProperty(cellData.getValue().getPosition()));

		TableColumn<Player, String> fieldStatusCol = new TableColumn<>("On Field");
		fieldStatusCol.setCellValueFactory(cellData -> 
		new SimpleStringProperty(cellData.getValue().isOnField(SceneManager.getCoachName()) ? "Yes" : "No"));

		playerTable.getColumns().addAll(numberCol, nameCol, positionCol, fieldStatusCol);
	}

	//Sets up a listener for the player selector table.
	private void setupTableSelectionListener() {
		playerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				addPlayerButton.setDisable(newSelection.isOnField(SceneManager.getCoachName()));
				removePlayerButton.setDisable(!newSelection.isOnField(SceneManager.getCoachName()));
			}
		});
	}

	//Updates player table.
	public void updatePlayerTable(List<Player> receivedPlayers) {
		players = receivedPlayers;
		playerTable.getItems().setAll(players);
	}

	//Refreshes the table
	public void refreshTable() {
		playerTable.refresh();
	}

	//Draws the soccer field, including penalty areas, middle circle, and borders.
	private void drawField() {
		GraphicsContext gc = fieldCanvas.getGraphicsContext2D();
		double width = fieldCanvas.getWidth();
		double height = fieldCanvas.getHeight();

		//Background
		gc.setFill(Color.GREEN);
		gc.fillRect(0, 0, width, height);

		//White lines
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(2);
		gc.strokeRect(0, 0, width, height);
		gc.strokeLine(0, height / 2, width, height / 2);

		//Center circle
		double centerCircleRadius = width / 5;
		gc.strokeOval(width / 2 - centerCircleRadius, height / 2 - centerCircleRadius, centerCircleRadius * 2, centerCircleRadius * 2);

		//Center dot
		gc.setFill(Color.WHITE);
		gc.fillOval(width / 2 - 3, height / 2 - 3, 6, 6);

		//Penalty areas
		double penaltyWidth = width * 0.6;
		double penaltyHeight = height * 0.2;
		gc.strokeRect((width - penaltyWidth) / 2, 0, penaltyWidth, penaltyHeight);
		gc.strokeRect((width - penaltyWidth) / 2, height - penaltyHeight, penaltyWidth, penaltyHeight);
	}

	//Draws a player on the field.
	public void drawPlayerOnField(Player player) {
		GraphicsContext gc = fieldCanvas.getGraphicsContext2D();
		double radius = 25;

		gc.setFill(player.getPlayerColor());
		gc.fillOval(player.getX(), player.getY(), radius * 2, radius * 2);

		gc.setFill(Color.WHITE);
		gc.setFont(new Font(16));
		gc.fillText(String.valueOf(player.getNumber()), player.getX() + radius - 6, player.getY() + radius + 6);
	}

	//Refreshes the soccer field.
	public void redrawField() {
		drawField();
		for (Player player : lineupController.getSelectedPlayers()) {
			drawPlayerOnField(player);
		}
	}

	//Sets up the property for dragging players around the soccer field canvas.
	private void setupDragAndDrop() {
		fieldCanvas.setOnMousePressed(e -> {
			double x = e.getX();
			double y = e.getY();

			for (Player player : lineupController.getSelectedPlayers()) {
				if (player.containsPoint(x, y)) {
					playerBeingDragged = player;
					prevDragX = x;
					prevDragY = y;
					break;
				}
			}
		});

		fieldCanvas.setOnMouseDragged(e -> {
			if (playerBeingDragged != null) {
				double x = e.getX();
				double y = e.getY();

				//Calculate movement
				double dx = x - prevDragX;
				double dy = y - prevDragY;

				//Update position
				double newX = playerBeingDragged.getX() + dx;
				double newY = playerBeingDragged.getY() + dy;

				//Keep player within canvas limits
				newX = Math.max(0, Math.min(newX,  fieldCanvas.getWidth() - 50));
				newY = Math.max(0, Math.min(newY,  fieldCanvas.getHeight() - 50));

				lineupController.updatePlayerPosition(playerBeingDragged, newX, newY);

				prevDragX = x;
				prevDragY = y;

				redrawField();
			}
		});

		fieldCanvas.setOnMouseReleased(e -> {
			playerBeingDragged = null;
		});
	}

	//Setter for selected players.
	public void setSelectedPlayers(List<Player> players) {
		this.selectedPlayers = new ArrayList<>(players);
	}

	//Getter for players that have been selected.
	public List<Player> getSelectedPlayers() {
		return new ArrayList<>(selectedPlayers);
	}

	//Getter for team color.
	public Color getTeamColor() {
		return teamColor;
	}

	//Getter for soccer field canvas.
	public Canvas getFieldCanvas() {
		return fieldCanvas;
	}

	//Method to show alerts.
	public void showAlert(String title, String content) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(content);
			alert.showAndWait();
		});
	}
}