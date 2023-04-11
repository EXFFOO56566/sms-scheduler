package com.dmbteam.scheduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.dmbteam.scheduler.db.DatabaseManager;
import com.dmbteam.scheduler.db.DbItem;
import com.dmbteam.scheduler.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dobrikostadinov on 5/9/15.
 */
public class SmsService extends Service {

    private int numMessagesOne = 0;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);


        if (intent != null && intent.getIntExtra(Constants.KEY_DBITEM_ID, -1) > -1) {

            int id = intent.getIntExtra(Constants.KEY_DBITEM_ID, -1);

            final DbItem dbItem = DatabaseManager.getInstance().requestDbItem(id);

            if (dbItem != null && !dbItem.isSent()) {

                showNotification();

                dbItem.setIsSent(true);

                DatabaseManager.getInstance().updateDbItem(dbItem);

                Toast.makeText(getApplicationContext(), "Action was triggered for name(s)" + dbItem.getName() + " and Number(s)" + dbItem.getPhoneNumbersAsList(), Toast.LENGTH_LONG).show();

                Intent refreshHomeFragmentIntent = new Intent();
                refreshHomeFragmentIntent.setAction(Constants.ACTION_REFRESH);
                sendBroadcast(refreshHomeFragmentIntent);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sentSms(dbItem.getPhoneNumbersAsList(), dbItem.getMessage());
                    }
                }).start();
            }
        }
    }

    private void sentSms(List<String> numbers, String message) {

        for (int i = 0; i < numbers.size(); i++) {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);
            sms.sendMultipartTextMessage(numbers.get(i), null, parts, null, null);
        }
    }

    private void showNotification() {

        // Getting Notification Service
        NotificationManager mManager = (NotificationManager) this.getApplicationContext()
                .getSystemService(
                        this.getApplicationContext().NOTIFICATION_SERVICE);
            /*
             * When the user taps the notification we have to show the Home
			 * Screen of our App, this job can be done with the help of the
			 * following Intent.
			 */
        Intent intent1 = new Intent(this.getApplicationContext(),
                MainActivity.class);

        String notifShortMessage = getString(R.string.notification_message);
        if (notifShortMessage.length() > 35) {
            notifShortMessage = notifShortMessage.substring(0, 35) + "...";
        }

        Notification notification = new Notification(R.drawable.ic_launcher, notifShortMessage,
                System.currentTimeMillis());

        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent
                .getActivity(this.getApplicationContext(), 0, intent1,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.setLatestEventInfo(this.getApplicationContext(),
                getApplicationContext().getString(R.string.app_name),
                notifShortMessage, pendingNotificationIntent);

        mManager.notify(99991, notification);
    }

}
