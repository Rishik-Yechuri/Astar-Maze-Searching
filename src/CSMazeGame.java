import java.io.*;
import java.util.Arrays;

public class CSMazeGame {
    static int[][] maze = new int[20][20];

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        boolean mazeCompleted = false;
        initializeMaze();
        while (!mazeCompleted) {
            updateMaze();
            updateFrontEnd();
            if (/*All options have been explored or Maze has been completed*/
                    false) {
                mazeCompleted = true;
            }
        }
    }

    //0 represents an empty space,1 represents a wall,and -1 represents the player
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
           }
       }
    }

    public static void updateMaze() {
    }

    public static void updateFrontEnd() {
    }
}