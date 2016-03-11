package android.feetme.fr.punchme.adapters;

import android.content.Context;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.utils.PreferenceUtils;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Anas on 04/03/2016.
 */
public class GloveAdapter extends ArrayAdapter<Glove> {

    private static final String TAG = GloveAdapter.class.getSimpleName();

    private List<Glove> mList;
    private Set<String> mAddresses = new HashSet<>();
    private Context mContext;
    private Glove mDefaultGlove;
    private boolean isGloveConnected;



    public GloveAdapter(Context context, List<Glove> gloves, int side) {
        super(context, R.layout.item_glove, gloves);
        mList = gloves;
        mContext = context;

        for(Glove glove : gloves){
            if(glove != null && glove.getAddress() != null){
                mAddresses.add(glove.getAddress());
            }
        }

        mDefaultGlove = PreferenceUtils.getGlove(mContext, side);
    }

    public GloveAdapter(Context context, int side){
        this(context, new ArrayList<Glove>(), side);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InsoleViewHolder insoleViewHolder;

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_glove, null);
            insoleViewHolder = new InsoleViewHolder();

            insoleViewHolder.init(convertView);
            convertView.setTag(insoleViewHolder);
        }else{
            insoleViewHolder = (InsoleViewHolder) convertView.getTag();
            insoleViewHolder.init(convertView);
        }

        final Glove glove = mList.get(position);
        insoleViewHolder.populate(glove);

        return convertView;
    }

    @Override
    public void add(Glove glove) {
        String address = glove.getAddress();
        if(!mAddresses.contains(address)){
            String name = glove.getName();
            if(name != null && !name.equals("")){
                super.add(glove);
                mAddresses.add(address);
            }
        }
    }

    @Override
    public void addAll(Collection<? extends Glove> insoles) {
        for(Glove insole: insoles){
            add(insole);
        }
    }

    @Override
    public void remove(Glove glove) {
        super.remove(glove);
        mAddresses.remove(glove.getAddress());
    }

    @Override
    public void clear() {
        super.clear();
        mAddresses.clear();
    }

    public void sort(){
        List<Glove> gloves = new ArrayList<>(mList);
        mList.clear();

        //add to first position the default insole if it is in the list
        if(mDefaultGlove != null){
            String defaultAddress = mDefaultGlove.getAddress();
            if(mAddresses.contains(defaultAddress)) {
                for (Glove glove : gloves) {
                    if (glove.getAddress().equals(defaultAddress)) {
                        mList.add(glove);
                        gloves.remove(glove);
                        break;
                    }
                }
            }
        }

        Collections.sort(gloves, new Comparator<Glove>() {
            @Override
            public int compare(Glove lhs, Glove rhs) {
                return lhs.getSensorNb() - rhs.getSensorNb();
            }
        });

        mList.addAll(gloves);

        for(Glove glove: mList) Log.d(TAG, glove.getName());
    }

    public void setGloveConnected(boolean isGloveConnected) {
        this.isGloveConnected = isGloveConnected;
    }

    public boolean isGloveConnected() {
        return isGloveConnected;
    }

    public void setDefaultInsole(Glove glove){
        mDefaultGlove = glove;
    }

    class InsoleViewHolder {
        TextView name;
        TextView address;
        ImageView state;

        void init(View convertView){
            this.name = (TextView) convertView.findViewById(R.id.item_insole_name);
            this.state = (ImageView) convertView.findViewById(R.id.ic_state);
            this.address = (TextView) convertView.findViewById(R.id.item_insole_address);
        }

        void populate(final Glove glove){
            if(glove == null){
                return;
            }

            if(mDefaultGlove != null){
                if(glove.getAddress().equals(mDefaultGlove.getAddress())){
                    Drawable drawable;
                    PorterDuffColorFilter filter;
                    if(isGloveConnected){
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.vector_drawable_check);
                        filter = new PorterDuffColorFilter(ContextCompat.getColor(mContext,android.R.color.holo_green_light), PorterDuff.Mode.MULTIPLY);
                    }else{
                        drawable = ContextCompat.getDrawable(mContext, R.drawable.vector_drawable_circle);
                        filter = new PorterDuffColorFilter(ContextCompat.getColor(mContext,R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                    }
                    drawable.setColorFilter(filter);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        state.setBackground(drawable);
                    }
                }
            }

            name.setText(glove.getName());
            address.setText(glove.getAddress());
        }
    }
}
