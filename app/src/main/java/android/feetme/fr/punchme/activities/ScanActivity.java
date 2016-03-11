package android.feetme.fr.punchme.activities;

import android.content.Context;
import android.content.Intent;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.fragments.BTGloveScanFragment;
import android.feetme.fr.punchme.fragments.GloveListsFragment;
import android.feetme.fr.punchme.fragments.GloveScanFragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by Anas on 19/02/2016.
 */
public class ScanActivity extends ServiceActivity implements GloveScanFragment.IScanFrameListener, GloveListsFragment.Listener {

    private static final String TAG_FRAGMENT = "ScanFragment";

    private boolean isScanFrameDisplayed;
    private IScanDismissListener mScanDismissListener;

    public static Intent newIntent(Context packageContext){
        return new Intent(packageContext, ScanActivity.class);
    }

    @Override
    public void onScanFrameShowed(boolean b) {
        isScanFrameDisplayed = b;
    }

    @Override
    public void onBackPressed() {
        if(isScanFrameDisplayed && mScanDismissListener != null){
            mScanDismissListener.dismiss();
        }else{
            previousActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("Gloves");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousActivity();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame, new BTGloveScanFragment())
                .commit();
    }

    private void previousActivity() {
        startActivity(PunchMapActivity.newIntent(this));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_top_out);
    }

    @Override
    public void onInsoleClick(long insoleId) {
        //TODO
    }
}
