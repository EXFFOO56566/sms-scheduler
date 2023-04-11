package com.dmbteam.scheduler.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dmbteam.scheduler.MainActivity;
import com.dmbteam.scheduler.R;
import com.dmbteam.scheduler.SmsReceiver;
import com.dmbteam.scheduler.adapter.AdapterCompose;
import com.dmbteam.scheduler.cmn.Contact;
import com.dmbteam.scheduler.db.DatabaseManager;
import com.dmbteam.scheduler.db.DbItem;
import com.dmbteam.scheduler.settings.AppConstants;
import com.dmbteam.scheduler.util.Constants;
import com.dmbteam.scheduler.util.ContactsManager;
import com.dmbteam.scheduler.widget.ExpandableHeightGridView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by dobrikostadinov on 5/7/15.
 */
public class FragmentCompose extends Fragment {
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMMM-yyyy");
    public static final NumberFormat TIME_FORMATTER = new DecimalFormat("00");


    private long MILISECS_IN_HOUR = 1000 * 60 * 60;
    private long MILISECS_IN_MIN = 1000 * 60;

    public static final String TAG = FragmentCompose.class.getSimpleName();

    private GestureDetector gestureDetector;

    private ExpandableHeightGridView mGridView;
    private AdapterCompose mAdapter;
    private EditText mDateView;
    private EditText mTimeView;
    private EditText mMessage;
    private Calendar setDateCalendar = Calendar.getInstance();
    private long mSetTime;
    private View mScheduleBtn;
    private View mDelimiterGridView;
    private float density;
    private AdView mAdmobView;


    public static FragmentCompose newInstance() {

        FragmentCompose fragmentCompose = new FragmentCompose();

        return fragmentCompose;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        density = getActivity().getResources().getDisplayMetrics().density;

        return inflater.inflate(R.layout.fra_compose, null);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).setupComposeActionBar();

        refreshList();
    }

    @Override
    public void onPause() {
        super.onPause();

        hideSoftKeyboard();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initAdmob();

        gestureDetector = new GestureDetector(getActivity(), new OnSwipeGestureListener());

        mGridView = (ExpandableHeightGridView) getView().findViewById(R.id.fra_compose_gridview);
        mGridView.setExpanded(true);

        mMessage = (EditText) getView().findViewById(R.id.fra_contacts_message);
        setupMessageWatcher();

        mDateView = (EditText) getView().findViewById(R.id.fra_contacts_date);

        mDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });

        mDateView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDateDialog();
                }
            }
        });

        mTimeView = (EditText) getView().findViewById(R.id.fra_contacts_time);
        mTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog();
            }
        });

        mTimeView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimeDialog();
                }
            }
        });

        mScheduleBtn = getView().findViewById(R.id.fra_compose_schedule);
        mScheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDbItem();
            }
        });

        mDelimiterGridView = getView().findViewById(R.id.fra_compose_delimiter_gridview);
        makeGridViewBorderThin();

        refreshList();
    }

    private void makeGridViewBorderThin() {
        mDelimiterGridView.setSelected(false);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                mDelimiterGridView.getLayoutParams();
        params.height = (int) density;
        mDelimiterGridView.setLayoutParams(params);

    }

    private void makeGridViewBorderBig() {
        mDelimiterGridView.setSelected(true);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                mDelimiterGridView.getLayoutParams();
        params.height = (int) (2 * density);
        mDelimiterGridView.setLayoutParams(params);

    }


    public void refreshList() {

        List<Contact> mScheduledContacts = ContactsManager.getInstance(getActivity()).getAllScheduledContacts();

        if (mScheduledContacts.size() == 0) {

            makeGridViewBorderThin();

            mScheduledContacts.add(new Contact(-1));
            mScheduledContacts.add(new Contact(-1));

            mGridView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        } else {
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });

            makeGridViewBorderBig();

            if (mScheduledContacts.size() > 2 && mScheduledContacts.get(0).getId() == -1 && mScheduledContacts.get(1).getId() == -1) {

                mScheduledContacts.remove(0);
                mScheduledContacts.remove(0);
            }
        }

        if (mScheduledContacts.size() == 1) {
            mGridView.setNumColumns(1);
            mGridView.setColumnWidth(800);
        } else {
            mGridView.setNumColumns(2);
        }

        mAdapter = new AdapterCompose(getActivity(), 0, mScheduledContacts);

        mGridView.setAdapter(mAdapter);
    }

    private void showDateDialog() {

        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
                        setDateCalendar = Calendar.getInstance();
                        setDateCalendar.set(Calendar.YEAR, i);
                        setDateCalendar.set(Calendar.MONTH, i1);
                        setDateCalendar.set(Calendar.DAY_OF_MONTH, i2);
                        setDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        setDateCalendar.set(Calendar.MINUTE, 0);
                        setDateCalendar.set(Calendar.SECOND, 0);

                        mDateView.setText(DATE_FORMATTER.format(setDateCalendar.getTime()));

                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }

    private void showTimeDialog() {

        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i1) {

                mSetTime = i * MILISECS_IN_HOUR + i1 * MILISECS_IN_MIN;

                mTimeView.setText(TIME_FORMATTER.format(i) + ":" + TIME_FORMATTER.format(i1));

            }
        }, 12, 00, true);

        timePickerDialog.show(getActivity().getFragmentManager(), "TimePickerDialog");

    }

    private void createDbItem() {

        DbItem dbItem = new DbItem();

        dbItem.setTime(mSetTime);
        dbItem.setDate(setDateCalendar.getTimeInMillis());

        String contacts = "";

        for (int i = 0; i < mAdapter.getCount(); i++) {
            contacts += mAdapter.getItem(i).getName() + "^" + mAdapter.getItem(i).getPhone() + ";";
        }

        dbItem.setNumbers(contacts);

        dbItem.setMessage(mMessage.getText().toString());

        if (validateInputData(dbItem)) {
            int createdItemId = DatabaseManager.getInstance().createDbItem(dbItem);

            if (createdItemId > -1) {

                DatabaseManager.getInstance().requestAllDbItems();

                setupFutureTask(dbItem.getDateAndTimeAsDate(), createdItemId);

                getActivity().getSupportFragmentManager().popBackStack();

                ContactsManager.getInstance(getActivity()).getAllScheduledContacts().clear();
            }
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.error_scheduling), Toast.LENGTH_LONG).show();
        }
    }

    private void setupFutureTask(Date date, int dbItemId) {

        Intent myIntent = new Intent(getActivity(), SmsReceiver.class);
        myIntent.putExtra(Constants.KEY_DBITEM_ID, dbItemId);
        myIntent.setAction("action_" + dbItemId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),
                0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getActivity()
                .getSystemService(Activity.ALARM_SERVICE);

        //Toast.makeText(getActivity(), "Scheduled alarm for " + date.toString(), Toast.LENGTH_LONG).show();

        alarmManager.set(AlarmManager.RTC, date.getTime(),
                pendingIntent);
    }


    private boolean validateInputData(DbItem dbItem) {

        if (!dbItem.isValidNumbers()) {
            Toast.makeText(getActivity(), "Please select at least one contact", Toast.LENGTH_LONG).show();

            return false;
        }


        if (dbItem.getMessage().length() == 0) {
            Toast.makeText(getActivity(), "Please set valid sms message", Toast.LENGTH_LONG).show();

            return false;
        }

        if (dbItem.getDateAndTimeAsDate().getTime() - new Date().getTime() < MILISECS_IN_MIN) {
            Toast.makeText(getActivity(), "Please set correct time for sending the Sms", Toast.LENGTH_LONG).show();

            return false;
        }

        return true;

    }

    private void hideSoftKeyboard() {
        Context c = getActivity().getBaseContext();
        View v = mMessage.findFocus();
        if (v == null)
            return;
        InputMethodManager inputManager = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void setupMessageWatcher() {

    }

    protected void initAdmob() {
        mAdmobView = (AdView) getView().findViewById(R.id.compose_admob);

        if (mAdmobView != null) {

            if (AppConstants.isAdmobEnabledComposeScreen) {
                mAdmobView.setVisibility(View.VISIBLE);
                AdRequest.Builder builder = new AdRequest.Builder();
                AdRequest adRequest = builder.build();

                // Start loading the ad in the background.
                mAdmobView.loadAd(adRequest);

            } else {
                mAdmobView.setVisibility(View.GONE);
            }
        }
    }

    private final class OnSwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            ((MainActivity) getActivity()).showContactsScreen();

            return super.onDown(e);
        }
    }

}
