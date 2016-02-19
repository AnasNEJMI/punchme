package android.feetme.fr.punchme.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by Anas on 19/02/2016.
 */
public class PunchMapView extends GLSurfaceView {

    private PunchMapRenderer mRenderer;

    public PunchMapView(Context context) {
        super(context);
        init(context);
    }

    public PunchMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

    }

    public PunchMapRenderer getRenderer(){
        return mRenderer;
    }

}
