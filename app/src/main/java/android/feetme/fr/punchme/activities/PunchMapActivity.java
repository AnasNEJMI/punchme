package android.feetme.fr.punchme.activities;

import android.content.Context;
import android.content.Intent;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.fragments.PunchMapFragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Anas on 19/02/2016.
 */
public class PunchMapActivity extends ServiceActivity {

    public static Intent newIntent(Context packageContext){
        return new Intent(packageContext, PunchMapActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame, new PunchMapFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_punchmap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_select_glove){
            startActivity(ScanActivity.newIntent(this));
            overridePendingTransition(R.anim.fade_top_in, R.anim.fake_anim);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
}
