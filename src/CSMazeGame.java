import javax.print.attribute.HashDocAttributeSet;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.util.*;

public class CSMazeGame {
    static int randomNumber = 0;
    static Coord previousMove;
    static int[][] maze = new int[20][20];
    static Coord currentMove;
    static Stack visitStack;
    static Queue<Coord> fullStack;
    static boolean searchingDone = false;
    static int timesToGo = 0;
    static int directionToGo = 0;
    HashMap<Coord, Integer> obstacleLocations = new HashMap<Coord, Integer>();
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
        Scanner scanner = new Scanner(System.in);
        long startTime;
        long currentTime;
        //boolean mazeCompleted = false;
        file = new File("AiData");
        br = new BufferedWriter(new FileWriter(file));
        //br.flush();
        for (int x = 1; x <= 3; x++) {
            System.out.print("What is the random number you would like to use(1-8):");
            randomNumber = scanner.nextInt();
            initializeMaze(x);
            startTime = System.currentTimeMillis();
            while (!searchingDone) {
                currentTime = System.currentTimeMillis();
                long timePassed = currentTime - startTime;
                if (timePassed >= 1000) {
                    updateMaze();
                    updateFrontEnd();
                    if (/*All options have been explored or Maze has been completed*/
                            false) {
                        searchingDone = true;
                    }
                    startTime = currentTime;
                }

            }
            searchingDone = false;
            coordinateDecrypted = false;
            if (x == 3) {
                g.clearRect(0, 0, 1000, 1000);
                g.setFont(new Font("TimesRoman", Font.PLAIN, 34));
                g.drawString("Mazes Completed", 350, 450);
            }
            //br.write(String.valueOf(fullStack) + " " + fullStack.size());
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
        File file;
        file = mazeNum == 1 ? new File("Maze1") : mazeNum == 2 ? new File("Maze2") : new File("Maze3");
        BufferedReader br = new BufferedReader(new FileReader(file));
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                maze[y][x] = br.read() - 48;
                if (maze[y][x] == -38) {
                    maze[y][x] = br.read() - 48;
                }
            }
        }
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                if (inBounds(y + 1, x + 1) && maze[y][x] == 0 && maze[y + 1][x] == 0 && maze[y][x + 1] == 0 && maze[y + 1][x + 1] == 0) {
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
                        maze[randomPlaces.get(whichOne).rPos][randomPlaces.get(whichOne).cPos] = -3;
                    }
                }
            }
            if (maze[y][19] == 0) {
                maze[y][19] = -1;
                currentMove = new Coord(y, 19);
            }
        }
        br.readLine();
        String cluesString = br.readLine();
        if (cluesString != null) {
            String[] dataBlocks = cluesString.split(" ");
            for (int x = 0; x < dataBlocks.length; x++) {
                String[] partsOfData = dataBlocks[x].split(",");
                holdKeys.put(partsOfData[0] + "," + partsOfData[1], partsOfData[2]);
                if (partsOfData[2].equals("ugottriked")) {
                    maze[Integer.valueOf(partsOfData[0])][Integer.valueOf(partsOfData[1])] = -4;
                } else if (partsOfData[2].length() < 3) {
                    maze[Integer.valueOf(partsOfData[0])][Integer.valueOf(partsOfData[1])] = -5;
                } else {
                    maze[Integer.valueOf(partsOfData[0])][Integer.valueOf(partsOfData[1])] = -6;
                }
                //  maze[Integer.valueOf(partsOfData[0])][Integer.valueOf(partsOfData[1])] = -4;
            }
        }
       // br.readLine();
        String doorString = br.readLine();
        if(doorString != null){
            String[] coordinateGroups = doorString.split(",");
            for(int x=0;x<coordinateGroups.length;x++){
                String[] coords = coordinateGroups[x].split("\\.");
                holdDoors.add(new Coord(Integer.parseInt(coords[0]),Integer.parseInt(coords[1])));
            }
        }
        //for(int y =0;y<20;y++){}
        visitStack = new Stack();
        fullStack = new LinkedList<>();
        visitStack.push(currentMove);
        //fullStack.add(currentMove);
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
        mainAi = new AiObject();
    }

    public static void updateMaze() throws InterruptedException {
        //boolean keepGoingBack = true;
        int oldPos = maze[currentMove.rPos][currentMove.cPos];
        Coord oldCoord = new Coord(currentMove.rPos, currentMove.cPos);
        //maze[currentMove.rPos][currentMove.cPos] = -2;
        //previousMove = currentMove;
        if (previousMove != null) {
            maze[previousMove.rPos][previousMove.cPos] = -2;
        }
        if (currentMove.isFree()) {
            searchingDone = true;
        } else if (!getMove(oldPos, oldCoord, true)) {
            goBackAlgorithim();
        } else {
            // maze[currentMove.rPos][currentMove.cPos] = -2;
            currentMove = new Coord(currentMove.rPos, currentMove.cPos);
            visitStack.push(new Coord(currentMove.rPos, currentMove.cPos));
            fullStack.add(new Coord(currentMove.rPos, currentMove.cPos));
        }
        maze[oldCoord.rPos][oldCoord.cPos] = -2;
        //maze[currentMove.rPos][currentMove.cPos] = -1;
    }

    public static void goBackAlgorithim() throws InterruptedException {
        //Do things that make it go back
        boolean keepGoingBack = true;
        Coord oldLoc = currentMove;
        int oldValue = maze[currentMove.rPos][currentMove.cPos];
        while (keepGoingBack) {
            if (!visitStack.isEmpty()) {
                visitStack.pop();
                if (!visitStack.isEmpty()) {
                    Coord tempCoord = (Coord) visitStack.peek();
                    currentMove = new Coord(tempCoord.rPos, tempCoord.cPos);//(Coord) visitStack.peek();
                    // maze[currentMove.rPos][currentMove.cPos] = -2;
                }
            } else {
                searchingDone = true;
                keepGoingBack = false;
            }
            if (getMove(maze[currentMove.rPos][currentMove.cPos], currentMove, false)) {
                keepGoingBack = false;
            }
            updateFrontEnd();
            Thread.sleep(950);
        }
        if (keepGoingBack) {
            Thread.sleep(950);
        }
        updateFrontEnd();
    }

    static boolean getMove(int valueOfSpot, Coord realCurrentMove, boolean takeAction)
    // This method checks eight possible positions in a counter-clock wise manner
    // starting with the (-1,0) position.  If a position is found the method returns
    // true and the currentMove coordinates are altered to the new position
    {
        int rMovement = 0;
        int cMovement = 0;
        int a = 0;
        boolean keepLoopRunning = true;
        boolean foundSomething = false;
        int currentValue = 0;
        if (coordinateDecrypted && currentMove.rPos == mainAi.getDecryptedCoord().rPos && currentMove.cPos == mainAi.getDecryptedCoord().cPos) {
            coordinateDecrypted = false;
        }
        if (inBounds(realCurrentMove.rPos, realCurrentMove.cPos)) {
            currentValue = /*valueOfSpot;*/maze[currentMove.rPos][currentMove.cPos];
        }
        if (currentValue > 1 || timesToGo > 0) {
            //keepLoopRunning = false;
            if (timesToGo == 0) {
                timesToGo = 2;
                directionToGo = currentValue;
            } else {
                currentValue = directionToGo;
            }
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
            currentMove.rPos += rMovement;
            currentMove.cPos += cMovement;
            return true;
        }
        if (currentValue <= -4 && currentValue > -7) {
            String valueOfKey = holdKeys.get(currentMove.rPos + "," + currentMove.cPos);
            if (!valueOfKey.equals("ugottriked")) {
                if (valueOfKey.length() < 3) {
                    if (mainAi.addKey(valueOfKey)) {
                        placeToGo = mainAi.getDecryptedCoord();
                        maze[placeToGo.rPos][placeToGo.cPos] = -7;
                        openDoors();
                        coordinateDecrypted = true;
                    }
                } else {
                    if (mainAi.addCoord(new Coord(Integer.valueOf(valueOfKey.split("\\.")[0]), Integer.valueOf(valueOfKey.split("\\.")[1])))) {
                        placeToGo = mainAi.getDecryptedCoord();
                        maze[placeToGo.rPos][placeToGo.cPos] = -7;
                        openDoors();
                        coordinateDecrypted = true;
                    }
                }
            }
        }
        int[][] holdPossibleMoves = new int[][]{{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        ArrayList<Coord> holdPossibleLocs = new ArrayList<>();
        ArrayList<Integer> locScore = new ArrayList<>();
        while (keepLoopRunning) {
            rMovement = holdPossibleMoves[a][0];
            cMovement = holdPossibleMoves[a][1];
            int spaceValue = 1;
            if (inBounds(currentMove.rPos + rMovement, currentMove.cPos + cMovement)) {
                spaceValue = maze[currentMove.rPos + rMovement][currentMove.cPos + cMovement];
            }
            if (inBounds(currentMove.rPos + rMovement, currentMove.cPos + cMovement) && spaceValue != 1 && spaceValue != -3 && spaceValue != -2/*&& spaceValue > -1*/) {
                if (!coordinateDecrypted) {
                    if (takeAction) {
                        currentMove.rPos += rMovement;
                        currentMove.cPos += cMovement;
                    }
                    return true;
                } else {
                    holdPossibleLocs.add(new Coord(currentMove.rPos + rMovement, currentMove.cPos + cMovement));
                }
            }
            if (a >= 7) {
                keepLoopRunning = false;
            }
            a++;
        }
        if (coordinateDecrypted) {
            int topScore = 0;
            int topScorePos = 0;
            int mainDistance = Math.abs(mainAi.getDecryptedCoord().rPos - currentMove.rPos) + Math.abs(mainAi.getDecryptedCoord().cPos - currentMove.cPos);
            for (int x = 0; x < holdPossibleLocs.size(); x++) {
                int currentDistance = Math.abs(mainAi.getDecryptedCoord().rPos - holdPossibleLocs.get(x).rPos) + Math.abs(mainAi.getDecryptedCoord().cPos - holdPossibleLocs.get(x).cPos);
                int score = mainDistance - currentDistance;
                if (score > topScore) {
                    topScore = score;
                    topScorePos = x;
                }
            }
            if (holdPossibleLocs.size() > 0) {
                if (takeAction) {
                    currentMove.rPos = holdPossibleLocs.get(topScorePos).rPos;
                    currentMove.cPos = holdPossibleLocs.get(topScorePos).cPos;
                }
                return true;
            }
        }
        maze[realCurrentMove.rPos][realCurrentMove.cPos] = -2;
        return false;
    }

    private static boolean inBounds(int r, int c)
    // This method determines if a coordinate position is inbounds or not
    {
        if (r >= 0 && r < 20 && c >= 0 && c < 20) {
            return true;
        }
        return false;
    }

    public static void openDoors(){
        for(int x=0;x<holdDoors.size();x++){
            maze[holdDoors.get(x).rPos][holdDoors.get(x).cPos] = 0;
        }
    }

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