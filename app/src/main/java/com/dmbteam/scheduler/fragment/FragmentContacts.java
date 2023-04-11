package com.dmbteam.scheduler.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.dmbteam.scheduler.MainActivity;
import com.dmbteam.scheduler.R;
import com.dmbteam.scheduler.adapter.AdapterContacts;
import com.dmbteam.scheduler.util.ContactsManager;

/**
 * Created by dobrikostadinov on 5/7/15.
 */
public class FragmentContacts extends Fragment {

    public static final String TAG = FragmentContacts.class.getSimpleName();

    public static FragmentContacts newInstance() {

        FragmentContacts fragmentContacts = new FragmentContacts();

        return fragmentContacts;
    }


    private RecyclerView mRecyclerView;
    private EditText mSearchEditText;
    private AdapterContacts mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fra_contacts, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.fra_contacts_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        mSearchEditText = (EditText) getView().findViewById(R.id.fra_contacts_search);
        mSearchEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);


        mSearchEditText.addTextChangedListener(new FilterSearchTextWatcher());

        mAdapter = new AdapterContacts(ContactsManager.getInstance(getActivity()).getAllContacts(), getActivity());

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).setupContactsActionBar();
    }

    @Override
    public void onPause() {
        super.onPause();

        hideSoftKeyboard();
    }

    private class FilterSearchTextWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAdapter.getFilter().filter(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void hideSoftKeyboard() {
        Context c = getActivity().getBaseContext();
        View v = mSearchEditText.findFocus();
        if (v == null)
            return;
        InputMethodManager inputManager = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
