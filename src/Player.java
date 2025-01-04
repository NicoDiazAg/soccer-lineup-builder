//Author: Nicolas Diaz-Aguilar
//Contains each player's associated information and methods.
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javafx.scene.paint.Color;

public class Player {
	private int number;
	private String name;
	private String position;
	private Color playerColor = Color.WHITE;
	private double x;
	private double y;
	private Map<String, Boolean> coachPlayerOnFieldStatus = new HashMap<>();

	//Creates a player instance, sets position values to 0.0 by default
	public Player(int number, String name, String position) {
		this.number = number;
		this.name = name;
		this.position = position;
	}

	//Getters and setters.
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Color getPlayerColor() {
		return playerColor;
	}

	public void setPlayerColor(Color color) {
		this.playerColor = color;
	}

	public double getX() {
		return x;
	}

	public void setX(double xPosition) {
		this.x = xPosition;
	}

	public double getY() {
		return y;
	}

	public void setY(double yPosition) {
		this.y = yPosition;
	}

	public boolean isOnField(String coachName) {
		return coachPlayerOnFieldStatus.getOrDefault(coachName, false);
	}

	public void setOnField(boolean status, String coachName) {
		coachPlayerOnFieldStatus.put(coachName, status);
	}

	//Moves player's coordinates
	public void moveBy(double dx, double dy) {
		x += dx;
		y += dy;
	}

	//Checks if a point if within each player's circle
	public boolean containsPoint(double pointX, double pointY) {
		double radius = 25;
		double centerX = x + radius;
		double centerY = y + radius;
		return Math.pow(pointX - centerX, 2) + Math.pow(pointY - centerY, 2) <= Math.pow(radius, 2);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Player player = (Player) o;
		return number == player.number;
	}

	@Override
	public int hashCode() {
		return Objects.hash(number);
	}
}
