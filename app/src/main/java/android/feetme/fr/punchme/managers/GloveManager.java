package android.feetme.fr.punchme.managers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.feetme.fr.punchme.calibration.Calibrator;
import android.feetme.fr.punchme.controllers.GloveController;
import android.feetme.fr.punchme.controllers.IGloveController;
import android.feetme.fr.punchme.dao.DaoAccess;
import android.feetme.fr.punchme.dao.DaoSession;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.dao.GloveDao;
import android.feetme.fr.punchme.utils.IFrameSubscriber;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Anas on 19/02/2016.
 */
public class GloveManager implements IGloveManager, IGloveController.Listener {

    private static final String TAG = GloveManager.class.getSimpleName();

    private final Context mContext;
    private final Glove mGlove;

    private IGloveController mGloveController;
    private CalibrationCallback mCalibrationCallback;
    private boolean isServiceOn = true;

    private final List<IFrameSubscriber> mSubscribers = new CopyOnWriteArrayList<>();

    /*
     * Manage reconnections.
     * On disconnection, if it is the first reconnection attempt, don't wait long.
     * Otherwise, wait longer seconds.
     */
    private boolean isFirstReconnectionAttempted = false;
    protected static final int CONNECTION_WHAT = 300;
    protected static final int CONNECTION_DELAY_1 = 3000;
    protected static final int CONNECTION_DELAY_2 = 30000;

    private Calibrator mCalibrator;

    private Handler mConnectionTimer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isBluetoothEnabled() && mGloveController != null) {
                mGloveController.connect();
            }
        }
    };

    protected GloveManager(Builder builder){

        mContext = builder.context;
        mGlove = builder.glove;

        if(mContext == null) throw  new IllegalStateException("Context cannot be null");
        if(mGlove == null) throw  new IllegalStateException("Insole cannot be null");

        if(mGlove.getSensorNb() != null) {
            mCalibrator = new Calibrator(mGlove);
        }
    }

    protected IGloveController newGloveController(Context context, Glove glove,
                                                    IGloveController.Listener listener){
        return GloveController.newInstance(context, glove, listener);
    }

    public static class Builder {
        private final Context context;
        private final Glove glove;

        public Builder(Context context, Glove glove) {
            this.context = context;
            this.glove = glove;
        }

        public GloveManager build() {
            return new GloveManager(this);
        }
    }



    @Override
    public void startConnection() {
        Log.d(TAG, "start connection in SideService " + mGlove.getSide());
        if(mGloveController == null) {
            mGloveController = newGloveController(mContext, mGlove, this);
        }
        int state = mGloveController.getState();
        if(state == IGloveController.STATE_DISCONNECTED) {
            mGloveController.connect();
        }
    }

    @Override
    public void stopConnection() {
        Log.d(TAG, "stop connection in SideService " + mGlove.getSide());
        isServiceOn = false;
        if(mGloveController != null){
            mGloveController.disconnect();
            mGloveController = null;
        }
        cancelCalibration();
        unsubscribeAllFrameSubscribers();
    }

    private void onConnection(){
        //stop connection timer
        mConnectionTimer.removeMessages(CONNECTION_WHAT);
        isFirstReconnectionAttempted = false;

        broadcastConnectionChange(mGlove.getSide(), true);

        if (mGloveController != null) {
            mGloveController.requestStartSending();
        }
    }

    private void onDisconnection(){
        //send reconnection event if the service has not been stopped
        mConnectionTimer.removeMessages(CONNECTION_WHAT);
        if(isServiceOn) {
            int reconnectionDelay;
            if(!isFirstReconnectionAttempted) {
                reconnectionDelay = CONNECTION_DELAY_1;
                isFirstReconnectionAttempted = true;
            }else{
                reconnectionDelay = CONNECTION_DELAY_2;
            }
            mConnectionTimer.sendMessageDelayed(mConnectionTimer.obtainMessage(CONNECTION_WHAT),
                    reconnectionDelay);
        }

        broadcastConnectionChange(mGlove.getSide(), false);
    }


    private void broadcastConnectionChange(int side, boolean connected){
        Intent intent = new Intent();
        intent.setAction(connected ? ACTION_BT_CONNECTION : ACTION_BT_DISCONNECTION);
        intent.putExtra(EXTRA_SIDE, side);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    @Override
    public boolean startCalibration(Callback callback) {
        if(mCalibrator != null && !isCalibrating()) {
            subscribe(mCalibrator);
            mCalibrator.startCalibration(callback);
            return true;
        }
        return false;
    }

    @Override
    public boolean stopCalibration() {
        if(mCalibrator != null && isCalibrating()) {
            mCalibrator.unsubscribe();
            mCalibrator.stopCalibration();
            for(IFrameSubscriber sub: mSubscribers){
                sub.onCalibrationReceived(mCalibrator.getCalibration().clone(), mGlove.getSide());
            }
            return true;
        }
        return false;
    }

    @Override
    public void cancelCalibration() {
        if(mCalibrator != null && isCalibrating()) {
            mCalibrator.unsubscribe();
            mCalibrator.cancelCalibration();
        }
    }

    @Override
    public boolean isCalibrating() {
        return mCalibrator != null && mCalibrator.isCalibrating();
    }

    @Override
    public byte[] getCalibration() {
        return mCalibrator != null ? mCalibrator.getCalibration() : null;
    }

    private void unsubscribeAllFrameSubscribers(){
        for (IFrameSubscriber sub: mSubscribers) unsubscribe(sub);
    }

    public void unsubscribe(IFrameSubscriber subscriber){
        if(subscriber != null){
            if(mSubscribers.remove(subscriber)) subscriber.onUnsubscribed();
        }
    }

    @Override
    public void subscribe(IFrameSubscriber subscriber){
        if(subscriber != null && !mSubscribers.contains(subscriber)){
            mSubscribers.add(subscriber);
            subscriber.onSubscribed(this);
            byte[] cal = mCalibrator != null ? mCalibrator.getCalibration() : null;
            if(cal != null) subscriber.onCalibrationReceived(cal, mGlove.getSide());
        }
    }

    @Override
    public boolean isConnected() {
        return mGloveController != null
                && mGloveController.getState() == mGloveController.STATE_CONNECTED;
    }

    @Override
    public void requestCalibration(CalibrationCallback callback) {
        if(isConnected()){
            mCalibrationCallback = callback;
            mGloveController.requestCalibration();
        }
    }

    /**
     *
     * @return true if bluetooth is on, false otherwise
     */
    private boolean isBluetoothEnabled(){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.isEnabled();
    }

    /////////////////////////////////////////////
    // IInsoleController.Listener methods
    /////////////////////////////////////////////

    @Override
    public void onStateChanged(int state) {
        if(state == IGloveController.STATE_CONNECTED) {
            onConnection();
        }else if(state == IGloveController.STATE_DISCONNECTED) {
            onDisconnection();
        }
    }

    @Override
    public void onDataFrame(byte[] frame) {
        for(IFrameSubscriber sub: mSubscribers){
            sub.onFrameReceived(frame.clone(), mGlove.getSide());
        }
    }

    @Override
    public void onSensorNb(int sensorNb) {
        if(mGlove.getSensorNb() == null) {

            mGlove.setSensorNb(sensorNb);
            updateGloveInDb(mGlove);

            if(mCalibrator == null) mCalibrator = new Calibrator(mGlove);

            Intent intent = new Intent();
            intent.setAction(ACTION_GLOVE);
            intent.putExtra(EXTRA_SIDE, mGlove.getSide());
            intent.putExtra(EXTRA_SENSOR_NB, sensorNb);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    @Override
    public void onSide(int side) {

    }

    @Override
    public void onPong() {
        //nothing to do yet
    }

    @Override
    public void onStartSending() {
        //nothing to do yet
    }

    @Override
    public void onStopSending() {
        //nothing to do yet
    }

    @Override
    public void onBootMode() {
        //nothing to do yet
    }

    @Override
    public void onCalibration() {
        //nothing to do yet
    }

    private void updateGloveInDb(Glove glove) {
        DaoAccess access = DaoAccess.getInstance(mContext);
        DaoSession daoSession = access.openSession();
        GloveDao dao = daoSession.getGloveDao();
        dao.update(glove);
        access.closeSession();
    }
}
