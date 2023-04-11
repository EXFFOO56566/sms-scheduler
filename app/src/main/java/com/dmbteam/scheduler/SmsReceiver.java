package com.dmbteam.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dmbteam.scheduler.util.Constants;

/**
 * Created by dobrikostadinov on 5/9/15.
 */
public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int id = intent.getIntExtra(Constants.KEY_DBITEM_ID, -1);

//        Toast.makeText(context, "Receiver activated with id " + id, Toast.LENGTH_LONG).show();

        Intent myIntent = new Intent(context, SmsService.class);
        myIntent.putExtra(Constants.KEY_DBITEM_ID, id);
        context.startService(myIntent);
    }
}
