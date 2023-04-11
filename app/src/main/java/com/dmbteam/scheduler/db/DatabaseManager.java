package com.dmbteam.scheduler.db;

import android.content.Context;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dobrikostadinov on 2/20/15.
 */
public class DatabaseManager {

    public static final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager mInstance;
    private Context mContext;
    private DatabaseHelper mHelper;
    private DbItem mDbItem;
    private List<DbItem> mAllItems;

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseManager(context);
        }
    }

    private DatabaseManager(Context ctx) {
        mContext = ctx;
        mHelper = new DatabaseHelper(ctx);
    }

    public static DatabaseManager getInstance() {
        return mInstance;
    }

    public List<DbItem> requestAllDbItems() {
        mAllItems = new ArrayList<DbItem>();

        try {
            mAllItems = mHelper.getDbItemDao().queryForAll();

        } catch (java.sql.SQLException e) {
            Log.i(TAG, "Error fetching records");
        }

        return mAllItems;
    }

    public DbItem requestDbItem(long id) {

        try {
            return mDbItem = mHelper.getDbItemDao().queryForId(id);
        } catch (java.sql.SQLException e) {
            Log.i(TAG, "Error fetching categories");
        } catch (Exception e) {
            Log.i(TAG, "Error fetching categories");
        }

        return null;
    }

    public int createDbItem(DbItem dbItem) {
        try {
            mHelper.getDbItemDao().create(dbItem);

            return dbItem.getId();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void updateDbItem(DbItem dbItem){

        try {
            mHelper.getDbItemDao().update(dbItem);

            requestAllDbItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void markAsSent(long dbItemId) {

        try {
            DbItem dbItem = requestDbItem(dbItemId);

            if (dbItem != null) {
                dbItem.setIsSent(true);
                mHelper.getDbItemDao().update(dbItem);
            }

            requestAllDbItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDbItem(long dbItemId) {

        try {
            mHelper.getDbItemDao().deleteById(dbItemId);

            requestAllDbItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private List<DbItem> getAllDbItems() {
        return mAllItems;
    }

    public List<DbItem> getAllRealItems() {

        if (getAllDbItems() == null) {
            return getAllDbItems();
        } else {
            List<DbItem> filteredItems = new ArrayList<DbItem>();

            for (int i = 0; i < getAllDbItems().size(); i++) {
                if (!getAllDbItems().get(i).isSent()) {

                    filteredItems.add(getAllDbItems().get(i));
                }
            }

            return filteredItems;
        }
    }

    public List<DbItem> getAllSentItems() {

        if (getAllDbItems() == null) {
            return getAllDbItems();
        } else {
            List<DbItem> filteredItems = new ArrayList<DbItem>();

            for (int i = 0; i < getAllDbItems().size(); i++) {
                if (getAllDbItems().get(i).isSent()) {

                    filteredItems.add(getAllDbItems().get(i));
                }
            }

            return filteredItems;
        }
    }

}
