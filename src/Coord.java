class Coord
        // Coord is a class that stores a single maze location.
{
    //stores the row and column
    public int rPos;
    public int cPos;

    //Initialize location values
    public Coord(int r, int c) {
        rPos = r;
        cPos = c;
    }

    //if the value is top left corner,return true
    public boolean isFree() {
        return (rPos == 0 && cPos == 0);
    }
}