package android.feetme.fr.punchme.service;

import android.feetme.fr.punchme.Glove;

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
    boolean isConnected();

    /**
     * Returns the default glove to which the service will connect when connection is started.
     * @return the requested default Insole object
     */
    Glove getGlove();


}
