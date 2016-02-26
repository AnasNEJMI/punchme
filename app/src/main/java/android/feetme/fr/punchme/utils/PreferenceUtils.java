package android.feetme.fr.punchme.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.dao.DaoAccess;
import android.feetme.fr.punchme.dao.DaoSession;
import android.feetme.fr.punchme.dao.Glove;
import android.feetme.fr.punchme.dao.GloveDao;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Created by Anas on 26/02/2016.
 */
public class PreferenceUtils {

    private static final String TAG = PreferenceUtils.class.getSimpleName();

    /**
     * The MAC address of the default gloves.
     */
    public static final String ADDRESS_LEFT = "address_left";
    public static final String ADDRESS_RIGHT = "address_right";

    /**
     * Calibration saved for default insoles. String, Base64 encoded byte array.
     */
    public static final String CALIB_LEFT = "calib_left";
    public static final String CALIB_RIGHT = "calib_right";

    private static void setPreference(Context context, String name, String value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, value);
        editor.commit();
    }

    private static String getPreference(Context context, String name){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(name, null);
    }

    private static void setPreference(Context context, String name, int value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    private static int getIntPreference(Context context, String name, int defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(name, defaultValue);
    }

    public static void clearGlove(Context context, int side){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if(side == GloveFactory.SIDE_LEFT) {
            editor.putString(ADDRESS_LEFT, null);
        }else {
            editor.putString(ADDRESS_RIGHT, null);
        }
        editor.commit();
    }

    public static void setGlove(Context context, Glove glove){

        if(glove != null) {

            DaoAccess access = DaoAccess.getInstance(context);
            DaoSession daoSession = access.openSession();
            GloveDao dao = daoSession.getGloveDao();

            List<Glove> insoles = dao.queryBuilder()
                    .where(GloveDao.Properties.Address.eq(glove.getAddress()))
                    .list();

            dao.insert(glove);

            access.closeSession();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            if(glove.getSide() == GloveFactory.SIDE_LEFT) {
                editor.putString(ADDRESS_LEFT, glove.getAddress());
                editor.putString(CALIB_LEFT, null);
            }else {
                editor.putString(ADDRESS_RIGHT, glove.getAddress());
                editor.putString(CALIB_RIGHT, null);
            }
            editor.commit();

        }
    }

    public static Glove getGlove(Context context, int type){


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String address = (type == GloveFactory.SIDE_LEFT) ? prefs.getString(ADDRESS_LEFT, null) :
                prefs.getString(ADDRESS_RIGHT, null);

        if(address == null){
            return null;
        }else{
            DaoAccess access = DaoAccess.getInstance(context);
            DaoSession daoSession = access.openSession();
            GloveDao dao = daoSession.getGloveDao();
            Glove glove;

            List<Glove> insoles = dao.queryBuilder()
                    .where(GloveDao.Properties.Address.eq(address))
                    .list();
            if(insoles.size() > 0){
                glove = insoles.get(0);
            }else{
                glove = null;
            }

            access.closeSession();

            return glove;
        }
    }
}
