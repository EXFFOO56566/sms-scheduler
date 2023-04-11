package com.dmbteam.scheduler.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmbteam.scheduler.MainActivity;
import com.dmbteam.scheduler.R;
import com.dmbteam.scheduler.adapter.AdapterScheduled;
import com.dmbteam.scheduler.db.DatabaseManager;
import com.dmbteam.scheduler.db.DbItem;
import com.dmbteam.scheduler.settings.AppConstants;
import com.dmbteam.scheduler.util.Constants;
import com.dmbteam.scheduler.util.ContactsManager;
import com.dmbteam.scheduler.util.SwipeDismissRecyclerViewTouchListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dobrikostadinov on 5/7/15.
 */
public class FragmentHome extends Fragment {

    public static final String TAG = FragmentHome.class.getSimpleName();

    private int undoPosition;
    private DbItem undoObject;

    private View addBtn;
    private View mEmptyContainerScheduled;
    private View mContainerDataView;
    private RecyclerView mRecyclerView;
    private AdapterScheduled mAdapterScheduled;
    private View mTabScheduled;
    private View mTabSent;
    private SwipeDismissRecyclerViewTouchListener mSwipeRecyclerListener;
    private LinearLayoutManager mLayoutManager;
    private View mUndoContainer;
    private View mUndoBtn;
    private Handler undoHandler;
    private BroadcastReceiver mRefreshReceiver;
    private IntentFilter mIntentFilter;
    private View mEmptyContainerSent;
    private AdView mAdmobView;
    private View mCancelledBtn;

    public static FragmentHome newInstance() {

        FragmentHome fragmentHome = new FragmentHome();

        return fragmentHome;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fra_home, null);
    }

    @Override
    public void onResume() {
        super.onResume();

        ContactsManager.getInstance(getActivity()).getAllScheduledContacts().clear();

        ((MainActivity) getActivity()).setupHomeActionBar();

        mRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (mTabScheduled.isSelected()) {
                    mTabScheduled.performClick();
                    //mAdapterScheduled = new AdapterScheduled(getActivity(), DatabaseManager.getInstance().getAllRealItems());
                    //mRecyclerView.setAdapter(mAdapterScheduled);
                } else if (mTabSent.isSelected()) {
                    mTabSent.performClick();
                    //mAdapterScheduled = new AdapterScheduled(getActivity(), DatabaseManager.getInstance().getAllSentItems());
                    //mRecyclerView.setAdapter(mAdapterScheduled);
                }
            }
        };

        mIntentFilter = new IntentFilter(Constants.ACTION_REFRESH);

        getActivity().registerReceiver(mRefreshReceiver, mIntentFilter);

        mTabScheduled.performClick();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mRefreshReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mEmptyContainerSent = getView().findViewById(R.id.fra_home_empty_container_scheduled);

        mUndoContainer = getView().findViewById(R.id.fra_home_undo_container);
        mUndoBtn = getView().findViewById(R.id.fra_home_undo_btn);

        mCancelledBtn = getView().findViewById(R.id.fra_home_cancelled_btn);
        setCancelledBtnListener();

        mEmptyContainerScheduled = getView().findViewById(R.id.fra_home_empty_container_sent);
        mContainerDataView = getView().findViewById(R.id.fra_home_content);

        addBtn = getView().findViewById(R.id.fra_home_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showComposeScreen();
            }
        });

        mTabScheduled = getView().findViewById(R.id.scheduled_tab);
        mTabScheduled.setSelected(true);
        mTabSent = getView().findViewById(R.id.sent_tab);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.fra_home_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        setupSwipeListener();
        setUndoBtnListener();
        setTabListeners();

        initAdmob();
    }

    private void setupSwipeListener() {

        mSwipeRecyclerListener = new SwipeDismissRecyclerViewTouchListener(
                mRecyclerView,
                new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(RecyclerView recyclerView,
                                          int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {

                            List<DbItem> realItems = new ArrayList<DbItem>(DatabaseManager.getInstance().getAllRealItems());

                            mUndoContainer.setVisibility(View.VISIBLE);

                            undoPosition = position;
                            undoObject = new ArrayList<DbItem>(realItems).get(undoPosition);

                            undoHandler = new Handler();

                            undoHandler.postDelayed(undoRunnable, 3000);


                            // TODO: this is temp solution for preventing
                            // blinking item onDismiss
                            mLayoutManager.findViewByPosition(position)
                                    .setVisibility(View.GONE);

                            realItems.remove(position); // mItems.remove(position);

                            mAdapterScheduled = new AdapterScheduled(getActivity(), realItems);
                            mRecyclerView.setAdapter(mAdapterScheduled);
                        }
                    }
                });
    }

    private void setTabListeners() {

        mTabScheduled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setOnTouchListener(mSwipeRecyclerListener);
                mRecyclerView.setOnScrollListener(mSwipeRecyclerListener.makeScrollListener());

                mTabScheduled.setSelected(true);
                mTabSent.setSelected(false);

                addBtn.setVisibility(View.VISIBLE);

                mEmptyContainerSent.setVisibility(View.GONE);

                List<DbItem> realItems = DatabaseManager.getInstance().getAllRealItems();

                if (realItems.size() == 0) {
                    mEmptyContainerScheduled.setVisibility(View.VISIBLE);
                    mContainerDataView.setVisibility(View.GONE);
                } else {
                    mEmptyContainerScheduled.setVisibility(View.GONE);
                    mContainerDataView.setVisibility(View.VISIBLE);

                    mAdapterScheduled = new AdapterScheduled(getActivity(), realItems);
                    mRecyclerView.setAdapter(mAdapterScheduled);
                }
            }
        });

        mTabSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setOnTouchListener(null);
                mRecyclerView.setOnScrollListener(null);


                mTabScheduled.setSelected(false);
                mTabSent.setSelected(true);

                addBtn.setVisibility(View.GONE);

                List<DbItem> sentItems = DatabaseManager.getInstance().getAllSentItems();

                mEmptyContainerScheduled.setVisibility(View.GONE);

                mContainerDataView.setVisibility(View.VISIBLE);

                if (sentItems.size() == 0) {
                    mEmptyContainerSent.setVisibility(View.VISIBLE);
                }

                mAdapterScheduled = new AdapterScheduled(getActivity(), sentItems);
                mRecyclerView.setAdapter(mAdapterScheduled);
            }
        });
    }

    protected void initAdmob() {
        mAdmobView = (AdView) getView().findViewById(R.id.home_admob);

        if (mAdmobView != null) {

            if (AppConstants.isAdmobEnabledHomeScreen) {
                mRecyclerView.setPadding(0, 0, 0, (int) (getResources().getDisplayMetrics().density * 50));

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


    private void setUndoBtnListener() {

        mUndoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<DbItem> realItems = DatabaseManager.getInstance().getAllRealItems();

                undoHandler.removeCallbacks(undoRunnable);
                mUndoContainer.setVisibility(View.GONE);

                mAdapterScheduled = new AdapterScheduled(getActivity(), realItems);
                mRecyclerView.setAdapter(mAdapterScheduled);
            }
        });
    }

    private void setCancelledBtnListener() {
        mCancelledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUndoContainer.setVisibility(View.GONE);
            }
        });
    }


    Runnable undoRunnable = new Runnable() {
        @Override
        public void run() {
            mUndoContainer.setVisibility(View.GONE);

            DatabaseManager.getInstance().deleteDbItem(undoObject.getId());

            mTabScheduled.performClick();
        }
    };
}