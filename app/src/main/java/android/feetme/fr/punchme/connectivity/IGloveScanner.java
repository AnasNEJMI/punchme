package android.feetme.fr.punchme.connectivity;

import java.util.List;
import java.util.UUID;

/**
 * Created by Anas on 04/03/2016.
 */
public interface IGloveScanner {

    void startScan();

    void stopScan();

    boolean isScanning();

    boolean isBluetoothEnabled();

    /**
     * Interface for Bluetooth or BLE img_tuto_scan listeners.
     */
    interface Listener {

        /**
         * Every time device is found while scanning.
         * @param address Hardware MAC address of  the discovered device.
         * @param name Name of the discovered device.
         * @param uuids In case of BLE, advertising UUIDs. Null in case of core BT.
         */
        void onDeviceScanned(String address, String name, List<UUID> uuids);

        void onScanStarted();

        void onScanFinished();
    }
}
