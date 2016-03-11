package android.feetme.fr.punchme.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.controllers.IPunchMapController;
import android.feetme.fr.punchme.managers.IGloveManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

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

        FloatingActionButton connectionBtn = (FloatingActionButton) view.findViewById(R.id.connect_to_glove);
        connectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


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
//        if(mMainServiceManager.isConnected(GloveFactory.SIDE_LEFT)){
//            mControllerLeft.startDrawing(mMainServiceManager.getGlove(GloveFactory.SIDE_LEFT));
//            mMainServiceManager.registerFrameSubscriber(mControllerLeft, GloveFactory.SIDE_LEFT);
//
//        }
//
//        if(mMainServiceManager.isConnected(GloveFactory.SIDE_RIGHT)) {
//            mControllerRight.startDrawing(mMainServiceManager.getGlove(GloveFactory.SIDE_RIGHT));
//            mMainServiceManager.registerFrameSubscriber(mControllerRight, GloveFactory.SIDE_RIGHT);
//        }

        if (mMainServiceManager.isConnectionStarted()) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected void registerReceiver(){
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int side = intent.getExtras().getInt(IGloveManager.EXTRA_SIDE);

                if(action.equals(IGloveManager.ACTION_BT_CONNECTION)){
                    onBTConnection(side);
                }else if(action.equals(IGloveManager.ACTION_BT_DISCONNECTION)){
                    onBTDisconnection(side);
                }else if(action.equals(IGloveManager.ACTION_GLOVE)){
                    Log.d(TAG, "insole update received");
                    if (mMainServiceManager.isConnected(side)) {
                        if (side == GloveFactory.SIDE_LEFT) {
                            //do something when connected !!!! ////
                            //TODO
//                            mControllerLeft.startDrawing(mMainServiceManager.getGlove(side));
                        } else {
                            //do something when connected !!!! ////
                            //TODO
//                            mControllerRight.startDrawing(mMainServiceManager.getGlove(side));
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(IGloveManager.ACTION_BT_CONNECTION);
        filter.addAction(IGloveManager.ACTION_BT_DISCONNECTION);
        filter.addAction(IGloveManager.ACTION_CALIBRATED);
        filter.addAction(IGloveManager.ACTION_GLOVE);
        filter.addAction(IGloveManager.ACTION_BOOTMODE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }

    private void onBatteryInfoReceived(int level) {
        //TODO
    }

    private void onBTDisconnection(int side) {

        //TODO
//        if(side == GloveFactory.SIDE_LEFT){
//            mControllerLeft.stopDrawing();
//            if(mMainServiceManager != null){
//                mMainServiceManager.unregisterFrameSubscriber(mControllerLeft, InsoleFactory.SIDE_LEFT);
//            }
//        }else{
//            mControllerRight.stopDrawing();
//            if(mMainServiceManager != null){
//                mMainServiceManager.unregisterFrameSubscriber(mControllerRight, InsoleFactory.SIDE_RIGHT);
//            }
//        }
    }

    private void onBTConnection(int side) {
        if(mMainServiceManager != null) {

            //TODO
//            if (side == GloveFactory.SIDE_LEFT) {
//                mControllerLeft.startDrawing(mMainServiceManager.getGlove(side));
//                mMainServiceManager.registerFrameSubscriber(mControllerLeft, side);
//            } else {
//                mControllerRight.startDrawing(mMainServiceManager.getInsole(side));
//                mMainServiceManager.registerFrameSubscriber(mControllerRight, side);
//            }

        }
    }
}
