package android.feetme.fr.punchme.service;

import android.feetme.fr.punchme.controllers.IGloveController;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.managers.IGloveManager;
import android.feetme.fr.punchme.utils.IFrameSubscriber;

/**
 * Created by Anas on 19/02/2016.
 */
public interface IMainServiceManager {

    /**
     * Starts automatic connection to the gloves.
     * While the automatic connection is on, classes implementing this interface will continuously
     * try to connect to gloves that are set as default gloves.
     */
    void startConnection();

    /**
     * Stop the automatic connection to default gloves.
     * Disconnects gloves if connected.
     */
    void stopConnection();

    /**
     * Returns the automatic connection state.
     * @return true if automatic connection is on, false otherwise
     */
    boolean isConnectionStarted();

    /**
     * Gives the connection state of the glove.
     */
    boolean isConnected(int side);

    /**
     * Update default glove, to which the service will connect when connection is started.
     * If the provided glove is null, unsets the glove for the provided side.
     *
     * @param glove the glove to update
     * @param side left or right
     */
    void updateGlove(Glove glove, int side);

    /**
     * Returns the default glove to which the service will connect when connection is started.
     * @return the requested default glove object
     */
    Glove getGlove(int side);

    /**
     * Registers an IFrameSubscriber that will receive data frames from insoles.
     *
     * @param subscriber object that will receive data frames
     * @param side left or right
     */
    void registerFrameSubscriber(IFrameSubscriber subscriber, int side);

    /**
     * Unregisters an IFrameSubscriber that will receive data frames from insoles.
     *
     * @param subscriber object that was receiving data frames
     * @param side left or right
     */
    void unregisterFrameSubscriber(IFrameSubscriber subscriber, int side);

    /**
     * Request to the insole to calibrate.
     *
     * @param side left or right
     */
    void requestCalibration(int side, IGloveManager.CalibrationCallback callback);


    /**
     *
     * @param side left or right
     * @return true is the calibration started, false otherwise
     */
    boolean startCalibration(int side);

    /**
     *
     * @param side left or right
     * @return true if the calibration stopped correctly
     */
    boolean stopCalibration(int side);

    /**
     *
     * @param side
     */
    void cancelCalibration(int side);

    /**
     *
     * @param side left or right
     */
    boolean isCalibrating(int side);

}
