package android.feetme.fr.punchme.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.feetme.fr.punchme.service.IMainServiceManager;
import android.feetme.fr.punchme.service.MainService;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Anas on 19/02/2016.
 */
public class ServiceActivity extends AppCompatActivity {

    private static final String TAG = ServiceActivity.class.getSimpleName();

    protected IMainServiceManager mMainManager;

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            MainService.MainBinder b = (MainService.MainBinder) binder;
            mMainManager = b.getMainManager();
        }

        public void onServiceDisconnected(ComponentName className) {
            mMainManager = null;
        }
    };

    private void bindService(){
        Intent service = newServiceIntent();
        startService(service);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService(){
        unbindService(mConnection);

        if(!isServiceOn()){
            stopService(newServiceIntent());
        }
    }

    public boolean isServiceOn(){
        return mMainManager != null && mMainManager.isConnectionStarted();
    }

    protected Intent newServiceIntent() {
        return new Intent(this, MainActivity.class);
    }

}
