package com.dmbteam.scheduler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dmbteam.scheduler.cmn.Contact;
import com.dmbteam.scheduler.fragment.FragmentCompose;
import com.dmbteam.scheduler.fragment.FragmentContacts;
import com.dmbteam.scheduler.fragment.FragmentHome;
import com.dmbteam.scheduler.util.ContactsManager;
import com.dmbteam.scheduler.util.ImageOptionsBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupImageLoader();

        initToolbar();
        setupHomeActionBar();
        showHomeScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Initializes the toolbar
     */
    protected void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    public void setupHomeActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");
    }

    public void setupContactsActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.back_header_contact));
    }

    public void setupComposeActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void setupImageLoader() {
        ImageLoaderConfiguration imageLoaderConfiguration = ImageOptionsBuilder
                .createImageLoaderConfiguration(this);
        ImageLoader.getInstance().init(imageLoaderConfiguration);

    }

    public void showHomeScreen() {

        showScreen(FragmentHome.newInstance(), FragmentHome.TAG, false, false);
    }

    public void showComposeScreen() {

        setupComposeActionBar();

        ContactsManager contactsManager = ContactsManager.getInstance(this);

        showScreen(FragmentCompose.newInstance(), FragmentCompose.TAG, true, false);
    }


    public void showContactsScreen() {

        showScreen(FragmentContacts.newInstance(), FragmentContacts.TAG, true, false);
    }


    private void showScreen(Fragment content,
                            String contentTag, boolean addToBackStack, boolean clearBackStack) {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.left_slide_in, R.animator.left_slide_out,
                R.animator.right_slide_in, R.animator.right_slide_out);


        ft.replace(R.id.activity_main_content, content, contentTag);


        if (clearBackStack) {
            fm.popBackStackImmediate(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        if (addToBackStack) {
            ft.addToBackStack(String.valueOf(System.identityHashCode(content)));
        }

        ft.commitAllowingStateLoss();
        fm.executePendingTransactions();
    }

    public void refreshComposeFragmentList() {

        Fragment fragmentCompose = getSupportFragmentManager().findFragmentByTag(FragmentCompose.TAG);

        if (fragmentCompose != null) {
            ((FragmentCompose) fragmentCompose).refreshList();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Fragment fragmentContact = getSupportFragmentManager().findFragmentByTag(FragmentContacts.TAG);

                if (fragmentContact == null) {
                    ContactsManager.getInstance(this).getAllScheduledContacts().clear();
                } else {

                    List<Contact> mScheduledContacts = ContactsManager.getInstance(this).getAllScheduledContacts();

                    if (mScheduledContacts.size() >= 2 && mScheduledContacts.get(0).getId() == -1 && mScheduledContacts.get(1).getId() == -1) {

                        mScheduledContacts.remove(0);
                        mScheduledContacts.remove(0);
                    }
                }

                getSupportFragmentManager().popBackStack();

                break;
            case R.id.rate:

                rateApp();

                break;
        }
        return true;
    }

    private void rateApp() {
        final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
            startActivity(rateAppIntent);
        }
    }

    @Override
    public void onBackPressed() {

        int a = 10;

        super.onBackPressed();
    }
}
