package android.feetme.fr.punchme.utils;

/**
 * This interface must be implemented by classes that will transfer insole data frames
 * to some {@link android.feetme.fr.punchme.utils.IFrameSubscriber}.
 *
 * Subscribers must safely unregister from the producer to avoid memory leaks.
 *
 * Created by Anas on 26/02/2016.
 */
public interface IFrameProducer {

    /**
     * This method registers a subscriber to this producer so that it will receive
     * new available frames.
     *
     * @param subscriber
     */
    void subscribe(IFrameSubscriber subscriber);

    /**
     * This method unregisters an already registered subscriber.
     * Subscribers must unregister safely to avoid leaks.
     *
     * @param subscriber
     */
    void unsubscribe(IFrameSubscriber subscriber);
}