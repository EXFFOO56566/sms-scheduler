package com.dmbteam.scheduler;

import android.app.Application;

import com.dmbteam.scheduler.db.DatabaseManager;
import com.dmbteam.scheduler.util.ContactsManager;

/**
 * Created by dobrikostadinov on 5/7/15.
 */
public class SchedulerContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Crashlytics.start(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContactsManager.getInstance(SchedulerContext.this);

            }
        }).start();

        DatabaseManager.init(SchedulerContext.this);
        DatabaseManager.getInstance().requestAllDbItems();
    }
}
