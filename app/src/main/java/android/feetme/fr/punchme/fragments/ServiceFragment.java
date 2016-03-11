package android.feetme.fr.punchme.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.feetme.fr.punchme.activities.MainActivity;
import android.feetme.fr.punchme.activities.ScanActivity;
import android.feetme.fr.punchme.service.IMainServiceManager;
import android.feetme.fr.punchme.service.MainService;
import android.os.IBinder;
import android.support.v4.app.Fragment;

/**
 * Created by Anas on 19/02/2016.
 */
public abstract class ServiceFragment extends Fragment {

    protected IMainServiceManager mMainServiceManager;
    protected boolean bound = false;

    @Override
    public void onResume(){
        super.onResume();
        bindService();
    }

    @Override
    public void onPause(){
        super.onPause();
        unbindService();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            MainService.MainBinder b = (MainService.MainBinder) binder;
            mMainServiceManager = b.getMainManager();
            ServiceFragment.this.onServiceConnected();

        }

        public void onServiceDisconnected(ComponentName className) {
            mMainServiceManager = null;
        }
    };

    protected abstract void onServiceConnected();

    private void bindService(){
        if(!bound){
            Intent service = newServiceIntent();
            getActivity().startService(service);
            getActivity().bindService(service, mConnection, Context.BIND_AUTO_CREATE);
            bound = true;
        }
    }

    private void unbindService(){
        if(bound)
            getActivity().unbindService(mConnection);
        bound = false;
    }

    private Intent newServiceIntent() {
        return new Intent(getActivity(), MainService.class);
    }

}
