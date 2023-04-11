package com.dmbteam.scheduler.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by dobrikostadinov on 2/20/15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String LOG_TAG = DatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "smsscheduler.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<DbItem, Long> mDbItemDao = null;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, DbItem.class);
            TableUtils.createTable(connectionSource, DbItem.class);
        } catch (java.sql.SQLException e) {
            Log.e(LOG_TAG, "Error creating database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, DbItem.class, true);
            TableUtils.createTable(connectionSource, DbItem.class);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error upgrading database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns Category Dao object
     *
     * @return
     * @throws SQLException
     */
    public Dao<DbItem, Long> getDbItemDao() throws SQLException {
        if (mDbItemDao == null) {
            mDbItemDao = getDao(DbItem.class);
        }

        return mDbItemDao;
    }
}
