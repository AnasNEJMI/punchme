package android.feetme.fr.punchme.controllers;

import android.feetme.fr.punchme.Glove;

/**
 * Created by Anas on 19/02/2016.
 */
public interface IPunchMapController {

    void onResume();

    void onPause();

    void startDrawing(Glove glove);

    void stopDrawing();

}
