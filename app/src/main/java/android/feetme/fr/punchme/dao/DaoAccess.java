package android.feetme.fr.punchme.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Anas on 26/02/2016.
 */
public class DaoAccess {

    private final static String DB_NAME = "feetme.db";

    private SQLiteDatabase mDb;
    private DaoMaster mMaster;
    private Context mContext;
    private int sessionCounter;

    private static DaoAccess mDaoAccess;

    public synchronized static DaoAccess getInstance(Context context){
        if(mDaoAccess == null){
            mDaoAccess = new DaoAccess(context);
        }
        return mDaoAccess;
    }

    private DaoAccess(Context context){
        mContext = context;
        sessionCounter = 0;
    }

    private void openDbConnection(){
        if(mDb == null || !mDb.isOpen()){
            DaoMaster.DevOpenHelper openHelper
                    = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
            mDb = openHelper.getWritableDatabase();
            mMaster = new DaoMaster(mDb);
        }
    }

    public synchronized DaoSession openSession(){
        openDbConnection();
        sessionCounter++;
        return mMaster.newSession();
    }

    public synchronized void closeSession(){
        sessionCounter--;
        if(sessionCounter == 0) {
            mDb.close();
            mDb = null;
            mMaster = null;
        }
    }

    /*
     * Test method
     */
    public void createTables(){
        if(mDb == null || !mDb.isOpen()) {
            DaoMaster.DevOpenHelper openHelper
                    = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
            mDb = openHelper.getWritableDatabase();
        }
        DaoMaster.createAllTables(mDb, true);
        mDb.close();
        mDb = null;
    }

    /*
     * Test method
     */
    public void dropTables(){
        if(mDb == null || !mDb.isOpen()) {
            DaoMaster.DevOpenHelper openHelper
                    = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
            mDb = openHelper.getWritableDatabase();
        }
        DaoMaster.dropAllTables(mDb, true);
        mDb.close();
        mDb = null;
    }
}
