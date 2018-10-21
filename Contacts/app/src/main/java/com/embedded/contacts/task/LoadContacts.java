package com.embedded.contacts.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.view.Window;

import com.embedded.contacts.R;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.utils.AndroidAppUtils;
import com.embedded.contacts.utils.ContactPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version
 * Read All contacts using content provider
 */

public class LoadContacts extends AsyncTask<String, Integer, List<Contacts>> {

    private Context context;
    private boolean isFirstTimeLoad;
    private OnContactsReadListener listener;

    private ProgressDialog progressDialog;

    public LoadContacts(Context context, OnContactsReadListener listener, boolean isFirstTimeLoad) {
        this.context = context;
        this.listener = listener;
        this.isFirstTimeLoad = isFirstTimeLoad;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (isFirstTimeLoad) {
            progressDialog = new ProgressDialog(context);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage(context.getResources().getString(R.string.fetching_contacts));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected List<Contacts> doInBackground(String... strings) {
        return readPhoneContacts();
    }

    @Override
    protected void onPostExecute(List<Contacts> lists) {
        super.onPostExecute(lists);
        if (isFirstTimeLoad) {
            progressDialog.dismiss();
        }
        listener.onReadComplete(lists);
    }

    /**
     * Read all contacts form phone
     *
     * @return
     */
    private List<Contacts> readPhoneContacts() {
        List<Contacts> list = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        long maxId= ContactPreference.getLong(context,ContactPreference.KEY);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                Contacts contacts = new Contacts();
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if(maxId==0){
                    maxId=Long.parseLong(id);
                }
                else{
                    if(maxId<Long.parseLong(id)){
                        maxId=Long.parseLong(id);
                    }
                }
                String time = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP));

                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                String image_uri = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                contacts.setContactId(id);
                contacts.setLastUpdateTime(Long.parseLong(time));
                contacts.setName(contactName);
                contacts.setPhotoUri(image_uri);

                list.add(contacts);
            }
            ContactPreference.saveLong(context,ContactPreference.KEY,maxId);
            AndroidAppUtils.showLogD(" LoadContact "," maxid "+maxId);
            cursor.close();
        }
        return list;
    }


}
