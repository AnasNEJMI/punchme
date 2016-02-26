package android.feetme.fr.punchme.fragments;

import android.feetme.fr.punchme.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Anas on 19/02/2016.
 */
public class ScanFragment extends ServiceFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);




        return view;
    }


    //TODO

    @Override
    protected void onServiceConnected() {

    }
}
