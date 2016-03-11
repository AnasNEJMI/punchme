package android.feetme.fr.punchme.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.Utils;
import android.feetme.fr.punchme.activities.IScanDismissListener;
import android.feetme.fr.punchme.adapters.ScanAdapter;
import android.feetme.fr.punchme.connectivity.IGloveScanner;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Anas on 04/03/2016.
 */
public abstract class GloveScanFragment extends GloveListsFragment implements IGloveScanner.Listener, IScanDismissListener {

    protected static final String TAG = GloveScanFragment.class.getSimpleName();

    private static final int BTN_TITLE_BEFORESCAN = R.string.insoles;
    private static final int BTN_TITLE_SCANNING = R.string.scan;

    protected IGloveScanner mGloveScanner;
    protected ScanAdapter mAdapter;

    private ListView mListView;
    protected ImageButton mDismissBtn;
    protected LinearLayout mScanFrame;
    private TextView scanBtnText;
    protected FloatingActionButton mShowScanBtn;

    private ImageView scanBtn;
    private Animation scanBtnAnim;

    private int SCAN_STATE_HIDDEN = 0;
    private int SCAN_STATE_SHOWING = 1;
    private int SCAN_SCREEN_STATE_SHOWN = 2;
    private int SCAN_SCREEN_STATE_HIDING = 3;
    protected int mScanScreenState = SCAN_STATE_HIDDEN;

    public interface IScanFrameListener{
        void onScanFrameShowed(boolean b);
    }

    protected IScanFrameListener mScanFrameListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mScanFrameListener = (IScanFrameListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("context must implement IScanFrameListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mScanFrameListener = null;
    }

    protected final OvershootInterpolator interpolator = new OvershootInterpolator(2);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = new ScanAdapter(getActivity(), new ArrayList<String>());

        mListView = (ListView) view.findViewById(R.id.scan_list_0);
        mListView.setAdapter(mAdapter);

        scanBtn = (ImageView) view.findViewById(R.id.scan_btn_ic);
        setScanBtnClickListener();
        setItemClickListeners();
        scanBtnText = (TextView) view.findViewById(R.id.scan_btn_text);

        mShowScanBtn = (FloatingActionButton) view.findViewById(R.id.button_show_scan);
        mShowScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScanFragment();
                Utils.scaleDown(getActivity(), mShowScanBtn);
            }
        });

        mDismissBtn = (ImageButton) view.findViewById(R.id.dismiss_btn_ic);
        mDismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mScanFrame = (LinearLayout) view.findViewById(R.id.scan_frame);


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mScanScreenState != SCAN_STATE_SHOWING && mScanScreenState != SCAN_SCREEN_STATE_SHOWN) {
            Utils.scaleUp(getActivity(), mShowScanBtn);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.scaleDown(getActivity(), mShowScanBtn);
    }

    @Override
    public void onScanStarted() {
        setScanBtnText(BTN_TITLE_SCANNING);
        if(scanBtnAnim == null) {
            scanBtnAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_indefinitely);
        }
        scanBtn.startAnimation(scanBtnAnim);
    }

    @Override
    public void onScanFinished() {
        setScanBtnText(BTN_TITLE_BEFORESCAN);
        if(scanBtnAnim != null) {
            scanBtnAnim.cancel();
            scanBtnAnim.reset();
        }
    }

    private void setScanBtnText(int textId){
        scanBtnText.setText(getActivity().getResources().getString(textId));
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void dismiss() {
        if(mScanScreenState == SCAN_SCREEN_STATE_SHOWN) {
            mScanScreenState = SCAN_SCREEN_STATE_HIDING;
            mScanFrame.animate()
                    .setInterpolator(interpolator)
                    .translationYBy((float) mScanFrame.getHeight())
                    .setDuration(500)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null && !isDetached()) {
                                mScanScreenState = SCAN_STATE_HIDDEN;
                                mScanFrameListener.onScanFrameShowed(false);
                                Utils.scaleUp(getActivity(), mShowScanBtn);
                            }
                        }
                    });
            stopScan();
        }
    }

    protected void stopScan() {
        if(mGloveScanner.isScanning()) mGloveScanner.stopScan();
    }

    private void setScanBtnClickListener(){
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGloveScanner.isScanning()) {
                    mGloveScanner.stopScan();
                    Log.d("click", "stop");
                } else {
                    mAdapter.clear();
                    if (!mGloveScanner.isBluetoothEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1);
                    }
                    startScan();
                    Log.d("click", "start");
                }
            }
        });
    }

    protected void startScan(){
        if(mGloveScanner.isBluetoothEnabled()){
            mGloveScanner.startScan();
        }else{
            Toast.makeText(getActivity(), R.string.bluetooth_off, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * img_tuto_scan result items click listener
     */
    private void setItemClickListeners(){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String address = mAdapter.getAddress(i);
                String name = mAdapter.getName(address);
                if (address.equals(name)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.scan_dialog_no_name)
                            .setMessage(R.string.scan_dialog_no_name_message)
                            .setPositiveButton(R.string.ok, null)
                            .create()
                            .show();
                } else {
                    showInsoleTypeDialog(i);
                }
            }
        });
    }

    public void showInsoleTypeDialog(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.scan_dialog_setup)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onInsoleDialogClick(index);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing
                    }
                });
        builder.create().show();
    }

    protected abstract void onInsoleDialogClick(int index);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showScanFragment(){
        if(mScanScreenState == SCAN_STATE_HIDDEN){
            mScanScreenState = SCAN_STATE_SHOWING;
            mScanFrame.animate()
                    .setInterpolator(interpolator)
                    .translationYBy((float) - mScanFrame.getHeight())
                    .setDuration(500)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null && !isDetached()) {
                                mScanScreenState = SCAN_SCREEN_STATE_SHOWN;
                                mScanFrameListener.onScanFrameShowed(true);
                            }
                        }
                    });
            mScanFrame.animate().alphaBy(1);
            mDismissBtn.setImageResource(R.drawable.vector_drawable_close);

            Log.d("1", "about to");
            if (mGloveScanner.isScanning()) {
                mGloveScanner.stopScan();
                Log.d("2", "stop");
            } else {
                mAdapter.clear();
                Log.d("2", "clear");
                if(!mGloveScanner.isBluetoothEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
                startScan();
                Log.d("2", "start");
            }
        }
    }
}
