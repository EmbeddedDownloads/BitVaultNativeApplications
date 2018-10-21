package com.embedded.contacts;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.embedded.contacts.adapter.ContactsListAdapter;
import com.embedded.contacts.fragment.AllContactFragment;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.model.listcount;
import com.embedded.contacts.task.LoadAllDetail;
import com.embedded.contacts.task.OnContactsReadListener;
import com.embedded.contacts.task.OnProfileLoadListener;
import com.embedded.contacts.task.ProfileLoader;
import com.embedded.contacts.utils.AndroidAppUtils;
import com.embedded.contacts.utils.BitVaultFont;
import com.embedded.contacts.utils.ContactPreference;
import com.embedded.contacts.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.embedded.contacts.MainActivity.ADD_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.DELETE_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.SEARCH_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.mainContactList;

/**
 * Created Dheeraj Bansal root on 12/6/17.
 * version 1.0.0
 * Search contact
 */

@SuppressWarnings("JavaDoc")
public class ContactSearchActivity extends AppCompatActivity implements View.OnClickListener,OnContactsReadListener {


    private EditText searchEdt;
    private String contactID;
    private ContactsListAdapter adapter;
    private List<Contacts> tempContactsList;
    private List<Contacts> mainContactsList;
    private listcount mlistcount;
    private ListView searchListView;
    private TextView nocontactTxt;
    private ContactSearchActivity contactSearchActivity;
    private boolean isSecuredContacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);
        contactSearchActivity = this;
        mlistcount = new listcount();
        isSecuredContacts = getIntent().getBooleanExtra(getResources().getString(R.string.secure_contacts), false);
        initViews();

    }

    private void initViews() {
        searchListView = (ListView) findViewById(R.id.id_search_listview);
        searchEdt = (EditText) findViewById(R.id.id_search_edt);
        nocontactTxt = (TextView) findViewById(R.id.nocontactsTxt);
        BitVaultFont backTxt = (BitVaultFont) findViewById(R.id.id_search_back_txt);
        final BitVaultFont closeTxt = (BitVaultFont) findViewById(R.id.id_search_close_txt);
        backTxt.setOnClickListener(this);
        closeTxt.setOnClickListener(this);

        tempContactsList = new ArrayList<>();
        mainContactsList = new ArrayList<>();
        if(mainContactList != null){
            mainContactsList.addAll(mainContactList);}
        if (isSecuredContacts) {
            for (int i = 0; i < mainContactsList.size(); i++) {
                if (mainContactsList.get(i).isSecure()) {
                    tempContactsList.add(mainContactsList.get(i));
                }
            }
            mainContactsList.clear();
            mainContactsList.addAll(tempContactsList);
        } else {
            tempContactsList.addAll(mainContactsList);
        }
        adapter = new ContactsListAdapter(this, tempContactsList);
        adapter.setSearchMode(true);
        adapter.setSecuredSelected(isSecuredContacts);
        searchListView.setAdapter(adapter);
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (searchEdt.getText().toString().length() > 0) {
                    filterContact(searchEdt.getText().toString());
                    closeTxt.setVisibility(View.VISIBLE);
                } else {
                    if (mainContactsList.size() == 0) {
                        nocontactTxt.setVisibility(View.VISIBLE);
                        searchListView.setVisibility(View.GONE);
                    } else {
                        tempContactsList.clear();
                        tempContactsList.addAll(mainContactsList);
                        adapter.updateDataSet(tempContactsList);
                        nocontactTxt.setVisibility(View.GONE);
                        searchListView.setVisibility(View.VISIBLE);
                    }
                    closeTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(ContactSearchActivity.this, ContactViewActivity.class);
                Contacts contacts = tempContactsList.get(i);
                intent.putExtra(getResources().getString(R.string.intent_contact_id), contacts.getContactId());
                if (!(contacts.getPhotoUri() != null && contacts.getPhotoUri().length() > 0)) {
                    intent.putExtra(getResources().getString(R.string.intent_color), contacts.getColorPos());
                }
                startActivityForResult(intent, DELETE_CONTACT_CODE);
            }
        });
    }

    /**
     * Update color code
     *
     * @param position
     * @param colorPos
     */
    public void updateList(int position, int colorPos) {
        if (tempContactsList.size() > position) {
            tempContactsList.get(position).setColorPos(colorPos);
        }
    }

    /**
     * Search form list
     *
     * @param keyword
     */
    private void filterContact(String keyword) {
        // Filter search by number , name and public key
        tempContactsList.clear();
        int mainlistSize = mainContactsList.size();
        for (int i = 0; i < mainlistSize; i++) {
            int listnum_size = mainContactsList.get(i).getListNumber().size();
            int publickey_size = mainContactsList.get(i).getListPublicKey().size();

            if (listnum_size > 0) { // search by number
                for (int j = 1; j <= listnum_size; j++) {
                    if (mainContactsList.get(i).getListNumber().get(listnum_size - j).getNumber().substring(0, mainContactsList.get(i).getListNumber().get(listnum_size - j).getNumber().length()).contains(keyword)) {
                        mlistcount.setlistnumber(listnum_size - j);
                        tempContactsList.add(mainContactsList.get(i));
                    }
                }
            }
            // search by name
            if (mainContactsList.get(i).getName().toLowerCase().contains(keyword.toLowerCase()) && mainContactsList.get(i).getName() != null) {
                tempContactsList.add(mainContactsList.get(i));
            }
            // search by public key
            if (publickey_size > 0)
                for (int j = 1; j <= publickey_size; j++) {
                    if (mainContactsList.get(i).getListPublicKey().get(publickey_size - j).getPublicKey().toLowerCase().contains(keyword.toLowerCase())) {
                        mlistcount.setpublickeynymber(publickey_size - j);
                        tempContactsList.add(mainContactsList.get(i));
                    }
                }
        }
        if (tempContactsList.size() == 0) {
            nocontactTxt.setVisibility(View.VISIBLE);
            searchListView.setVisibility(View.GONE);
        } else {
            nocontactTxt.setVisibility(View.GONE);
            searchListView.setVisibility(View.VISIBLE);
        }
        TreeSet<Contacts> hs = new TreeSet<>();
        hs.addAll(tempContactsList);
        tempContactsList.clear();
        tempContactsList.addAll(hs);
        adapter.updateDataSet(tempContactsList);

    }

    /*
    load Contacts
     */
    private void loadContacts() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new LoadAllDetail(contactSearchActivity, contactSearchActivity);
            }
        }, 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DELETE_CONTACT_CODE:
                if (data != null) {
                    String id = data.getStringExtra(getResources().getString(R.string.intent_contact_id));
                    for (int i = 0; i < mainContactsList.size(); i++) {
                        if (mainContactsList.get(i).getContactId().equals(id)) {
                            mainContactsList.remove(i);
                            tempContactsList.remove(i);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }else {
                    loadContacts();
                }
                break;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_search_back_txt:
                finish();
                break;
            case R.id.id_search_close_txt:
                searchEdt.setText("");
                if (mainContactsList.size() == 0) {
                    nocontactTxt.setVisibility(View.VISIBLE);
                    searchListView.setVisibility(View.GONE);
                } else {
                    adapter.addList(mainContactsList);
                    nocontactTxt.setVisibility(View.GONE);
                    searchListView.setVisibility(View.VISIBLE);
                }
                findViewById(R.id.id_search_close_txt).setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onReadComplete(List<Contacts> list) {
        if (mainContactsList != null) {
            mainContactsList.clear();
        }
        mainContactsList.addAll(list);
        adapter.updateDataSet(mainContactsList);
        adapter.notifyDataSetChanged();
    }

}