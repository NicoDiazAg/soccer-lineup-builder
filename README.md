# Soccer Lineup Builder
This Java application was our final project for our Software Development class. I worked on it with my classmate Vicente Lyon (https://github.com/VicenteLyon). We used EclipseIDE.

It consists of a Soccer Lineup Builder that accepts multiple simultaneos coach connections through the usage of threads. It allows coaches to select and add players from a preloaded XML file onto a soccer field canvas, where they can drag them and come up with a lineup that the can save afterwards.

To open and run it using Eclipse, please follow these steps:

Import all of the files in an empty project. Include all of the .java files and the SoccerPlayers.xml in the project. SoccerPlayers.xml includes a predetermined list of players for the application that can be modified if desired. Once every necessary file has been uploaded to the project, follow these steps to run the application:

1.- Run the LineupServer.java class. You should be able to see this line printed out in the console: 

![image](https://github.com/user-attachments/assets/95241a24-b7d6-409f-80f5-13bf27be0980)

2.- Run the SoccerLineupBuilder.java class. This class starts the program by setting a login scene. Here, you will enter a server address (in case you are running coach clients in the same device you can leave this as localhost), and the port, which for now we can leave as it is. Here you can enter your coach name, which will be displayed after in the main scene.

![image](https://github.com/user-attachments/assets/7234bfff-daf9-43f7-926b-7334a60f3797)

3.- Click connect. This will take you to the main soccer lineup builder scene. You will be able to see the main components of the GUI.

![image](https://github.com/user-attachments/assets/5e8c9f2e-e32c-4737-a8cc-51ee45779ee7)

4.- From here, you can start building your roster. To do so, click on a player that you would like to add to your lineup from the table on the left, and click on the “Add Player to Field” button on the right. This will display the selected player on the soccer field and you will be able to drag it around according to your preference. You can add up to 11 players, which is the soccer team size. To remove a player, select it from the table and click on the “Remove Player” button. This will clear it from the soccer field and update its on-field status on the table.

![image](https://github.com/user-attachments/assets/2b0bc353-d505-496d-a6cd-b7b579c70570)

5.- Once you have finished your 11-player lineup, you can send it to the other coach(es) you are connected with. To do this, click on the “Send Lineup” button on the right, and a pop-up will appear on your screen. Here, select a coach, and then “OK”. This will create a pop-up window on the other coach’s screen, from where they can accept or decline your lineup-sending request. If they decide to take it, it will load your lineup on their client side.

![image](https://github.com/user-attachments/assets/954ee2b5-f667-4633-90ec-08f12a588791)

6.- If the other coach doesn’t like the lineup or decides that other players would be a better fit on it, they can modify it and send the new lineup to the other coach(es) and vice versa, unlimited times! 

7.- Once a lineup is complete (11 players on it), each coach can save it to their device as a .xml file. To do this, click on the “Save Roster” button on the left. This will create a file with the coach’s name appended in the project folder.

8.- Pressing the “Disconnect” button will successfully end the coach connection with the server. Make sure that once every coach has disconnected, you stop the server too.
