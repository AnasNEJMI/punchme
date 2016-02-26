package android.feetme.fr.punchme.calibration;

/**
 * Created by Anas on 26/02/2016.
 */
public interface ICalibrator {

    /**
     * Starts calibration
     *
     * @param callback callback that will be notified when calibration is ready.
     * @return true if the calibration started, false otherwise
     */
    boolean startCalibration(Callback callback);

    /**
     * Terminates and validate calibration.
     *
     * @return true if the calibration is valid, false otherwise
     */
    boolean stopCalibration();

    /**
     * Cancel a started calibration.
     */
    void cancelCalibration();

    boolean isCalibrating();

    byte[] getCalibration();

    interface Callback{
        void onCalibrated(byte[] calibration);
        void onCancelled();
    }
}