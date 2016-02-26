package android.feetme.fr.punchme.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.controllers.IPunchMapController;
import android.feetme.fr.punchme.managers.IGloveManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Anas on 19/02/2016.
 */
public class PunchMapFragment extends ServiceFragment {

    private static final String TAG = PunchMapFragment.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    protected BroadcastReceiver mReceiver;

    private IPunchMapController mPunchMapController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_punch_map, container, false);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onServiceConnected() {
        //TODO
    }

    protected void registerReceiver(){
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(IGloveManager.ACTION_BT_CONNECTION)){
                    onBTConnection();
                }else if(action.equals(IGloveManager.ACTION_BT_DISCONNECTION)){
                    onBTDisconnection();
                }else if(action.equals(IGloveManager.ACTION_BATTERY)){
                    int level = intent.getExtras().getInt(IGloveManager.EXTRA_LEVEL);
                    onBatteryInfoReceived(level);
                }else if(action.equals(IGloveManager.ACTION_GLOVE)){
                    if(mMainServiceManager.isConnected()){
                        mPunchMapController.startDrawing(mMainServiceManager.getGlove());
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(IGloveManager.ACTION_BT_CONNECTION);
        filter.addAction(IGloveManager.ACTION_BT_DISCONNECTION);
        filter.addAction(IGloveManager.ACTION_BATTERY);
        filter.addAction(IGloveManager.ACTION_CALIBRATED);
        filter.addAction(IGloveManager.ACTION_GLOVE);
        filter.addAction(IGloveManager.ACTION_BOOTMODE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }

    private void onBatteryInfoReceived(int level) {
        //TODO
    }

    private void onBTDisconnection() {
        //TODO
    }

    private void onBTConnection() {
        //TODO
    }
}
