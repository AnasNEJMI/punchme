package android.feetme.fr.punchme.managers;

/**
 * Created by Anas on 19/02/2016.
 */
public interface IGloveManager {

    /**
     * Broadcast intent ID for an glove connection.
     */
    String ACTION_BT_CONNECTION = "fr.punchme.btconnection";

    /**
     * Broadcast intent ID for an glove disconnection.
     */
    String ACTION_BT_DISCONNECTION = "fr.punchme.btdisconnection";

    /**
     * Broadcast intent ID for new battery info received.
     */
    String ACTION_BATTERY = "fr.punchme.battery";

    /**
     * Broadcast intent ID for a notification of a calibration done.
     */
    String ACTION_CALIBRATED = "fr.punchme.calibrated";

    /**
     * Broadcast intent ID for new glove information.
     */
    String ACTION_GLOVE = "fr.punchme.glove";

    /**
     * Broadcast intent ID for a boot mode detection.
     */
    String ACTION_BOOTMODE = "fr.feetme.bootmode";

    /**
     * Broadcast value of the battery level.
     */
    String EXTRA_LEVEL = "level";

    /**
     * Broadcast value of the software version of the glove.
     */
    String EXTRA_VERSION_SOFTWARE = "version_software";

    /**
     * Broadcast value of the hardware version of the glove.
     */
    String EXTRA_VERSION_HARDWARE = "version_hardware";

    /**
     * Broadcast value of the number of sensors of the glove.
     */
    String EXTRA_SENSOR_NB = "sensors";
}
