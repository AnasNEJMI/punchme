package android.feetme.fr.punchme.service;

import android.app.Service;
import android.content.Intent;
import android.feetme.fr.punchme.Glove;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Created by Anas on 19/02/2016.
 */
public class MainService extends Service implements IMainServiceManager{

    private static final String TAG = MainService.class.getSimpleName();

    private Binder mBinder = new MainBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void startConnection() {
        //TODO
    }

    @Override
    public void stopConnection() {
        //TODO
    }

    @Override
    public boolean isConnectionStarted() {
        //TODO
        return false;
    }

    @Override
    public boolean isConnected() {
        //TODO
        return false;
    }

    @Override
    public Glove getGlove() {
        //TODO
        return null;
    }

    public class MainBinder extends Binder {
        public IMainServiceManager getMainManager(){
            return MainService.this;
        }
    }
}
