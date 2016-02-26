package android.feetme.fr.punchme.managers;

import android.feetme.fr.punchme.calibration.ICalibrator;
import android.feetme.fr.punchme.utils.IFrameProducer;

/**
 * Created by Anas on 19/02/2016.
 */
public interface IGloveManager extends IFrameProducer, ICalibrator {

    /**
     * Broadcast intent ID for a glove connection.
     */
    String ACTION_BT_CONNECTION = "fr.feetme.btconnection";

    /**
     * Broadcast intent ID for a glove disconnection.
     */
    String ACTION_BT_DISCONNECTION = "fr.feetme.btdisconnection";

    /**
     * Broadcast intent ID for a notification of a calibration done.
     */
    String ACTION_CALIBRATED = "fr.feetme.calibrated";

    /**
     * Broadcast intent ID for new glove information.
     */
    String ACTION_GLOVE = "fr.feetme.glove";

    /**
     * Broadcast intent ID for a boot mode detection.
     */
    String ACTION_BOOTMODE = "fr.feetme.bootmode";

    /**
     * Broadcast value of the side of the glove.
     */
    String EXTRA_SIDE = "side";

    /**
     * Broadcast value of the number of sensors of the glove.
     */
    String EXTRA_SENSOR_NB = "sensors";

    /**
     * Starts the connection to the glove.
     */
    void startConnection();

    /**
     * Stops the connection to the glove.
     */
    void stopConnection();

    /**
     * Gives the connection state of the glove.
     */
    boolean isConnected();

    /**
     * request a new calibration.
     */
    void requestCalibration(CalibrationCallback callback);

    /**
     * Callback interface for a calibration request.
     */
    interface CalibrationCallback{
        void onCalibrated();
    }
}
