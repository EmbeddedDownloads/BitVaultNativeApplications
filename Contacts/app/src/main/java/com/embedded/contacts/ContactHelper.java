package com.embedded.contacts;

import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.utils.AndroidAppUtils;
import com.embedded.contacts.utils.ContactPreference;
import com.embedded.contacts.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created Dheeraj Bansal root on 15/6/17.
 * version 1.0.0
 * Retrieve, Delete, Update Contacts
 */

@SuppressWarnings({"WeakerAccess", "JavaDoc"})
public class ContactHelper {

    private static final String TAG = ContactHelper.class.getCanonicalName();

    /**
     * Delete contact from provider
     *
     * @param contactHelper
     * @param id
     */
    public static void deleteContact(ContentResolver contactHelper,
                                     String id) {

        ArrayList<ContentProviderOperation> mContentProviderOperation = new ArrayList<>();
        String[] args = new String[]{id};

        mContentProviderOperation.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
        } catch (RemoteException | OperationApplicationException e) {
            AndroidAppUtils.showLogE(TAG, e.toString());
        }
    }

    /**
     * Delete profile from provider
     *
     * @param context
     */
    public static void deleteProfile(Context context) {
        ArrayList<ContentProviderOperation> mContentProviderOperation = new ArrayList<>();

        mContentProviderOperation.add(ContentProviderOperation.newDelete(ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI).build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
            ContactPreference.saveProfile(context, -1);
        } catch (RemoteException | OperationApplicationException e) {
            AndroidAppUtils.showLogE(TAG, e.toString());
        }
    }

    /**
     * Add contact multiple fields
     *
     * @param context
     */
    public static String addContact(Context context, Contacts contacts) {

        int contactId = 0;

        ArrayList<ContentProviderOperation> mContentProviderOperation = new ArrayList<>();
        mContentProviderOperation.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        if (contacts.getName() != null) {
            mContentProviderOperation.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            contacts.getName().replaceAll("\\s+", " ").replace(",","!9@v")).build());
        }
        int contactlistnumSize = contacts.getListNumber().size();
        for (int i = 0; i < contactlistnumSize; i++) {
            String listnum = contacts.getListNumber().get(i).getNumber();
            if (listnum != null &&
                    listnum.length() > 0) {
                mContentProviderOperation.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.LABEL,
                                Utils.getNumberType(contacts.getListNumber().get(i).getNumberType()))
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contacts.getListNumber().get(i).getNumber())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                Utils.getNumberType(contacts.getListNumber().get(i).getNumberType()))
                        .build());
            }
        }
        int contactlist = contacts.getListPublicKey().size();
        for (int i = 0; i < contactlist; i++) {
            String pubkey = contacts.getListPublicKey().get(i).getPublicKey();
            if (contacts.getListPublicKey().get(i).getPublicKey() != null &&
                    pubkey.length() > 0) {
                mContentProviderOperation.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.SipAddress.LABEL,
                                ContactsContract.CommonDataKinds.SipAddress.TYPE_CUSTOM)
                        .withValue(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS,
                                contacts.getListPublicKey().get(i).getPublicKey())
                        .withValue(ContactsContract.CommonDataKinds.SipAddress.TYPE,
                                ContactsContract.CommonDataKinds.SipAddress.TYPE_CUSTOM)
                        .build());
            }
        }

        if (contacts.getPhotoUri() != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Bitmap mBitmap;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(contacts.getPhotoUri()));
                if (mBitmap != null) {
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
                    mBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                    mContentProviderOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, Utils.toByteArray(mBitmap))
                            .build());

                }
            } catch (IOException e) {
                AndroidAppUtils.showLogE(TAG, e.toString());
            }
        }
        String contact_ID = null;
        try {
            ContentProviderResult[] res = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
            if (res.length > 0) {
                Uri myContactUri = res[0].uri;
                int lastSlash = myContactUri.toString().lastIndexOf("/");
                int length = myContactUri.toString().length();
                contact_ID = (String) myContactUri.toString().subSequence(lastSlash + 1, length);
                ContactPreference.saveLong(context, ContactPreference.KEY, Long.parseLong(contact_ID));

                AndroidAppUtils.showLogD("MyContact", " contact ID after Addd " + contact_ID);
                return contact_ID;
            }
        } catch (Exception e) {
            AndroidAppUtils.showLogE(TAG, e.toString());
        }
        AndroidAppUtils.showLogD("MyContact", " contact ID after Addd " + contactId);
        return contact_ID;
    }

    /*
    Update contacts Except MI phones
     */
    public static String updateExceptMI(Context context, Contacts contacts) {

        String id = contacts.getContactId();
        AndroidAppUtils.showLogD(TAG, id);
        deleteContact(context.getContentResolver(), id);
        id = addContact(context, contacts);
        return id;
    }

    /**
     * update contact number and data
     *
     * @param context
     * @param contacts
     */
    public static void update(Context context, Contacts contacts) {

        ContentResolver contentResolver = context.getContentResolver();
        ContentProviderResult[] res;
        String id = contacts.getContactId();
        int listpubkeysize = contacts.getListPublicKey().size();

        ArrayList<ContentProviderOperation> mContentProviderOperation = new ArrayList<>();

        if (contacts.getName() != null) {
            mContentProviderOperation.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "= ? " + " AND " + ContactsContract.Data.MIMETYPE + "= ? ",
                            new String[]{String.valueOf(id), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contacts.getName().replace(",","!9@v").replaceAll("\\s+", " "))
                    .build());
            try {
                res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
                if (res.length > 0) {
                    AndroidAppUtils.showLogD(TAG, " Name update " + res[0].count);
                }
            } catch (RemoteException | OperationApplicationException e) {
                AndroidAppUtils.showLogD(TAG, e.toString());
            }
        }

        //key update

        mContentProviderOperation = new ArrayList<>();
        mContentProviderOperation.add(ContentProviderOperation.
                newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "= ? " + " AND " + ContactsContract.Data.MIMETYPE + "= ? ",
                        new String[]{id, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE})
                .build());
        try {
            res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
            if (res.length > 0) {
                AndroidAppUtils.showLogD(TAG, " Public key deleted " + res[0].count);
            }
        } catch (RemoteException | OperationApplicationException e) {
            AndroidAppUtils.showLogD(TAG, e.toString());
        }

        for (int i = 0; i < listpubkeysize; i++) {
            if (contacts.getListPublicKey().get(i).getPublicKey() != null &&
                    contacts.getListPublicKey().get(i).getPublicKey().length() > 0) {
                mContentProviderOperation.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, Long.parseLong(id))
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.SipAddress.LABEL,
                                ContactsContract.CommonDataKinds.SipAddress.TYPE_CUSTOM)
                        .withValue(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS,
                                contacts.getListPublicKey().get(i).getPublicKey())
                        .withValue(ContactsContract.CommonDataKinds.SipAddress.TYPE,
                                ContactsContract.CommonDataKinds.SipAddress.TYPE_CUSTOM)
                        .build());
            }
        }
        try {
            res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
            if (res.length > 0) {
                AndroidAppUtils.showLogD(TAG, " Public key deleted " + res[0].count);
            }
        } catch (RemoteException | OperationApplicationException e) {
            AndroidAppUtils.showLogD(TAG, e.toString());
        }


        //number update

        mContentProviderOperation = new ArrayList<>();
        mContentProviderOperation.add(ContentProviderOperation.
                newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "= ? " + " AND " + ContactsContract.Data.MIMETYPE + "= ? ",
                        new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                .build());
        try {
            res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
            if (res.length > 0) {
                AndroidAppUtils.showLogD(TAG, " Number delete current list is less than previous " + res[0].count);
            }
        } catch (RemoteException | OperationApplicationException e) {
            AndroidAppUtils.showLogD(TAG, e.toString());
        }


        mContentProviderOperation = new ArrayList<>();
        for (int i = 0; i < contacts.getListNumber().size(); i++) {
            if (contacts.getListNumber().get(i).getNumber() != null &&
                    contacts.getListNumber().get(i).getNumber().length() > 0) {
                mContentProviderOperation.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, Long.parseLong(id))
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.LABEL,
                                Utils.getNumberType(contacts.getListNumber().get(i).getNumberType()))
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contacts.getListNumber().get(i).getNumber())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                Utils.getNumberType(contacts.getListNumber().get(i).getNumberType()))
                        .build());
            }
        }

        try {
            res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
            if (res.length > 0) {
                AndroidAppUtils.showLogD(TAG, " Number delete current list is less than previous " + res[0].count);
            }
        } catch (RemoteException | OperationApplicationException e) {
            AndroidAppUtils.showLogD(TAG, e.toString());
        }
        //photo
        mContentProviderOperation = new ArrayList<>();
        Bitmap mBitmap;
        if (contacts.getPhotoUri() == null || contacts.getPhotoUri().length() <= 4) {
            mContentProviderOperation.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "= ? " + " AND " + ContactsContract.Data.MIMETYPE + "= ? ",
                            new String[]{id, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, null)
                    .build());
            try {
                res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
                if (res.length > 0) {
                    AndroidAppUtils.showLogD(TAG, " photo update " + res[0].count);
                }
            } catch (RemoteException | OperationApplicationException e) {
                AndroidAppUtils.showLogE(TAG, e.toString());
            }
        } else {
            mContentProviderOperation.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "= ? " + " AND " + ContactsContract.Data.MIMETYPE + "= ? ",
                            new String[]{id, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, null)
                    .build());
            try {
                res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
                if (res.length > 0) {
                    AndroidAppUtils.showLogD(TAG, " photo update " + res[0].count);
                }
            } catch (RemoteException | OperationApplicationException e) {
                AndroidAppUtils.showLogE(TAG, e.toString());
            }
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(contacts.getPhotoUri()));
                if (mBitmap != null) {
                    mContentProviderOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, Long.parseLong(id))
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, Utils.toByteArray(mBitmap))
                            .build());
                    AndroidAppUtils.showLogD(TAG, " photo update before query");
                    res = contentResolver.applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
                    if (res.length > 0) {
                        AndroidAppUtils.showLogD(TAG, " photo update " + res[0].count);
                    }
                }
            } catch (IOException | RemoteException | OperationApplicationException e) {
                AndroidAppUtils.showLogE(TAG, e.toString());
            }
        }
        ContactPreference.saveLong(context, ContactPreference.KEY, Long.parseLong(id));

    }

    /**
     * For update and Add user profile contact data
     *
     * @param context
     * @param contacts
     * @return
     */
    public static ContentProviderResult[] createOrUpdateUserProfile(Context context, Contacts contacts) {

        deleteProfile(context);

        ArrayList<ContentProviderOperation> mContentProviderOperation = new ArrayList<>();
        int rawContactInsertIndex = mContentProviderOperation.size();
        int listpublickeysize = contacts.getListPublicKey().size();

        try {
            mContentProviderOperation.add(ContentProviderOperation
                    .newInsert(ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, AccountManager.KEY_ACCOUNT_TYPE)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, AccountManager.KEY_ACCOUNT_NAME)
                    .build());

            if (contacts.getName() != null) {
                mContentProviderOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contacts.getName().replaceAll("\\s+", " "))
                        .build());
            }

            if (contacts.getFirstName() != null) {
                mContentProviderOperation.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DATA2,
                                contacts.getFirstName()).build());
            }

            if (contacts.getLastName() != null) {
                mContentProviderOperation.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DATA3,
                                contacts.getLastName()).build());
            }

            for (int i = 0; i < contacts.getListNumber().size(); i++) {
                if (contacts.getListNumber().get(i).getNumber() != null &&
                        contacts.getListNumber().get(i).getNumber().length() > 0) {
                    mContentProviderOperation.add(ContentProviderOperation.
                            newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    contacts.getListNumber().get(i).getNumber())
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                    Utils.getNumberType(contacts.getListNumber().get(i).getNumberType()))
                            .build());
                }
            }

            for (int i = 0; i < listpublickeysize; i++) {
                if (contacts.getListPublicKey().get(i).getPublicKey() != null &&
                        contacts.getListPublicKey().get(i).getPublicKey().length() > 0) {
                    mContentProviderOperation.add(ContentProviderOperation.
                            newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.SipAddress.LABEL,
                                    Utils.getNumberType("Custom"))
                            .withValue(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS, contacts.getListPublicKey().get(i).getPublicKey())
                            .withValue(ContactsContract.CommonDataKinds.SipAddress.TYPE,
                                    Utils.getNumberType("Custom"))
                            .build());
                }
            }

            if (contacts.getPhotoUri() != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                Bitmap mBitmap;
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(contacts.getPhotoUri()));
                    if (mBitmap != null) {
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
                        mBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                        mContentProviderOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                .withValue(ContactsContract.Data.MIMETYPE,
                                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, Utils.toByteArray(mBitmap))
                                .build());
                    }
                } catch (IOException e) {
                    AndroidAppUtils.showLogE(TAG, e.toString());
                }
            }
            ContactPreference.saveProfile(context, listpublickeysize);
            return context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, mContentProviderOperation);
        } catch (RemoteException | OperationApplicationException e) {
            AndroidAppUtils.showLogD(TAG, e.toString());
        }

        return null;
    }

}