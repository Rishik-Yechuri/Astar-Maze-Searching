import java.io.*;
import java.util.Arrays;
import java.util.Stack;

public class CSMazeGame {
    static int[][] maze = new int[20][20];
    static Coord currentMove;
    static Stack visitStack;
    static boolean searchingDone = false;
    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime;
        long currentTime;
        //boolean mazeCompleted = false;
        initializeMaze();
        startTime = System.currentTimeMillis();
        while (!searchingDone) {
            currentTime = System.currentTimeMillis();
            long timePassed = currentTime-startTime;
            if(timePassed>=1000){
                updateMaze();
                updateFrontEnd();
                if (/*All options have been explored or Maze has been completed*/
                        false) {
                    searchingDone = true;
                }
                startTime = currentTime;
            }
        }
    }

    //0 represents an empty space,1 represents a wall,and -1 represents the player
    //-2 represents blocks visited by the player
    //Future block like helped blocks or harmful blocks will use the same scheme
    public static void initializeMaze() throws IOException {
        File file = new File("Maze1");
        BufferedReader br = new BufferedReader(new FileReader(file));
       for(int y=0;y<20;y++){
           for(int x=0;x<20;x++){
               maze[y][x] = br.read()-48;
               if(maze[y][x] == -38 ){
                   maze[y][x] = br.read()-48;
               }
           }
       }
       for(int y =0;y<20;y++){
           if(maze[y][19] == 0){
               maze[y][19] = -1;
               currentMove = new Coord(y,19);
           }
       }
       visitStack = new Stack();
       visitStack.push(currentMove);
       //currentMove = new Coord();
    }

    public static void updateMaze() throws InterruptedException {
        //boolean keepGoingBack = true;
        maze[currentMove.rPos][currentMove.cPos] = -2;
        if(currentMove.isFree()){
            searchingDone = true;
        }else if(!getMove()){
            goBackAlgorithim();
        }else{
            currentMove = new Coord(currentMove.rPos, currentMove.cPos);
            visitStack.push(currentMove);
        }
    }
    public static void goBackAlgorithim() throws InterruptedException {
        //Do things that make it go back
        boolean keepGoingBack = true;
        while (keepGoingBack) {
            Thread.sleep(950);
            if (!visitStack.isEmpty()) {
                visitStack.pop();
                if (!visitStack.isEmpty()) {
                    currentMove = (Coord) visitStack.peek();
                }
            } else {
                searchingDone = true;
                keepGoingBack = false;
            }
            if (getMove()) {
                keepGoingBack = false;
            }
            updateFrontEnd();
        }
        if (!visitStack.isEmpty()) {
            currentMove = (Coord) visitStack.peek();
        }
        updateFrontEnd();
    }
    static boolean getMove()
    // This method checks eight possible positions in a counter-clock wise manner
    // starting with the (-1,0) position.  If a position is found the method returns
    // true and the currentMove coordinates are altered to the new position
    {
        int rMovement = 0;
        int cMovement = 0;
        int a = 0;
        boolean keepLoopRunning = true;
        boolean foundSomething = false;
        int[][] holdPossibleMoves = new int[][]{{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        while (keepLoopRunning) {
            rMovement = holdPossibleMoves[a][0];
            cMovement = holdPossibleMoves[a][1];
            if (inBounds(currentMove.rPos + rMovement, currentMove.cPos + cMovement) && maze[currentMove.rPos + rMovement][currentMove.cPos + cMovement] == 0) {
                currentMove.rPos += rMovement;
                currentMove.cPos += cMovement;
                return true;
            }
            if (a >= 7) {
                keepLoopRunning = false;
            }
            a++;
        }
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
    public static void updateFrontEnd() {
    }
}