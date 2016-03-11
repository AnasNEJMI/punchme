package android.feetme.fr.punchme;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by Anas on 04/03/2016.
 */
public class Utils {

    ///////// ---- Floating action button (FAB) animations ---- /////////

    public static void scaleUp(Context context, FloatingActionButton floatingActionButton){
        floatingActionButton.setEnabled(true);
        Animation scaleAnim = AnimationUtils.loadAnimation(context, R.anim.anim_scale_up);
        scaleAnim.setStartOffset(100);
        scaleAnim.setFillAfter(true);
        floatingActionButton.startAnimation(scaleAnim);
    }

    public static void scaleDown(Context context, final FloatingActionButton floatingActionButton){
        floatingActionButton.setEnabled(false);
        Animation scaleAnim = AnimationUtils.loadAnimation(context, R.anim.anim_scale_down);
        scaleAnim.setFillAfter(true);
        floatingActionButton.startAnimation(scaleAnim);
    }

    public static void shake(Context context, FloatingActionButton floatingActionButton){
        Animation shakeAnim = AnimationUtils.loadAnimation(context, R.anim.shake);
        floatingActionButton.startAnimation(shakeAnim);
    }

}
