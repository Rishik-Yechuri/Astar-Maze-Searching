import java.util.ArrayList;
import java.util.HashMap;

public class AiObject {
    Coord coordToGoTo;
    int realKey = 0;
    public AiObject(){
    }
    //stores the key
    public boolean addKey(String key){
        if(key.length() < 3){
            realKey = Integer.parseInt(key);
            //if the coordinate has also been found,decrypt it with the key
            if(coordToGoTo != null){
                return decryptCoord();
            }
        }
        return  false;
    }
    //store the coord
    public boolean addCoord(Coord coord){
        coordToGoTo = new Coord(coord.rPos,coord.cPos);
        //if key has also been found,decrypt the key
        if(realKey != 0){
            return decryptCoord();
        }
        return false;
    }
    //use the key to decrypt the coord values
    public boolean decryptCoord(){
        coordToGoTo = new Coord(coordToGoTo.rPos + realKey,coordToGoTo.cPos + realKey);
        return true;
    }
    //return the decrypted coord
    public Coord getDecryptedCoord(){
        return coordToGoTo;
    }
}