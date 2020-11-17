public class CSMazeGame {
    int[][] maze = new int[20][20];

    public static void main(String[] args) {
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

    public static void initializeMaze() {
        System.out.println("Works");
    }

    public static void updateMaze() {
    }

    public static void updateFrontEnd() {
    }
}