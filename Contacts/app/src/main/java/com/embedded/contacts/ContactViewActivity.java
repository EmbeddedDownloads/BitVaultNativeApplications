package com.embedded.contacts;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.embedded.contacts.adapter.PhoneViewAdapter;
import com.embedded.contacts.adapter.PublicKeyViewAdapter;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.task.OnProfileLoadListener;
import com.embedded.contacts.task.ProfileLoader;
import com.embedded.contacts.utils.AndroidAppUtils;
import com.embedded.contacts.utils.BitVaultFont;
import com.embedded.contacts.utils.ContactPreference;
import com.embedded.contacts.utils.Utils;

import static com.embedded.contacts.MainActivity.ADD_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.DELETE_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.UPDATE_CONTACT_CODE;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * Show all details of a contact
 */

public class ContactViewActivity extends AppCompatActivity implements OnDeleteDialogListener, OnProfileLoadListener {

    private ImageView contactImageView;
    private BitVaultFont contactTxt;
    private RecyclerView mobileRecyclerView;
    private RecyclerView publicKeyRecyclerView;

    private CollapsingToolbarLayout collapseLayout;
    private AppBarLayout layout;

    private Contacts contacts;
    private View view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_view_activity);

       initViews();
        getDataFromIntent(getIntent());
    }

    /**
     * Initializing Views
     */
    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        contactImageView = (ImageView) findViewById(R.id.contactImg);
        contactTxt = (BitVaultFont) findViewById(R.id.contactTxt);
        mobileRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewMobile);
        publicKeyRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewPublicKey);
        mobileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        publicKeyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        collapseLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        layout = (AppBarLayout) findViewById(R.id.appBarLayout);
        view = findViewById(R.id.contact_view);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View v = findViewById(R.id.id_action_edit);
                if (v != null) {
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return false;
                        }
                    });
                }
            }
        });
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View v = findViewById(R.id.id_action_delete);
                if (v != null) {
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return false;
                        }
                    });
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_action_edit:
                Intent intent = new Intent(this, AddContactActivity.class);

                if (!(contacts.getPhotoUri() != null && contacts.getPhotoUri().length() > 0)) {
                    intent.putExtra(getResources().getString(R.string.intent_color), contacts.getColorPos());
                }
                if (getIntent().hasExtra(getResources().getString(R.string.intent_title)) &&
                        getIntent().getStringExtra(getResources().getString(R.string.intent_title))
                                .equalsIgnoreCase(getResources().getString(R.string.my_profile))) {
                    intent.putExtra(getResources().getString(R.string.intent_title),
                            getResources().getString(R.string.my_profile));
                } else {
                    intent.putExtra(getResources().getString(R.string.intent_contact_id), contacts.getContactId());
                }
                intent.putExtra(getResources().getString(R.string.intent_edit), getResources().getString(R.string.intent_edit));
                startActivityForResult(intent, MainActivity.UPDATE_CONTACT_CODE);
                return true;
            case R.id.id_action_delete:
                new DeleteContactDialog(this, this).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogButtonPress(boolean isDelete) {
        if (isDelete) {
            Intent intent = new Intent();
            if (getIntent().hasExtra(getResources().getString(R.string.intent_title)) &&
                    getIntent().getStringExtra(getResources().getString(R.string.intent_title))
                            .equalsIgnoreCase(getResources().getString(R.string.my_profile))) {
                ContactHelper.deleteProfile(this);
                ContactPreference.saveProfile(this, -1);
            } else {
                ContactHelper.deleteContact(getContentResolver(), contacts.getContactId());
                intent.putExtra(getResources().getString(R.string.intent_contact_id), contacts.getContactId());
                intent.putExtra(getResources().getString(R.string.intent_delete), 0);
            }
            setResult(DELETE_CONTACT_CODE, intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPDATE_CONTACT_CODE:
                if (data != null) {
                    getDataFromIntent(data);
                }
                break;
            case ADD_CONTACT_CODE:
                if (data != null) {
                    getDataFromIntent(data);
                }
                break;
        }

    }
/*
load contacsts
 */
    private void loadContact() {
        // load contacts
        Cursor nameCursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = "
                        + contacts.getContactId(), null, null);
        if (nameCursor != null && nameCursor.moveToNext()) {
            if (nameCursor.getString(nameCursor.getColumnIndex(getString(R.string.mimetype))).contains(getString(R.string.name))) {
                String name = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.
                        CommonDataKinds.StructuredName.DATA1));

                String fName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.
                        CommonDataKinds.StructuredName.DATA2));
                String lName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.
                        CommonDataKinds.StructuredName.DATA3));

                if (fName != null && fName.length() > 0) {
                    contacts.setFirstName(fName.replace("!9@v",","));
                }
                if (lName != null && lName.length() > 0) {
                    contacts.setLastName(lName.replace("!9@v",","));
                }

                if (name != null && name.length() > 0) {
                    contacts.setName(name);
                    collapseLayout.setTitle(name.replace("!9@v",","));
                }
                nameCursor.close();
            }
        }

        Cursor pCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts._ID + " = ?",
                new String[]{contacts.getContactId()}, null);

        assert pCursor != null;
        pCursor.moveToFirst();
        if (pCursor.getCount() > 0) {
            String uri = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            contacts.setPhotoUri(uri);
            if (uri != null && uri.length() > 0) {
                contactImageView.setVisibility(View.VISIBLE);
                contactTxt.setVisibility(View.GONE);
                contactImageView.setImageURI(Uri.parse(uri));
            } else {
                layout.setBackgroundColor(Utils.getColorCode(this, contacts.getColorPos()));
                contactImageView.setVisibility(View.GONE);
                contactTxt.setVisibility(View.VISIBLE);
            }
            pCursor.close();
        }

        AndroidAppUtils.showLogD(getResources().getString(R.string.MyContact), getResources().getString(R.string.contact_ID_in_View) + contacts.getContactId());
        contacts.setListNumber(Utils.getMobileList(this, contacts.getContactId()));
        contacts.setListPublicKey(Utils.getKeyList(this, contacts.getContactId(), contacts.getName()));
        if (contacts.getListPublicKey().size() == 0)
            contacts.setSecure(false);
        else
            contacts.setSecure(true);
        mobileRecyclerView.setAdapter(new PhoneViewAdapter(this, contacts.getListNumber()));
        publicKeyRecyclerView.setAdapter(new PublicKeyViewAdapter(this, contacts.getListPublicKey()));
        if (contacts.getListNumber().size() > 0 && contacts.getListPublicKey().size() > 0) {
            view.setVisibility(View.VISIBLE);
        }

    }
/*
Load profile
 */

    private void loadProfile() {
        //Load profile
        Cursor pCursor = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);

        assert pCursor != null;
        pCursor.moveToFirst();
        if (pCursor.getCount() > 0) {

            String name = pCursor.getString(pCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            contacts.setName(name);
            collapseLayout.setTitle(contacts.getName());
            String uri = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            contacts.setPhotoUri(uri);
            if (uri != null && uri.length() > 0) {
                contactImageView.setVisibility(View.VISIBLE);
                contactTxt.setVisibility(View.GONE);
                contactImageView.setImageURI(Uri.parse(uri));
            } else {
                contacts.setColorPos(5);
                layout.setBackgroundColor(Utils.getColorCode(this, 5));
                contactImageView.setVisibility(View.GONE);
                contactTxt.setVisibility(View.VISIBLE);
            }
            pCursor.close();

            new ProfileLoader(this, this);
        }

    }

    /**
     * Display data from intent
     */
    private void getDataFromIntent(Intent intent) {
        contacts = new Contacts();

        if (intent.hasExtra(getResources().getString(R.string.intent_title)) &&
                intent.getStringExtra(getResources().getString(R.string.intent_title))
                        .equalsIgnoreCase(getResources().getString(R.string.my_profile))) {
            loadProfile();
        } else {
            String contactId = intent.getStringExtra(getResources().getString(R.string.intent_contact_id));
            contacts.setContactId(contactId);
            if (intent.hasExtra(getResources().getString(R.string.intent_color))) {
                contacts.setColorPos(intent.getIntExtra(getResources().getString(R.string.intent_color), 0));
            }
            loadContact();
        }

    }


    @Override
    public void onProfileLoadComplete(Cursor cursor) {
        Utils.setContactData(contacts, cursor);
        cursor.close();
        mobileRecyclerView.setAdapter(new PhoneViewAdapter(this, contacts.getListNumber()));
        publicKeyRecyclerView.setAdapter(new PublicKeyViewAdapter(this, contacts.getListPublicKey()));

    }
}
