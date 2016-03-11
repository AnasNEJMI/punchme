package android.feetme.fr.punchme.connectivity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

/**
 * Created by Anas on 04/03/2016.
 */
public class GloveScanner implements IGloveScanner{

    private static String TAG = GloveScanner.class.getSimpleName();

    public static final int BOND_STATE_INLIST_BONDED = 1;
    public static final int BOND_STATE_INLIST_NOT_BONDED = 2;
    public static final int BOND_STATE_NOT_INLIST_NOT_BONDED = 3;
    public static final int BOND_STATE_NOT_INLIST_BONDED = 4;
    public static final int BOND_STATE_BONDING = 5;

    private BluetoothAdapter mAdapter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mListener.onDeviceScanned(device.getAddress(), device.getName(), null);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                mListener.onScanStarted();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                mListener.onScanFinished();
            }
        }
    };

    private final IntentFilter mBroadcastFilter = new IntentFilter();

    private Context mContext;
    private Listener mListener;


    public GloveScanner(Context context, Listener listener){
        mContext = context;
        mListener = listener;
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        mBroadcastFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mBroadcastFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mBroadcastFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }
    @Override
    public void startScan() {
        if (!isScanning()) {
            boolean discoveryStarted = mAdapter.startDiscovery();
            Log.d(TAG, "Start discovery: " + discoveryStarted);

        }
    }

    @Override
    public void stopScan() {
        if(isScanning()){
            mAdapter.cancelDiscovery();
            Log.d(TAG, "Cancel discovery");
        }
    }

    @Override
    public boolean isScanning() {
        return mAdapter.isDiscovering();
    }

    @Override
    public boolean isBluetoothEnabled() {
        return mAdapter.isEnabled();
    }

    public void registerReceiver(){
        mContext.registerReceiver(mReceiver, mBroadcastFilter);
    }

    public void unregisterReceiver(){
        mContext.unregisterReceiver(mReceiver);
    }

    public int isDeviceBonded(String address){
        BluetoothDevice device = mAdapter.getRemoteDevice(address);
        int bondState = device.getBondState();

        boolean isInBondedDevicesList = false;

        for(BluetoothDevice dev : mAdapter.getBondedDevices()){
            if(dev.getAddress().equals(address)){
                isInBondedDevicesList = true; break;
            }
        }

        if(bondState == BluetoothDevice.BOND_BONDED && isInBondedDevicesList){
            return BOND_STATE_INLIST_BONDED;
        }else if(bondState == BluetoothDevice.BOND_BONDED && !isInBondedDevicesList){
            return BOND_STATE_NOT_INLIST_BONDED;
        }else if(bondState == BluetoothDevice.BOND_NONE && isInBondedDevicesList){
            return BOND_STATE_INLIST_NOT_BONDED;
        }else if(bondState == BluetoothDevice.BOND_NONE && !isInBondedDevicesList){
            return BOND_STATE_NOT_INLIST_NOT_BONDED;
        }else{
            return BOND_STATE_BONDING;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void pairDevice(String address){
        BluetoothDevice device = mAdapter.getRemoteDevice(address);
        device.createBond();
    }
}
