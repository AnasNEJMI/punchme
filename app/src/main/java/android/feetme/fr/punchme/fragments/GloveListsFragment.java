package android.feetme.fr.punchme.fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.adapters.GloveAdapter;
import android.feetme.fr.punchme.dao.DaoAccess;
import android.feetme.fr.punchme.dao.DaoSession;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.dao.GloveDao;
import android.feetme.fr.punchme.managers.IGloveManager;
import android.feetme.fr.punchme.utils.PreferenceUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

/**
 * Created by Anas on 19/02/2016.
 */
public abstract class GloveListsFragment extends ServiceFragment {

    private static final String TAG = GloveListsFragment.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    protected ListView mLeftListView;
        protected ListView mRightListView;
        protected GloveAdapter mAdapterLeft;
        protected GloveAdapter mAdapterRight;

        protected boolean registered = false;

        public interface Listener{
            void onInsoleClick(long insoleId);
        }

        private Listener mListener;

        protected BroadcastReceiver mInsoleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                    int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, 0);
                    onBondStateChanged(device, bondState, previousBondState);
                }if(action.equals(IGloveManager.ACTION_BT_CONNECTION) ||
                        action.equals(IGloveManager.ACTION_BT_DISCONNECTION)){
                    updateListsNoSort();
                }
            }
        };

    protected abstract void onBondStateChanged(BluetoothDevice device, int bondState, int previousBondState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        mLeftListView = (ListView) view.findViewById(R.id.insole_left_list);
        mRightListView = (ListView) view.findViewById(R.id.insole_right_list);
        mAdapterLeft = new GloveAdapter(getActivity(), GloveFactory.SIDE_LEFT);
        mAdapterRight = new GloveAdapter(getActivity(), GloveFactory.SIDE_RIGHT);

        mLeftListView.setAdapter(mAdapterLeft);
        mRightListView.setAdapter(mAdapterRight);

        mLeftListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onInsoleClick(mAdapterLeft.getItem(position).getId());
                }
            }
        });

        mRightListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onInsoleClick(mAdapterRight.getItem(position).getId());
                }
            }
        });

        updateLists();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (Listener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!registered){
            registerReceiver();
            registered = true;
        }
        updateLists();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(registered){
            getActivity().unregisterReceiver(mInsoleReceiver);
            registered = false;
        }
    }

    protected  void updateLists(){
        updateList(GloveFactory.SIDE_LEFT, mAdapterLeft);
        updateList(GloveFactory.SIDE_RIGHT, mAdapterRight);
    }

    protected void showUnableToUnselectDialog(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_insole_unable_to_unselect)
                .setMessage(R.string.dialog_insole_unable_to_unselect_msg)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show();
    }

    protected  void updateList(final int side, final GloveAdapter adapter){
        new AsyncTask<Void, Void, List<Glove>>(){

            Context context = getActivity().getApplicationContext();
            @Override
            protected List<Glove> doInBackground(Void... params) {

                DaoAccess access = DaoAccess.getInstance(context);
                DaoSession daoSession = access.openSession();
                GloveDao dao = daoSession.getGloveDao();
                List<Glove> gloves = dao.queryBuilder()
                        .where(GloveDao.Properties.Side.eq(side))
                        .list();
                access.closeSession();

                return gloves;
            }

            @Override
            protected void onPostExecute(List<Glove> gloves) {
                for(Glove glove: gloves) adapter.add(glove);

                adapter.setDefaultInsole(PreferenceUtils.getGlove(context, side));
                adapter.sort();

                if(getActivity() != null){
                    updateListNoSort(side, adapter);
                }
            }
        }.execute();
    }

    protected void updateListNoSort(final int side, final GloveAdapter adapter){
        if(mMainServiceManager != null){
            adapter.setGloveConnected(mMainServiceManager.isConnected(side));
        }
        adapter.notifyDataSetChanged();
    }

    protected void updateListsNoSort(){
        updateListNoSort(GloveFactory.SIDE_LEFT, mAdapterLeft);
        updateListNoSort(GloveFactory.SIDE_RIGHT, mAdapterRight);
    }

    @Override
    protected void onServiceConnected() {
        updateListsNoSort();
    }

    protected void registerReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(IGloveManager.ACTION_BT_CONNECTION);
        filter.addAction(IGloveManager.ACTION_BT_DISCONNECTION);
        getActivity().registerReceiver(mInsoleReceiver, filter);
    }
}
