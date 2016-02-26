package android.feetme.fr.punchme.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.managers.GloveManager;
import android.feetme.fr.punchme.managers.IGloveManager;
import android.feetme.fr.punchme.utils.IFrameSubscriber;
import android.feetme.fr.punchme.utils.PreferenceUtils;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Anas on 19/02/2016.
 */
public class MainService extends Service implements IMainServiceManager{

    private static final String TAG = MainService.class.getSimpleName();

    private static final String PREF_CONNECTION_STARTED = "service_status";
    private static final String PREF_RECORDING_STARTED = "recording_status";

    private Binder mBinder = new MainBinder();

    private Glove mGloveLeft;
    private Glove mGloveRight;

    private IGloveManager mManagerLeft;
    private IGloveManager mManagerRight;


    private volatile boolean isConnectionStarted;

    private BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF){
                    onBluetoothOff();
                }
            }
        }
    };

    private void onBluetoothOff() {
        Log.d(TAG, "Bluetooth Off, stopping connection");
        stopConnection();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "FeetmeService started");

        if (isConnectionPreferenceOn()) {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                if (!isConnectionStarted) {
                    startConnection();
                }
            } else {
                Log.d(TAG, "Bluetooth Off");
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getBaseContext();
        mGloveLeft = PreferenceUtils.getGlove(context, GloveFactory.SIDE_LEFT);
        mGloveRight = PreferenceUtils.getGlove(context, GloveFactory.SIDE_RIGHT);
        isConnectionStarted = false;
    }

    @Override
    public void onDestroy() {
        stopConnection();
        super.onDestroy();
    }

    public class MainBinder extends Binder {
        public IMainServiceManager getMainManager(){
            return MainService.this;
        }
    }

    protected IGloveManager newSideService(Context context, Glove glove){
        return new GloveManager.Builder(context, glove)
                .build();
    }

    private boolean isConnectionPreferenceOn(){
        return PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .getBoolean(PREF_CONNECTION_STARTED, false);
    }

    private void setConnectionPreference(boolean on){
        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .edit()
                .putBoolean(PREF_CONNECTION_STARTED, on)
                .commit();
    }

    @Override
    public synchronized void startConnection() {
        Log.d(TAG, "startConnection");
        isConnectionStarted = true;
        setConnectionPreference(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, filter);

        if(mManagerLeft == null && mGloveLeft != null){
            mManagerLeft = newSideService(getBaseContext(), mGloveLeft);
            mManagerLeft.startConnection();
        }

        if(mManagerRight == null && mGloveRight != null){
            mManagerRight = newSideService(getBaseContext(), mGloveRight);
            mManagerRight.startConnection();
        }
    }

    @Override
    public synchronized void stopConnection() {
        Log.d(TAG, "stopConnection");

        if(isConnectionStarted) {
            unregisterReceiver(mBluetoothStateReceiver);
        }

        if(mManagerLeft != null) mManagerLeft.stopConnection();
        mManagerLeft = null;

        if(mManagerRight != null) mManagerRight.stopConnection();
        mManagerRight = null;

        isConnectionStarted = false;
        setConnectionPreference(false);
    }

    @Override
    public boolean isConnectionStarted() {
        return isConnectionStarted;
    }

    @Override
    public boolean isConnected(int side) {
        if(side == GloveFactory.SIDE_LEFT){
            return mManagerLeft != null && mManagerLeft.isConnected();
        }else{
            return mManagerRight != null && mManagerRight.isConnected();
        }
    }

    @Override
    public Glove getGlove(int side) {
        if(side == GloveFactory.SIDE_LEFT){ return mGloveLeft; }
        else{ return mGloveRight; }
    }

    @Override
    public void registerFrameSubscriber(IFrameSubscriber subscriber, int side) {
        if(side == GloveFactory.SIDE_LEFT){
            if(mManagerLeft != null) mManagerLeft.subscribe(subscriber);
        }else{
            if(mManagerRight != null) mManagerRight.subscribe(subscriber);
        }
    }

    @Override
    public void unregisterFrameSubscriber(IFrameSubscriber subscriber, int side) {
        if(side == GloveFactory.SIDE_LEFT){
            if(mManagerLeft != null) mManagerLeft.unsubscribe(subscriber);
        }else{
            if(mManagerRight != null) mManagerRight.unsubscribe(subscriber);
        }
    }

    @Override
    public void requestCalibration(int side, IGloveManager.CalibrationCallback callback) {
        if(side == GloveFactory.SIDE_LEFT){
            mManagerLeft.requestCalibration(callback);
        }else{
            mManagerRight.requestCalibration(callback);
        }
    }

    @Override
    public boolean startCalibration(int side) {
        if(side == GloveFactory.SIDE_LEFT){
            return mManagerLeft != null && mManagerLeft.startCalibration(null);
        }else{
            return mManagerRight != null && mManagerRight.startCalibration(null);
        }
    }

    @Override
    public boolean stopCalibration(int side) {
        if(side == GloveFactory.SIDE_LEFT){
            return mManagerLeft != null && mManagerLeft.stopCalibration();
        }else{
            return mManagerRight != null && mManagerRight.stopCalibration();
        }
    }

    @Override
    public void cancelCalibration(int side) {
        if(side == GloveFactory.SIDE_LEFT){
            if(mManagerLeft != null) mManagerLeft.cancelCalibration();
        }else{
            if(mManagerRight != null) mManagerRight.cancelCalibration();
        }
    }

    @Override
    public boolean isCalibrating(int side) {
        if(side == GloveFactory.SIDE_LEFT){
            return mManagerLeft != null && mManagerLeft.isCalibrating();
        }else{
            return mManagerRight != null && mManagerRight.isCalibrating();
        }
    }

}
