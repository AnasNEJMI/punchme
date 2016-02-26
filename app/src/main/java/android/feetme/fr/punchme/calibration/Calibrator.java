package android.feetme.fr.punchme.calibration;

import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.utils.IFrameProducer;
import android.feetme.fr.punchme.utils.IFrameSubscriber;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Anas on 26/02/2016.
 */
public class Calibrator implements IFrameSubscriber, ICalibrator {


    private static final String TAG = Calibrator.class.getSimpleName();

    private IFrameProducer mFrameProducer;

    private ICalibrator.Callback mCallback;

    private Glove mGlove;

    private byte[] mCalibration;

    private boolean isCalibrating;

    private int mMaxValue = GloveFactory.MAX_SENSOR_VALUE;

    public Calibrator(Glove glove){
        if(glove == null || glove.getSensorNb() == null){
            throw new IllegalArgumentException();
        }
        mGlove = glove;
    }



    @Override
    public boolean startCalibration(Callback callback) {
        Log.d(TAG, "Calibration started");
        isCalibrating = true;
        mCallback = callback;
        mCalibration = new byte[mGlove.getSensorNb()];
        Arrays.fill(mCalibration, (byte) mMaxValue);

        return true;
    }

    @Override
    public boolean stopCalibration() {
        Log.d(TAG, "Calibration stopped");
        isCalibrating = false;
        if(mCallback != null) mCallback.onCalibrated(mCalibration);
        mCallback = null;
        StringBuilder builder = new StringBuilder();
        for(byte b: mCalibration){
            builder.append(b & 0xFF)
                    .append(' ');
        }
        Log.d(TAG, builder.toString());


        return true;
    }

    @Override
    public void cancelCalibration() {
        Log.d(TAG, "Calibration cancelled");
        isCalibrating = false;
        if(mCallback != null) {
            mCallback.onCancelled();
            mCallback = null;
        }
        mCalibration = null;
    }

    @Override
    public boolean isCalibrating() {
        return isCalibrating;
    }

    @Override
    public byte[] getCalibration() {
        return isCalibrating ? null : mCalibration;
    }

    @Override
    public void onCalibrationReceived(byte[] calibration, int side) {
        // nothing to do yet
    }

    @Override
    public void onFrameReceived(byte[] frame, int side) {
        if(!isCalibrating) return;

        int calibValue;
        int frameValue;
        for(int i = 0; i < mCalibration.length; i++){
            frameValue = frame[GloveFactory.OVERHEAD + i] & 0xFF;
            frameValue = frameValue <= mMaxValue ? frameValue : 256 - frameValue;

            calibValue = mCalibration[i] & 0xFF;
            calibValue = calibValue <= mMaxValue ? calibValue : 256 - calibValue;

            mCalibration[i] = calibValue < frameValue ?
                    mCalibration[i] : frame[GloveFactory.OVERHEAD + i];
        }
    }

    @Override
    public void onSubscribed(IFrameProducer producer) {
        mFrameProducer = producer;
    }

    @Override
    public void onUnsubscribed() {
        mFrameProducer = null;
    }

    @Override
    public void unsubscribe() {
        if(mFrameProducer != null) mFrameProducer.unsubscribe(this);
    }
}
