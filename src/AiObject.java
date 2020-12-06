import java.util.ArrayList;
import java.util.HashMap;

public class AiObject {
    Coord coordToGoTo;
    int realKey = 0;
    public AiObject(){
    }
    public boolean addKey(String key){
        if(key.length() < 3){
            realKey = Integer.parseInt(key);
            if(coordToGoTo != null){
                return decryptCoord();
            }
        }
        return  false;
    }
    public boolean addCoord(Coord coord){
        coordToGoTo = new Coord(coord.rPos,coord.cPos);
        if(realKey != 0){
            return decryptCoord();
        }
        return false;
    }
    public boolean decryptCoord(){
        coordToGoTo = new Coord(coordToGoTo.rPos + realKey,coordToGoTo.cPos + realKey);
        return true;
    }
    public Coord getDecryptedCoord(){
        return coordToGoTo;
    }
}