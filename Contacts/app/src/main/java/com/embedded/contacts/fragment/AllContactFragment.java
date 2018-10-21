package com.embedded.contacts.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.embedded.contacts.AddContactActivity;
import com.embedded.contacts.ContactViewActivity;
import com.embedded.contacts.MainActivity;
import com.embedded.contacts.R;
import com.embedded.contacts.adapter.AllContactAdapter;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.ui.CircularImageView;
import com.embedded.contacts.utils.AndroidAppUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.embedded.contacts.MainActivity.ADD_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.DELETE_CONTACT_CODE;

@SuppressWarnings("JavaDoc")
public class AllContactFragment extends Fragment {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private ListView listViewContact;
    private AllContactAdapter adapter;

    private List<Contacts> contactsList;

    private View profileView;
    private TextView profileNameTxt;
    private TextView setUpTxt;
    private TextView profileImageTxt;
    private ImageView profileSecureImg;
    private CircularImageView profileImageView;
    private MainActivity mainActivity;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.allcontacts_layout, container, false);
        initViews(view);
        listViewItemClick();
        return view;
    }

    /**
     * Initializing Views
     *
     * @param view
     */
    private void initViews(View view) {
        listViewContact = (ListView) view.findViewById(R.id.id_listView_contact);
        listViewContact.setFastScrollEnabled(true);
        contactsList = new ArrayList<>();

        LayoutInflater layoutInflater = (LayoutInflater)
                mainActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
        profileView = layoutInflater.inflate(R.layout.setup_layout, listViewContact, false);
        profileNameTxt = (TextView) profileView.findViewById(R.id.profileNameTxt);
        setUpTxt = (TextView) profileView.findViewById(R.id.setUpTxt);
        profileImageTxt = (TextView) profileView.findViewById(R.id.profileImgTxt);
        profileImageView = (CircularImageView) profileView.findViewById(R.id.profileImg);
        profileSecureImg = (ImageView) profileView.findViewById(R.id.secureImg);
        adapter = new AllContactAdapter(this, contactsList);
        listViewContact.setAdapter(adapter);
        listViewContact.addHeaderView(profileView);
    }

    /***
     * Clicking on listview item , open contact details
     */
    private void listViewItemClick() {
        listViewContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (listViewContact.getHeaderViewsCount() > 0 && i == 0) {
                    if (mainActivity.getProfileID() == null) {
                        Intent intent = new Intent(mainActivity, AddContactActivity.class);
                        intent.putExtra(getResources().getString(R.string.intent_edit), getResources().getString(R.string.intent_edit));
                        intent.putExtra(getResources().getString(R.string.intent_title), getResources().getString(R.string.my_profile));
                        startActivityForResult(intent, ADD_CONTACT_CODE);
                    } else {
                        Intent intent = new Intent(mainActivity, ContactViewActivity.class);
                        intent.putExtra(getResources().getString(R.string.intent_title), getResources().getString(R.string.my_profile));
                        startActivityForResult(intent, DELETE_CONTACT_CODE);
                    }
                } else {
                    if (listViewContact.getHeaderViewsCount() > 0)
                        i = i - 1;
                    Contacts contacts = contactsList.get(i);
                    Intent intent = new Intent(mainActivity, ContactViewActivity.class);
                    intent.putExtra(getResources().getString(R.string.intent_contact_id), contacts.getContactId());
                    AndroidAppUtils.showLogD(TAG, contacts.getContactId());
                    if (!(contacts.getPhotoUri() != null && contacts.getPhotoUri().length() > 0)) {
                        intent.putExtra(getResources().getString(R.string.intent_color), contacts.getColorPos());
                    }
                    startActivityForResult(intent, DELETE_CONTACT_CODE);
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }


    public void updateAdapter() {
        // Update adpater with contact list
        contactsList.clear();
        if (mainActivity.isSecureSelected()) {
            for (int i = 0; i < MainActivity.mainContactList.size(); i++) {
                if (MainActivity.mainContactList.get(i).isSecure()) {
                    contactsList.add(MainActivity.mainContactList.get(i));
                }
            }
            if(!mainActivity.isProfileSecure()){
                profileView.setVisibility(View.GONE);
            }
        } else {
            contactsList.addAll(MainActivity.mainContactList);
            profileView.setVisibility(View.VISIBLE);
        }
        adapter.updateDataSet(contactsList);

        setFastScrollVisible();
        loadProfile();
    }

    /*
    Load profile
     */
    public void loadProfile() {
        if (mainActivity.getProfileID() == null) {
            setUpTxt.setVisibility(View.VISIBLE);
            profileNameTxt.setVisibility(View.GONE);
            profileImageTxt.setVisibility(View.GONE);
            profileImageView.setVisibility(View.GONE);
        } else {
            setUpTxt.setVisibility(View.GONE);
            profileNameTxt.setVisibility(View.VISIBLE);
            profileNameTxt.setText(mainActivity.getProfileName());
            if (mainActivity.getProfilePhotoUri() != null) {
                profileImageView.setImageURI(Uri.parse(mainActivity.getProfilePhotoUri()));
                profileImageView.setVisibility(View.VISIBLE);
                profileImageTxt.setVisibility(View.GONE);
            } else {
                profileImageTxt.setVisibility(View.VISIBLE);
                profileImageView.setVisibility(View.GONE);
                if (mainActivity.getProfileName() != null) {
                    profileImageTxt.setText(mainActivity.getProfileName().substring(0, 1));
                }
            }
        }
        profileSecureImg.setVisibility(mainActivity.isProfileSecure() ? View.VISIBLE : View.GONE);
    }

    /**
     * Update color code
     *
     * @param position
     * @param colorPos
     */
    public void updateList(int position, int colorPos) {
        if (contactsList.size() > position) {
            contactsList.get(position).setColorPos(colorPos);
        }
    }

    private void setFastScrollVisible() {
        if (contactsList.size() < 10) {
            listViewContact.setFastScrollAlwaysVisible(false);
        } else {
            listViewContact.setFastScrollAlwaysVisible(true);
        }
    }


}
