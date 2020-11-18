class Coord
        // Coord is a class that stores a single maze location.
{
    public int rPos;
    public int cPos;

    public Coord(int r, int c) {
        rPos = r;
        cPos = c;
    }

    public boolean isFree() {
        return (rPos == 0 && cPos == 0);
    }

    public void setPos(int r, int c) {
        rPos += r;
        cPos += c;
    }
}