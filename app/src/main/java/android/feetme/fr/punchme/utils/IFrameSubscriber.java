package android.feetme.fr.punchme.utils;

/**
 * This interface must be implemented by classes that need to receive insole frames.
 *
 * Implemntations of this interface must register to
 * an {@link android.feetme.fr.punchme.utils.IFrameProducer}
 * to receive frame events.
 *
 *
/**
 * Created by Anas on 26/02/2016.
 */
public interface IFrameSubscriber {

    /**
     * This method will be called by the frame producer when a new calibration is available.
     *
     * @param calibration calibration frame, without insole overhead.
     * @param side left or right
     */
    void onCalibrationReceived(byte[] calibration, int side);

    /**
     * This method will be called by the frame producer when a new frame is available.
     *
     * @param frame frame with overhead.
     * @param side left or right
     */
    void onFrameReceived(byte[] frame, int side);

    /**
     * Callback to notify this IFrameSubscriber when it is registered
     *
     * @param producer IFrameProducer to which this subscriber is registered
     */
    void onSubscribed(IFrameProducer producer);

    /**
     * Callback to notify this IFrameSubscriber when it is unregistered.
     *
     */
    void onUnsubscribed();

    /**
     * Unregister from the IFrameProducer
     */
    void unsubscribe();
}