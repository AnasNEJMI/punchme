package android.feetme.fr.punchme.adapters;

import android.content.Context;
import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.R;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.utils.PreferenceUtils;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anas on 04/03/2016.
 */
public class ScanAdapter extends ArrayAdapter<String> {

    private ArrayList<String> list;
    private Map<String, String> map;
    private Context context;

    private boolean isLeftConnected;
    private boolean isRightConnected;

    public ScanAdapter(Context context, List<String> objects) {
        super(context, R.layout.item_glove, objects);
        this.list = (ArrayList<String>) objects;
        this.map = new HashMap<>();
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InsoleViewHolder insoleViewHolder;

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_glove, null);
            insoleViewHolder = new InsoleViewHolder();

            insoleViewHolder.init(convertView);
            convertView.setTag(insoleViewHolder);
        }else{
            insoleViewHolder = (InsoleViewHolder) convertView.getTag();
            insoleViewHolder.init(convertView);
        }

        final String address = list.get(position);
        insoleViewHolder.populate(address);

        return convertView;
    }

    public void add(String address, String name) {
        if(map.containsKey(address)){
            if(name != null){
                if(!name.equals(address)){
                    map.put(address, name);
                }
            }
        }else{
            super.add(address);
            map.put(address, name);
        }
    }

    public void remove(String address) {
        super.remove(address);
        map.remove(address);
    }

    @Override
    public void clear() {
        super.clear();
        list.clear();
        map.clear();
    }

    public String getAddress(int position){
        return list.get(position);
    }

    public String getName(String address){
        return map.get(address);
    }

    public void setIsLeftConnected(boolean isLeftConnected) {
        this.isLeftConnected = isLeftConnected;
    }

    public void setIsRightConnected(boolean isRightConnected) {
        this.isRightConnected = isRightConnected;
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

        void populate(final String address){
            if(address == null){
                return;
            }

            Glove defaultGloveLeft = PreferenceUtils.getGlove(context, GloveFactory.SIDE_LEFT);
            Glove defaultGloveRight = PreferenceUtils.getGlove(context, GloveFactory.SIDE_RIGHT);

            if(defaultGloveLeft != null){
                if(address.equals(defaultGloveLeft.getAddress())){
                    Drawable drawable;
                    PorterDuffColorFilter filter;
                    if(isLeftConnected){
                        drawable = ContextCompat.getDrawable(context, R.drawable.vector_drawable_check);
                        filter = new PorterDuffColorFilter(ContextCompat.getColor(context,android.R.color.holo_green_light), PorterDuff.Mode.MULTIPLY);
                    }else{
                        drawable = ContextCompat.getDrawable(context, R.drawable.vector_drawable_circle);
                        filter = new PorterDuffColorFilter(ContextCompat.getColor(context,R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                    }
                    drawable.setColorFilter(filter);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        state.setBackground(drawable);
                    }
                }
            }

            if(defaultGloveRight != null){
                if(address.equals(defaultGloveRight.getAddress())){
                    Drawable drawable;
                    PorterDuffColorFilter filter;
                    if(isRightConnected){
                        drawable = ContextCompat.getDrawable(context, R.drawable.vector_drawable_check);
                        filter = new PorterDuffColorFilter(ContextCompat.getColor(context,android.R.color.holo_green_light), PorterDuff.Mode.MULTIPLY);
                    }else{
                        drawable = ContextCompat.getDrawable(context, R.drawable.vector_drawable_circle);
                        filter = new PorterDuffColorFilter(ContextCompat.getColor(context,R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                    }
                    drawable.setColorFilter(filter);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        state.setBackground(drawable);
                    }
                }
            }

            this.address.setText(address);
            this.name.setText(map.get(address));
        }
    }
}
