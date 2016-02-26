package android.feetme.fr.punchme.controllers;

/**
 * Created by Anas on 26/02/2016.
 */
public interface IGloveController {

    /**
     * Connection state is disconnected.
     */
    int STATE_DISCONNECTED = 1;

    /**
     * Initiating an outgoing connection.
     */
    int STATE_CONNECTING = 2;

    /**
     * Connected to a remote device
     */
    int STATE_CONNECTED = 3;

    /**
     * Get the connection state.
     *
     * @return int
     */
    int getState();

    /**
     * Initiates connection with the glove.
     */
    void connect();

    /**
     * Stops the connection.
     */
    void disconnect();


    /**
     * Send a request to the connected device to get the number of sensor.
     */
    void requestSensorNb();

    /**
     * Send a request to the connected device to get the glove side.
     */
    void requestSide();

    /**
     * Send a ping request to the connected device.
     */
    void requestPing();

    /**
     * Send a request to the connected device to start sending data frames.
     */
    void requestStartSending();

    /**
     * Send a request to the connected device to stop sending data frames.
     */
    void requestStopSending();

    /**
     * Send a request to the connected device to calibrate it.
     */
    void requestCalibration();

    interface Listener {

        /**
         * Notification that the state of the connection with the glove has changed.
         *
         * @param state
         */
        void onStateChanged(int state);

        /**
         * A data frame is received.
         *
         * @param frame
         */
        void onDataFrame(byte[] frame);

        /**
         * The number of sensors of the glove is received.
         *
         * @param sensorNb
         */
        void onSensorNb(int sensorNb);

        /**
         * The side of the glove is received.
         *
         * @param side 1 for left 2 for right.
         */
        void onSide(int side);

        /**
         * The response to ping has been received.
         */
        void onPong();

        /**
         * The glove will start sending data.
         */
        void onStartSending();

        /**
         * The glove will stop sending data.
         */
        void onStopSending();

        /**
         * The glove is in boot mode.
         */
        void onBootMode();

        /**
         * The glove has just calibrated itself.
         */
        void onCalibration();
    }

}
