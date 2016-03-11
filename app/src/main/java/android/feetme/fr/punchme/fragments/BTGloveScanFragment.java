package android.feetme.fr.punchme.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.connectivity.GloveScanner;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.exceptions.GloveArgumentException;
import android.feetme.fr.punchme.utils.PreferenceUtils;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Anas on 04/03/2016.
 */
public class BTGloveScanFragment extends GloveScanFragment {

    private Glove mPairingGlove;
    private ProgressDialog mDialog;
    private Timer mTimer;

    protected boolean isScanReceiverRegistered = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mGloveScanner = new GloveScanner(getActivity(), this);
        return view;
    }

    @Override
    public void onResume() {
        if (!isScanReceiverRegistered) {
            ((GloveScanner) mGloveScanner).registerReceiver();
            isScanReceiverRegistered = true;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isScanReceiverRegistered) {
            ((GloveScanner) mGloveScanner).unregisterReceiver();
            isScanReceiverRegistered = false;
        }

    }

    /**
     * Listener for insole choice dialog.
     *
     * @param index index of the item clicked in the list
     */
    @Override
    public void onInsoleDialogClick(int index) {
        if (mMainServiceManager != null && mMainServiceManager.isConnectionStarted()) {
            showUnableToUnselectDialog();
        } else {
            mGloveScanner.stopScan();

            String address = mAdapter.getAddress(index);
            String name = mAdapter.getName(address);
            Glove glove;
            try {
                glove = GloveFactory.newInstance(address, name);
            } catch (GloveArgumentException e) {
                //TODO show failure result
                return;
            }

            int bondedState = ((GloveScanner) mGloveScanner).isDeviceBonded(glove.getAddress());

            if (bondedState == GloveScanner.BOND_STATE_INLIST_BONDED
                    || bondedState == GloveScanner.BOND_STATE_INLIST_NOT_BONDED) {

                updateDefaultInsole(glove);

            } else if (bondedState == GloveScanner.BOND_STATE_NOT_INLIST_NOT_BONDED) {
                mTimer = new Timer();
                mDialog = ProgressDialog.show(getActivity(),
                        getString(R.string.dialog_pairing_title),
                        getString(R.string.dialog_wait_message),
                        true);
                mPairingGlove = glove;

                ((GloveScanner) mGloveScanner).pairDevice(address);

                long delayInMillis = 10000;
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mDialog != null) {
                            mDialog.dismiss();
                            mDialog = null;
                            mPairingGlove = null;
                            showFailureDialog();
                        }
                    }
                }, delayInMillis);

            } else if (bondedState == GloveScanner.BOND_STATE_NOT_INLIST_BONDED) {
                Log.d(TAG, "WEIRD CASE");
            } else {
                Log.d(TAG, "already bonding");
            }
        }
    }

    private void showFailureDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_pairing_title)
                        .setMessage(R.string.dialog_pairing_failure_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Nothing to do
                            }
                        })
                        .show();
            }
        });
    }

    //////////////////////////////////////////////////////////////////
    // IScanListener
    //////////////////////////////////////////////////////////////////

    @Override
    public void onDeviceScanned(String address, String name, List<UUID> uuids) {

        if (name == null || name.equals("")) {
            mAdapter.add(address, address);
        } else {
            if (name.contains("PunchMe")) {
                mAdapter.add(address, name);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onBondStateChanged(BluetoothDevice device, int bondState, int previousBondState) {
        Log.d(TAG, "Bond state changed: " + bondState);
        if (mPairingGlove != null && device.getAddress().equals(mPairingGlove.getAddress())) {
            if (bondState == BluetoothDevice.BOND_BONDING) {
                Log.d(TAG, "Bond state changed: bonding");
            } else {
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "Bond state changed: bonded");
                    updateDefaultInsole(mPairingGlove);
                } else if (bondState == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "Bond state changed: none");
                    if (mDialog != null) showFailureDialog();
                }
                if (mDialog != null) mDialog.dismiss();
                if (mTimer != null) mTimer.cancel();
                mTimer = null;
                mPairingGlove = null;
            }
        }
    }

    protected void updateDefaultInsole(Glove glove){

        //Save the insole info to preferences
        PreferenceUtils.setGlove(getActivity(), glove);

        //we need the id of the inserted insole (if inserted)
        Glove updatedGlove = PreferenceUtils.getGlove(getActivity(), glove.getSide());
        if(updatedGlove != null){
            mMainServiceManager.updateGlove(updatedGlove, updatedGlove.getSide());
        }

        //update UI
        updateLists();
        mAdapter.notifyDataSetChanged();
    }

}
