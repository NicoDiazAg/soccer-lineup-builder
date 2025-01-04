//Author: Vicente Lyon
//Reads SoccerPlayers.xml

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class PlayerXMLReader {
	public static HashMap<String, Player> readFile(String filename) {
		HashMap<String, Player> players = new HashMap<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			File file = new File(filename);
			if (!file.exists()) {
				System.err.println("XML file not found: " + filename);
				return players;
			}

			Document doc = db.parse(file);
			NodeList list = doc.getElementsByTagName("PLAYER");

			for (int temp = 0; temp < list.getLength(); temp++) {
				Node node = list.item(temp);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String NUMBER = element.getElementsByTagName("NUMBER").item(0).getTextContent().trim();
					String NAME = element.getElementsByTagName("NAME").item(0).getTextContent().trim();
					String POSITION = element.getElementsByTagName("POSITION").item(0).getTextContent().trim();

					try {
						int number = Integer.parseInt(NUMBER);
						Player player = new Player(number, NAME, POSITION);
						players.put(NUMBER, player);
					} catch (NumberFormatException e) {
						System.err.println("Invalid player number format for player: " + NAME);
					}
					System.out.println("------- Found Player -------");
					System.out.println("Number: " + NUMBER);
					System.out.println("Name: " + NAME);
					System.out.println("Position: " + POSITION);
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return players;
	}
}