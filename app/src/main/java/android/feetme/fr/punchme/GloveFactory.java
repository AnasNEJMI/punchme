package android.feetme.fr.punchme;

import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.exceptions.GloveArgumentException;

/**
 * Created by Anas on 26/02/2016.
 */
public class GloveFactory {

    private static final String TAG = GloveFactory.class.getSimpleName();

    public static final int OVERHEAD = 5;

    public static final int SIDE_LEFT = 1;
    public static final int SIDE_RIGHT = 2;

    public static final int MAX_SENSOR_VALUE = 224;


    /**
     * @param address MAC address of the glove.
     * @param name Name of the glove "FeetMe [number]-[size]"
     * @return A new glove instance.
     * @throws android.feetme.fr.punchme.exceptions.GloveArgumentException
     */
    public static Glove newInstance(String address, String name){

        if(address == null){
            throw new GloveArgumentException("address is null");
        }

        Glove glove = new Glove();
        glove.setAddress(address);
        glove.setName(name);
        glove.setSide(getSide(name));
        //glove.setSensorNb(getSensorNb(glove.getSize()));

        return glove;
    }

    private static int getSide(String name) {
        int rIndex = name.indexOf('R');
        int lIndex = name.indexOf('L');

        if(rIndex == -1 && lIndex == -1){
            throw new GloveArgumentException("No side in name");
        }else if(rIndex != -1 && lIndex != -1){
            return lIndex < rIndex ? SIDE_LEFT : SIDE_RIGHT;
        }else{
            return lIndex > rIndex ? SIDE_LEFT : SIDE_RIGHT;
        }
    }
}
