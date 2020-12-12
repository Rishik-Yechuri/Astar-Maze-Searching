import javax.print.attribute.HashDocAttributeSet;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.util.*;

public class CSMazeGame {
    //Entered by the user,and is used by the obstacle generation algorithim
    static int randomNumber = 0;
    //Keep maze stored in 2D array,so it can be used in the program
    static int[][] maze = new int[20][20];
    //Coord object that stores current position
    static Coord currentMove;
    //Stores the path(not the entire path,just the shortest one)
    static Stack visitStack;
    //Stores the entire path(including areas that were back tracked out of)
    static Queue<Coord> fullStack;
    //Stores whether the searching is over or not
    static boolean searchingDone = false;
    //Used for the helper,and danger blocks(so AI goes in an direction twice instead of once)
    static int timesToGo = 0;
    //Also for the danger and helper blocks. The number represents the direction
    static int directionToGo = 0;
    //HashMap<Coord, Integer> obstacleLocations = new HashMap<Coord, Integer>();
    //Visual side of things being declared
    static JFrame frame;
    static Canvas canvas;
    static BufferStrategy bufferStrategy;
    static Graphics2D g;
    //Bufferedreader for Maze data created
    static File file;
    static BufferedWriter br;
    //Values for clues and keys
    static HashMap<String, String> holdKeys = new HashMap<>();
    static boolean coordinateDecrypted = false;
    static AiObject mainAi;
    static Coord placeToGo = null;
    static ArrayList<Coord> holdDoors = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        //Used to take keyboard input
        Scanner scanner = new Scanner(System.in);
        //The file that the AI data is saved to
        file = new File("AiData");
        //Used for each game cycle's loop
        long startTime;
        long currentTime;
        br = new BufferedWriter(new FileWriter(file));
        for (int x = 1; x <= 3; x++) {
            //Gets user input and creates the maze
            System.out.print("What is the random number you would like to use(1-8):");
            randomNumber = scanner.nextInt();
            initializeMaze(x);
            //Sets the current time
            startTime = System.currentTimeMillis();
            while (!searchingDone) {
                //each cycle's execution time + sleeping is equal to 1000 millis
                currentTime = System.currentTimeMillis();
                long timePassed = currentTime - startTime;
                if (timePassed >= 500) {
                    updateMaze();
                    updateFrontEnd();
                    /*if (*//*All options have been explored or Maze has been completed*//*
                            false) {
                        searchingDone = true;
                    }*/
                    startTime = currentTime;
                }

            }
            //Resets the stuff for the next maze
            searchingDone = false;
            coordinateDecrypted = false;
            //Once the third maze is completed,it displays that the mazes are done
            if (x == 3) {
                g.clearRect(0, 0, 1000, 1000);
                g.setFont(new Font("TimesRoman", Font.PLAIN, 34));
                g.drawString("Mazes Completed", 350, 450);
            }
            //Saves the data of where the AI traveled to a file
            String entireStack = "";
            for (int p = 0; p < fullStack.size(); p++) {
                Coord tempCoord = fullStack.poll();
                String finalString = "(" + tempCoord.rPos + "," + tempCoord.cPos + ")";
                entireStack += finalString;
            }
            br.write("# of steps:" + fullStack.size() + " Visited Path:" + entireStack);
            br.newLine();
        }
        br.close();
    }

    //0 represents an empty space,1 represents a wall,and -1 represents the player
    //-2 represents blocks visited by the player
    //Future block like helped blocks or harmful blocks will use the same scheme
    public static void initializeMaze(int mazeNum) throws IOException {
        //Depending on the mazeNum parameter a different maze file is selected
        File file;
        file = mazeNum == 1 ? new File("Maze1") : mazeNum == 2 ? new File("Maze2") : new File("Maze3");
        //Creates a bufferedreader so data can be read from the maze file
        BufferedReader br = new BufferedReader(new FileReader(file));
        //Transfers data from the file to the 2D array
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                maze[y][x] = br.read() - 48;
                if (maze[y][x] == -38) {
                    maze[y][x] = br.read() - 48;
                }
            }
        }
        //Goes through the maze 2D array
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                //Check if there is a 2 by 2 empty area
                if (inBounds(y + 1, x + 1) && maze[y][x] == 0 && maze[y + 1][x] == 0 && maze[y][x + 1] == 0 && maze[y + 1][x + 1] == 0) {
                    //Depending on the random number the obstacle location is different
                    /* If the number is 1,the location is the top left of the 2 x 2 area,2 is bottom left
                    3 is bottom right,and 4 is top right. If random number is greater than 4,
                    then obstacle location is randomly selected between two blocks.
                    For 5,location is picked from left side. For 6,location is picked from bottom.
                    For 7,location is picked from right side. For 8 location is picked from the top.
                     */
                    if (randomNumber == 1) {
                        maze[y][x] = -3;
                    } else if (randomNumber == 2) {
                        maze[y + 1][x] = -3;
                    } else if (randomNumber == 3) {
                        maze[y + 1][x + 1] = -3;
                    } else if (randomNumber == 4) {
                        maze[y][x + 1] = -3;
                    } else {
                        int whichOne = (int) (Math.random() * 2);
                        ArrayList<Coord> randomPlaces = new ArrayList<>();
                        if (randomNumber == 5) {
                            randomPlaces.add(new Coord(y, x));
                            randomPlaces.add(new Coord(y + 1, x));
                        } else if (randomNumber == 6) {
                            randomPlaces.add(new Coord(y + 1, x));
                            randomPlaces.add(new Coord(y + 1, x + 1));
                        } else if (randomNumber == 7) {
                            randomPlaces.add(new Coord(y + 1, x + 1));
                            randomPlaces.add(new Coord(y, x + 1));
                        } else if (randomNumber == 8) {
                            randomPlaces.add(new Coord(y, x + 1));
                            randomPlaces.add(new Coord(y, x));
                        }
                        //-3 represents a obstacle,so the selected block is set to that
                        maze[randomPlaces.get(whichOne).rPos][randomPlaces.get(whichOne).cPos] = -3;
                    }
                }
            }
            //If the location value is 0 and is on the right side,it is set to as the starting point
            if (maze[y][19] == 0) {
                maze[y][19] = -1;
                currentMove = new Coord(y, 19);
            }
        }
        //Read from the file,to figure out where the clues are located
        br.readLine();
        String cluesString = br.readLine();
        //If there are clues place them on the map,and save them to a Hashmap
        if (cluesString != null) {
            //Splits the string by " ",meaning that dataBlocks stores all the clues
            String[] dataBlocks = cluesString.split(" ");
            //for each clue split it and get the details,save those to the Hashmap
            for (int x = 0; x < dataBlocks.length; x++) {
                String[] partsOfData = dataBlocks[x].split(",");
                //index 0 and 1 represent coordinates,while index 2 represents the data stored in the clue
                holdKeys.put(partsOfData[0] + "," + partsOfData[1], partsOfData[2]);
                //Depending on what clue type it is,maze is updated accordingly
                if (partsOfData[2].equals("ugottriked")) {
                    maze[Integer.valueOf(partsOfData[0])][Integer.valueOf(partsOfData[1])] = -4;
                } else if (partsOfData[2].length() < 3) {
                    maze[Integer.valueOf(partsOfData[0])][Integer.valueOf(partsOfData[1])] = -5;
                } else {
                    maze[Integer.valueOf(partsOfData[0])][Integer.valueOf(partsOfData[1])] = -6;
                }
            }
        }
        //Finds locations of doors from file
        String doorString = br.readLine();
        //If it isn't null add data to Arraylist
        if(doorString != null){
            //Stores all the coordinates that are doors
            String[] coordinateGroups = doorString.split(",");
            //For each door create a Coord() from the X and Y coord,then save that to an Arraylist
            for(int x=0;x<coordinateGroups.length;x++){
                String[] coords = coordinateGroups[x].split("\\.");
                holdDoors.add(new Coord(Integer.parseInt(coords[0]),Integer.parseInt(coords[1])));
            }
        }
        //Initialize the stacks
        visitStack = new Stack();
        fullStack = new LinkedList<>();
        visitStack.push(currentMove);
        //Initialize front end
        if (frame == null) {
            frame = new JFrame("Maze Path Finder");
            JPanel panel = (JPanel) frame.getContentPane();
            panel.setPreferredSize(new Dimension(1000, 1000));
            panel.setLayout(null);
            canvas = new Canvas();
            //put a boundry to the square
            canvas.setBounds(0, 0, 1000, 1000);
            canvas.setIgnoreRepaint(true);
            panel.add(canvas);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            canvas.createBufferStrategy(1);
            bufferStrategy = canvas.getBufferStrategy();
            canvas.requestFocus();
            g = (Graphics2D) bufferStrategy.getDrawGraphics();
        }
        //Create the AiObject(used for storing some data)
        mainAi = new AiObject();
    }

    //Moves the Ai forward one(unless goBackAlgorithim() is called)
    public static void updateMaze() throws InterruptedException {
        int oldPos = maze[currentMove.rPos][currentMove.cPos];
        Coord oldCoord = new Coord(currentMove.rPos, currentMove.cPos);
        //If the Ai is free the simulation is over
        if (currentMove.isFree()) {
            searchingDone = true;
        }//other wise try to move forward
        else if (!getMove(oldPos, oldCoord, true)) {
            //If that isn't possible it starts backtracking
            goBackAlgorithim();
        } else {
            //Update the Stacks
            currentMove = new Coord(currentMove.rPos, currentMove.cPos);
            visitStack.push(new Coord(currentMove.rPos, currentMove.cPos));
            fullStack.add(new Coord(currentMove.rPos, currentMove.cPos));
        }
        //Mark the current spot as visited
        maze[oldCoord.rPos][oldCoord.cPos] = -2;
    }
    //Keeps going back until Ai is able to move forward again(unexplored space)
    public static void goBackAlgorithim() throws InterruptedException {
        //Used to store whether or not it should keep going back
        boolean keepGoingBack = true;
        //Save the current values to be used later
        Coord oldLoc = currentMove;
        int oldValue = maze[currentMove.rPos][currentMove.cPos];
        //If keepGoingBack is true,go back
        while (keepGoingBack) {
            if (!visitStack.isEmpty()) {
                //removes one move from the stack
                visitStack.pop();
                if (!visitStack.isEmpty()) {
                    //The current move is reset
                    Coord tempCoord = (Coord) visitStack.peek();
                    currentMove = new Coord(tempCoord.rPos, tempCoord.cPos);//(Coord) visitStack.peek();
                    // maze[currentMove.rPos][currentMove.cPos] = -2;
                }
            } else {
                searchingDone = true;
                keepGoingBack = false;
            }
            //Checks if a move can be made from the current position,but doesn't make it
            if (getMove(maze[currentMove.rPos][currentMove.cPos], currentMove, false)) {
                //Stop going back
                keepGoingBack = false;
            }
            //Update the UI and sleep
            updateFrontEnd();
            Thread.sleep(485);
        }
        if (keepGoingBack) {
            Thread.sleep(485);
        }
        updateFrontEnd();
    }

    static boolean getMove(int valueOfSpot, Coord realCurrentMove, boolean takeAction)
    // This method checks eight possible positions in a counter-clock wise manner
    // starting with the (-1,0) position.  If a position is found the method returns
    // true and the currentMove coordinates are altered to the new position
    {
        //How much should the Ai move in each direction
        int rMovement = 0;
        int cMovement = 0;
        //Used for the loop
        int a = 0;
        boolean keepLoopRunning = true;
        boolean foundSomething = false;
        int currentValue = 0;
        if (coordinateDecrypted && currentMove.rPos == mainAi.getDecryptedCoord().rPos && currentMove.cPos == mainAi.getDecryptedCoord().cPos) {
            coordinateDecrypted = false;
        }
        if (inBounds(realCurrentMove.rPos, realCurrentMove.cPos)) {
            //Set the current value
            currentValue = maze[currentMove.rPos][currentMove.cPos];
        }
        //If the value is not a wall or empty space(greater than 1),or times to go is greater than 0,then code is executed
        if (currentValue > 1 || timesToGo > 0) {
            //If times to go is 0 set it to 2,meaning that another iteration is left
            if (timesToGo == 0) {
                timesToGo = 2;
                //Set the direction which can be used next iteration
                directionToGo = currentValue;
            } else {
                currentValue = directionToGo;
            }
            //Each value represents a direction(helps the Ai)
            if (currentValue > 1 && currentValue < 6) {
                switch (currentValue) {
                    case 2:
                        rMovement = -1;
                        break;
                    case 3:
                        cMovement = -1;
                        break;
                    case 4:
                        rMovement = 1;
                        break;
                    case 5:
                        cMovement = 1;
                        break;
                }
            }
            //Same as last if statement but represents a danger block(sends to wrong direction)
            if (currentValue > 5) {
                switch (currentValue) {
                    case 6:
                        rMovement = -1;
                        break;
                    case 7:
                        cMovement = -1;
                        break;
                    case 8:
                        rMovement = 1;
                        break;
                    case 9:
                        cMovement = 1;
                        break;
                }
            }
            timesToGo--;
            //Change the current position depending on value of bloack
            currentMove.rPos += rMovement;
            currentMove.cPos += cMovement;
            return true;
        }
        //Checks to see if the value falls in range of a clue value
        if (currentValue <= -4 && currentValue > -7) {
            //Gets the key value from a Hashmap
            String valueOfKey = holdKeys.get(currentMove.rPos + "," + currentMove.cPos);
            //Checks if key is a fake key,if so it is ignored
            if (!valueOfKey.equals("ugottriked")) {
                //If it is a key,add it as a key
                if (valueOfKey.length() < 3) {
                    if (mainAi.addKey(valueOfKey)) {
                        placeToGo = mainAi.getDecryptedCoord();
                        maze[placeToGo.rPos][placeToGo.cPos] = -7;
                        openDoors();
                        coordinateDecrypted = true;
                    }
                }//Otherwise add it as a coordinate
                else {
                    if (mainAi.addCoord(new Coord(Integer.valueOf(valueOfKey.split("\\.")[0]), Integer.valueOf(valueOfKey.split("\\.")[1])))) {
                        placeToGo = mainAi.getDecryptedCoord();
                        maze[placeToGo.rPos][placeToGo.cPos] = -7;
                        openDoors();
                        coordinateDecrypted = true;
                    }
                }
            }
        }
        //All possible directions Ai can go,stored as row movement,and column movement
        int[][] holdPossibleMoves = new int[][]{{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        //Holds all moves which are possible
        ArrayList<Coord> holdPossibleLocs = new ArrayList<>();
        while (keepLoopRunning) {
            //Sets the row and column movement based on a value
            rMovement = holdPossibleMoves[a][0];
            cMovement = holdPossibleMoves[a][1];
            int spaceValue = 1;
            //if the new position is inbounds,it's value is saved
            if (inBounds(currentMove.rPos + rMovement, currentMove.cPos + cMovement)) {
                spaceValue = maze[currentMove.rPos + rMovement][currentMove.cPos + cMovement];
            }
            //If there isn't a wall,or obsacle,or isn't previously explore
            if (inBounds(currentMove.rPos + rMovement, currentMove.cPos + cMovement) && spaceValue != 1 && spaceValue != -3 && spaceValue != -2/*&& spaceValue > -1*/) {
                //if the coordinate hasn't been decrypted and take action is true,move
                if (!coordinateDecrypted) {
                    if (takeAction) {
                        currentMove.rPos += rMovement;
                        currentMove.cPos += cMovement;
                    }
                    //return true means that a move has been found
                    return true;
                } else {
                    //Add it to the list of possible locs,which will only be used if coordinateDecrypted is true
                    holdPossibleLocs.add(new Coord(currentMove.rPos + rMovement, currentMove.cPos + cMovement));
                }
            }
            //Ends the loop since all possible spaces have been explored
            if (a >= 7) {
                keepLoopRunning = false;
            }
            a++;
        }
        //If the coordinate is decrypted,it looks for the one which gets it closest to the target
        if (coordinateDecrypted) {
            //Stores the top score and index of coord with that score
            int topScore = 0;
            int topScorePos = 0;
            //Gets the distance from current position to goal destination
            int mainDistance = Math.abs(mainAi.getDecryptedCoord().rPos - currentMove.rPos) + Math.abs(mainAi.getDecryptedCoord().cPos - currentMove.cPos);
            for (int x = 0; x < holdPossibleLocs.size(); x++) {
                //Get distance from coordinate from holdPossibleLocs,to final destination
                int currentDistance = Math.abs(mainAi.getDecryptedCoord().rPos - holdPossibleLocs.get(x).rPos) + Math.abs(mainAi.getDecryptedCoord().cPos - holdPossibleLocs.get(x).cPos);
                //Calculates how much closer to the target the new coordinate is
                int score = mainDistance - currentDistance;
                //If the score is the greatest so far,replace score with new score
                if (score > topScore) {
                    topScore = score;
                    topScorePos = x;
                }
            }
            //If there is at least one possible move,and takeAction is true make a move
            if (holdPossibleLocs.size() > 0) {
                if (takeAction) {
                    currentMove.rPos = holdPossibleLocs.get(topScorePos).rPos;
                    currentMove.cPos = holdPossibleLocs.get(topScorePos).cPos;
                }
                //return true to indicate that a move has been found
                return true;
            }
        }
        //set the value of the old space to traveled
        maze[realCurrentMove.rPos][realCurrentMove.cPos] = -2;
        return false;
    }

    // This method determines if a coordinate position is inbounds or not
    private static boolean inBounds(int r, int c)
    {
        if (r >= 0 && r < 20 && c >= 0 && c < 20) {
            return true;
        }
        return false;
    }

    //Open the blocked doors
    public static void openDoors(){
        for(int x=0;x<holdDoors.size();x++){
            maze[holdDoors.get(x).rPos][holdDoors.get(x).cPos] = 0;
        }
    }

    //Paint the UI using data from the Stack,and 2D array
    public static void updateFrontEnd() {
      /*long startTime;
        long currentTime;*/
       /* currentTime = System.currentTimeMillis();
        long timePassed = currentTime - startTime;
        if (timePassed >= 1000) {
            updateMaze();
            updateFrontEnd();
            if (*//*All options have been explored or Maze has been completed*//*
                    false) {
                searchingDone = true;
            }
            startTime = currentTime;
        }*/
        g.setColor(Color.WHITE);
        g.drawRect(0,0,1000,1000);
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                int mazeValue = maze[y][x];
                Color color = new Color(255, 255, 255);
                if (mazeValue == 1) {
                    color = new Color(0, 0, 0);
                } else if (mazeValue == 0) {
                    color = new Color(255, 255, 255);
                } /*else if (mazeValue == -1) {
                    color = new Color(20, 20, 220);
                } */ else if (mazeValue == -2) {
                    color = new Color(190, 150, 30);
                } else if (mazeValue > 1 && mazeValue < 6) {
                    color = new Color(15, 200, 20);
                } else if (mazeValue > 5) {
                    color = new Color(200, 15, 20);
                } else if (mazeValue == -3) {
                    color = new Color(102, 102, 51);
                } else if (mazeValue == -4) {
                    color = new Color(110, 110, 110);
                } else if (mazeValue == -5) {
                    color = new Color(0, 153, 51);
                } else if (mazeValue == -6) {
                    color = new Color(190, 210, 0);
                }
                if (y == currentMove.rPos && x == currentMove.cPos) {
                    color = new Color(20, 20, 220);
                }
                g.setColor(color);
                g.fillRect(x * 50, y * 50, 50, 50);
                if (mazeValue == -7) {
                    Color tempColor = new Color(180, 0, 70);
                    g.setColor(tempColor);
                    g.fillOval(x * 50 + 10, y * 50 + 10, 30, 30);
                }
            }
        }
    }
    public void updateFaster(){}
}