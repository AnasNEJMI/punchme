package android.feetme.fr.punchme.gl;

import android.feetme.fr.punchme.dao.Glove;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Anas on 19/02/2016.
 */
public class PunchMapRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = PunchMapRenderer.class.getSimpleName();

    /**
     * Is false when the heatmap must not be drawn. True when the heatmap must be drawn.
     * When it is true, mglove must not be null.
     */
    private boolean isDrawing = false;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //TODO
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //TODO
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //TODO
    }











    public void enableDrawing(Glove glove){
        //TODO
    }

    public void disableDrawing(){
        isDrawing = false;
    }
}
