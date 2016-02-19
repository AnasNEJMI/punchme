package android.feetme.fr.punchme.controllers;

import android.feetme.fr.punchme.Glove;
import android.feetme.fr.punchme.gl.PunchMapRenderer;
import android.feetme.fr.punchme.gl.PunchMapView;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by Anas on 19/02/2016.
 */
public class PunchMapController implements IPunchMapController {

    private static final String TAG = PunchMapController.class.getSimpleName();
    private static final int REFRESH_PERIOD = 100; //ms = 10 Hz

    private PunchMapView mPunchMapView;
    private PunchMapRenderer mPunchMapRenderer;
    private View mBackground;



    public PunchMapController(PunchMapView punchMapView, @Nullable View bg){
        mPunchMapView = punchMapView;
        mPunchMapRenderer = mPunchMapView.getRenderer();
        mBackground = bg;
    }


    @Override
    public void onResume(){
        mPunchMapView.onResume();
    }

    @Override
    public void onPause(){
        mPunchMapView.onPause();
    }

    @Override
    public void startDrawing(Glove glove) {
        if(mBackground != null) {
            mBackground.setVisibility(View.INVISIBLE);
        }
        mPunchMapRenderer.enableDrawing(glove);
        mPunchMapView.setVisibility(View.VISIBLE);
        mPunchMapView.requestRender();
    }

    @Override
    public void stopDrawing() {
        mPunchMapRenderer.disableDrawing();
        mPunchMapView.requestRender();
        mPunchMapView.setVisibility(View.INVISIBLE);
        if(mBackground != null) {
            mBackground.setVisibility(View.VISIBLE);
        }

    }


    //TODO
}
