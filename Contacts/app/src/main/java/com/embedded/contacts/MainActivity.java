package com.embedded.contacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.embedded.contacts.fragment.AllContactFragment;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.task.LoadAllDetail;
import com.embedded.contacts.task.OnContactsReadListener;
import com.embedded.contacts.task.OnProfileLoadListener;
import com.embedded.contacts.task.ProfileLoader;
import com.embedded.contacts.utils.AndroidAppUtils;
import com.embedded.contacts.utils.BitVaultFont;
import com.embedded.contacts.utils.ContactPreference;
import com.embedded.contacts.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JavaDoc")
public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnContactsReadListener,
        OnProfileLoadListener, TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    public static final int DELETE_CONTACT_CODE = 201;
    public static final int ADD_CONTACT_CODE = 301;
    public static final int ADD_PROFILE_CODE = 601;
    public static final int UPDATE_CONTACT_CODE = 401;
    public static final int SEARCH_CONTACT_CODE = 501;
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int PERMISSION_REQUEST_CODE = 101;
    public static List<Contacts> mainContactList;
    private String contactID;
    private TextView searchTxt;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyPageAdapter adapter;
    private MainActivity mainActivity;
    private String profileID;
    private String profileName;
    private String profilePhotoUri;
    private boolean isProfileSecure;
    private boolean isSecureSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        mainContactList = new ArrayList<>();
        initViews();
    }

    /**
     * Initializing views
     */
    private void initViews() {
        mainActivity = this;
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all_contacts)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.secure_contacts)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        BitVaultFont contactAddTxt = (BitVaultFont) findViewById(R.id.id_add_contact_txt);
        contactAddTxt.setOnClickListener(this);
        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.addOnPageChangeListener(this);
        //Creating our pager adapter
        adapter = new MyPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);

        searchTxt = (TextView) findViewById(R.id.id_search_txt);
        searchTxt.setOnClickListener(mainActivity);
        openPermissionDialog();
        tabLayout.setSelectedTabIndicatorColor(getColor(R.color.line_color));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(getString(R.string.all_contacts));
        tabLayout.getTabAt(1).setText(getString(R.string.secure_contacts));
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_search_txt:
                Intent intent = new Intent(mainActivity, ContactSearchActivity.class);
                intent.putExtra(getResources().getString(R.string.secure_contacts), isSecureSelected);
                startActivityForResult(intent, SEARCH_CONTACT_CODE);
                break;
            case R.id.id_add_contact_txt:
                intent = new Intent(mainActivity, AddContactActivity.class);
                startActivityForResult(intent, ADD_CONTACT_CODE);
                break;
        }
    }

    /*
    Load profile
     */
    private void loadProfile() {
        Cursor pCursor =
                getContentResolver().query(ContactsContract.Profile.CONTENT_URI,
                        null, null, null, null);

        if (pCursor != null) {
            pCursor.moveToFirst();

            if (pCursor.getCount() == 0) {
                profileID = null;
            } else {
                profileID = pCursor.getString(pCursor.getColumnIndex(ContactsContract.Contacts._ID));
                profileName = pCursor.getString(pCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                profilePhotoUri = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            }
            pCursor.close();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            loadProfile();
        }

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.hasExtra(getString(R.string.saveContactByMessaging))) {
            String key = intent.getStringExtra(getString(R.string.saveContactByMessaging));
            Intent addcontact = new Intent(this, AddContactActivity.class);
            addcontact.putExtra(getString(R.string.saveContactByMessaging), key);
            startActivity(addcontact);
            finish();
        }
    }

    /**
     * Check runtime permission
     */
    private void openPermissionDialog() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            loadContacts();
            loadProfile();
        }
    }

    /**
     * Request runtime permission
     */
    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, Manifest.permission.READ_CONTACTS)
                && ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, Manifest.permission.WRITE_CONTACTS)) {
            AndroidAppUtils.showLogD(TAG, getString(R.string.permission_already_granted));
        } else {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AndroidAppUtils.showLogD(TAG, getString(R.string.permission_granted));
                    loadContacts();
                    loadProfile();
                } else {
                    AndroidAppUtils.showLogD(TAG, getString(R.string.permission_not_granted));
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DELETE_CONTACT_CODE:
                contactID = null;
                if (data != (null) && data.hasExtra(getResources().getString(R.string.intent_contact_id))) {
                    for (int i = 0; i < mainContactList.size(); i++) {
                        if (mainContactList.get(i).getContactId().
                                equals(data.getStringExtra(getResources().getString(R.string.intent_contact_id)))) {
                            mainContactList.remove(i);
                        }
                    }
                    for (int i = 0; i < adapter.getFragmentList().size(); i++) {
                        ((AllContactFragment) adapter.getFragmentList().get(i)).updateAdapter();
                    }
                }
//                loadContacts();
                break;
            case SEARCH_CONTACT_CODE:
                loadContacts();
                break;
            case ADD_CONTACT_CODE:
                if (data != null && data.hasExtra(getResources().getString(R.string.intent_code))) {
                    if (data.getIntExtra(getResources().getString(R.string.intent_code), 0) == ADD_PROFILE_CODE) {
                        viewProfile();
                    } else if (data.getIntExtra(getResources().getString(R.string.intent_code), 0) == ADD_CONTACT_CODE) {
                        contactID = data.getStringExtra(getResources().getString(R.string.intent_contact_id));
                        loadContacts();
                    } else if (data.getIntExtra(getResources().getString(R.string.intent_code), 0) == UPDATE_CONTACT_CODE) {
                        contactID = data.getStringExtra(getResources().getString(R.string.intent_contact_id));
                        loadContacts();
                    }
                }
                break;
            default:
                loadContacts();
                break;
        }
    }

    @Override
    public void onReadComplete(List<Contacts> list) {
        if (mainContactList != null) {
            mainContactList.clear();
        }
        mainContactList.addAll(list);
        if (contactID != null && !contactID.equals("null")) {
            long saveid = ContactPreference.getLong(this, ContactPreference.KEY);
            if (Long.parseLong(contactID) < saveid)
                contactID = String.valueOf(saveid);
            Intent intent = new Intent(mainActivity, ContactViewActivity.class);
            Contacts contacts = Utils.getContactsById(mainContactList, contactID);
            AndroidAppUtils.showLogD(getString(R.string.MyContact), getString(R.string.contact_ID_after) + contactID);

            if (contacts != null) {
                AndroidAppUtils.showLogD(getString(R.string.MyContact), getString(R.string.contact_id_in_object) + contacts.getContactId());
                intent.putExtra(getResources().getString(R.string.intent_contact_id), contacts.getContactId());
                if (!(contacts.getPhotoUri() != null && contacts.getPhotoUri().length() > 0)) {
                    intent.putExtra(getResources().getString(R.string.intent_color), contacts.getColorPos());
                }
                startActivityForResult(intent, DELETE_CONTACT_CODE);
            }
            contactID = null;
        }
        for (int i = 0; i < adapter.getFragmentList().size(); i++) {
            ((AllContactFragment) adapter.getFragmentList().get(i)).updateAdapter();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new ProfileLoader(MainActivity.this, MainActivity.this);
            }
        }, 0);
    }

    /*
    open contact view activity to view full profile
     */
    private void viewProfile() {
        Intent intent = new Intent(mainActivity, ContactViewActivity.class);
        intent.putExtra(getResources().getString(R.string.intent_title), getResources().getString(R.string.my_profile));
        startActivityForResult(intent, DELETE_CONTACT_CODE);
    }

    /*
    Load contacts
     */
    private void loadContacts() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new LoadAllDetail(mainActivity, mainActivity);
            }
        }, 0);
    }


    @Override
    public void onProfileLoadComplete(Cursor cursor) {
        if (cursor.getCount() > 0) {
            isProfileSecure = Utils.isProfileContainsKey(cursor);
            ContactPreference.saveProfile(this, isProfileSecure ? Contacts.SECURE : Contacts.UN_SECURE);
        } else {
            isProfileSecure = false;
        }
        cursor.close();

        for (int i = 0; i < adapter.getFragmentList().size(); i++) {
            ((AllContactFragment) adapter.getFragmentList().get(i)).loadProfile();
        }
    }


    public String getContactID() {
        return contactID;
    }

    public String getProfileID() {
        return profileID;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getProfilePhotoUri() {
        return profilePhotoUri;
    }

    public boolean isProfileSecure() {
        return isProfileSecure;
    }

    public boolean isSecureSelected() {
        return isSecureSelected;
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
        isSecureSelected = tab.getPosition() == 1;
        for (int i = 0; i < adapter.getFragmentList().size(); i++) {
            ((AllContactFragment) adapter.getFragmentList().get(i)).updateAdapter();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //tabLayout.getTabAt(position).getCustomView().setBackgroundColor(getColor(R.color.white));
    }

    @Override
    public void onPageSelected(int position) {
        isSecureSelected = position == 1;
        for (int i = 0; i < adapter.getFragmentList().size(); i++) {
            ((AllContactFragment) adapter.getFragmentList().get(i)).updateAdapter();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {


    }

}
