package com.embedded.contacts.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.embedded.contacts.R;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.model.Mobile;
import com.embedded.contacts.model.PublicKey;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import utils.SDKUtils;

/**
 * Created Dheeraj Bansal root on 14/6/17.
 * version 1.0.0
 * Utils
 */

@SuppressWarnings("JavaDoc")
public class Utils {

    private final static String TAG = Utils.class.getCanonicalName();

    public static String MOBILE = "Mobile";
    public static String HOME = "Home";
    public static String WORK = "Work";
    public static String MAIN = "Main";
    public static String CUSTOM = "Custom";


    /**
     * Get color code from position for background
     *
     * @param context
     * @param pos
     * @return
     */
    public static int getColorCode(Context context, int pos) {
        if (pos == 0) {
            return ContextCompat.getColor(context, R.color.green);
        } else if (pos == 1) {
            return ContextCompat.getColor(context, R.color.magenta);
        } else if (pos == 2) {
            return ContextCompat.getColor(context, R.color.red);
        } else if (pos == 3) {
            return ContextCompat.getColor(context, R.color.blue);
        } else if (pos == 4) {
            return ContextCompat.getColor(context, R.color.CYAN);
        } else {
            return ContextCompat.getColor(context, R.color.purple);
        }
    }

    /**
     * Get list of all numbers of a particular contacts
     *
     * @param context
     * @param contactId
     * @return
     */
    public static ArrayList<Mobile> getMobileList(Context context, String contactId) {

        Cursor pCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId}, null);
        ArrayList<Mobile> mobileList = new ArrayList<>();

        assert pCursor != null;
        while (pCursor.moveToNext()) {
            int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            String phoneNo = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
            phoneNo = phoneNo.replaceAll(" ", "");
            phoneNo = phoneNo.replaceAll("-", "");
            switch (phoneType) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, Utils.MOBILE));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, Utils.HOME));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, Utils.WORK));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, Utils.MAIN));
                    }
                    break;
                default:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, Utils.CUSTOM));
                    }
                    break;
            }
        }
        pCursor.close();

        return mobileList;

    }

    /**
     * Set All user profile contact data to object
     *
     * @param contact
     * @param pCursor
     */
    public static void setContactData(Contacts contact, Cursor pCursor) {


        ArrayList<Mobile> mobileList = new ArrayList<>();
        ArrayList<PublicKey> keyList = new ArrayList<>();

        while (pCursor.moveToNext()) {
            String mimeType = pCursor.getString(pCursor.getColumnIndex("mimetype"));
            if (mimeType != null) {
                if (mimeType.contains("name")) {
                    String fName = pCursor.getString(pCursor.getColumnIndex(ContactsContract.
                            CommonDataKinds.StructuredName.DATA2));
                    String lName = pCursor.getString(pCursor.getColumnIndex(ContactsContract.
                            CommonDataKinds.StructuredName.DATA3));
                    if (fName != null && fName.length() > 0) {
                        contact.setFirstName(fName);
                    }
                    if (lName != null && lName.length() > 0) {
                        contact.setLastName(lName);
                    }
                } else if (mimeType.contains("phone")) {
                    int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    String phoneNo = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                    phoneNo = phoneNo.replaceAll(" ", "");
                    phoneNo = phoneNo.replaceAll("-", "");
                    switch (phoneType) {
                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                            if (!isNumberExist(mobileList, phoneNo)) {
                                mobileList.add(new Mobile(phoneNo, Utils.MOBILE));
                            }
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                            if (!isNumberExist(mobileList, phoneNo)) {
                                mobileList.add(new Mobile(phoneNo, Utils.HOME));
                            }
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                            if (!isNumberExist(mobileList, phoneNo)) {
                                mobileList.add(new Mobile(phoneNo, Utils.WORK));
                            }
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                            if (!isNumberExist(mobileList, phoneNo)) {
                                mobileList.add(new Mobile(phoneNo, Utils.MAIN));
                            }
                            break;
                        default:
                            if (!isNumberExist(mobileList, phoneNo)) {
                                mobileList.add(new Mobile(phoneNo, Utils.CUSTOM));
                            }
                            break;
                    }
                } else if (mimeType.contains("address")) {
                    int keyType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
                    String publicKey = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
                    switch (keyType) {
                        case ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME:
                        case ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK:
                        case ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER:
                            break;
                        default:
                            keyList.add(new PublicKey(publicKey));
                            break;
                    }
                }
            }
        }
        contact.setName(contact.getFirstName() + " " + contact.getLastName());
        contact.setListPublicKey(keyList);
        contact.setListNumber(mobileList);

    }


    /**
     * Check profile contains keys
     *
     * @param pCursor
     */
    public static boolean isProfileContainsKey(Cursor pCursor) {


        ArrayList<PublicKey> keyList = new ArrayList<>();

        while (pCursor.moveToNext()) {
            String mimeType = pCursor.getString(pCursor.getColumnIndex("mimetype"));
            if (mimeType != null) {
                if (mimeType.contains("address")) {
                    int keyType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
                    String publicKey = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
                    switch (keyType) {
                        case ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME:
                        case ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK:
                        case ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER:
                            break;
                        default:
                            keyList.add(new PublicKey(publicKey));
                            break;
                    }
                }
            }
        }
        return keyList.size() > 0;

    }

    /**
     * Get list of key form content provider based on contactid
     *
     * @param context
     * @param contactId
     * @return
     */
    public static ArrayList<PublicKey> getKeyList(Context context, String contactId, String name) {
        ArrayList<PublicKey> keyList = new ArrayList<>();
        Cursor postal = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.SipAddress.CONTACT_ID + " = "
                        + contactId, null, null);

        if (postal != null) {
            List<Mobile> list = getMobileList(context, contactId);
            while (postal.moveToNext()) {
                int keyType = postal.getInt(postal.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
                String publicKey = postal.getString(postal.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
                switch (keyType) {
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME:
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK:
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER:
                        break;
                    default:
                        if (publicKey != null && publicKey.length() > 0) {
                            publicKey = publicKey.trim();
                            if (!publicKey.equals("")) {
                                if (name == null) {
                                    addKey(keyList, list, publicKey);
                                } else if (!name.contains(publicKey) && !publicKey.contains(name)) {
                                    addKey(keyList, list, publicKey);
                                }
                            }
                        }
                        break;
                }
            }
            postal.close();
        }

        return keyList;
    }

    /*
    Add Public Key
     */
    private static void addKey(ArrayList<PublicKey> keyList, List<Mobile> list, String publicKey) {
       // Adding Public key
        boolean isExist = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNumber().equalsIgnoreCase(publicKey)) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            keyList.add(new PublicKey(publicKey));
        }
    }


    /*
    To get position from number type for selection in spinner
     */
    public static int getPosition(String numberType) {
        if (numberType.equalsIgnoreCase(MOBILE)) {
            return 0;
        } else if (numberType.equalsIgnoreCase(HOME)) {
            return 1;
        } else if (numberType.equalsIgnoreCase(WORK)) {
            return 2;
        } else if (numberType.equalsIgnoreCase(MAIN)) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * Convert bitmap to byte array for save image in contacts
     *
     * @param bitmap
     * @return
     */
    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    /**
     * TO convert position to number type for spinner
     *
     * @param position
     * @return
     */
    public static String getNumberTypeFromPos(int position) {
        if (position == 0) {
            return MOBILE;
        } else if (position == 1) {
            return HOME;
        } else if (position == 2) {
            return WORK;
        } else if (position == 3) {
            return MAIN;
        } else {
            return CUSTOM;
        }
    }

    /**
     * To prevent duplicate form list
     *
     * @param list
     * @param phone
     * @return
     */
    private static boolean isNumberExist(List<Mobile> list, String phone) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNumber().equalsIgnoreCase(phone) || list.get(i).getNumber().endsWith(phone)) {
                return true;
            }
        }
        return false;
    }

    public static Contacts getContactsById(List<Contacts> list, String id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getContactId().equals(id)) {
                return list.get(i);
            }
        }
        return null;
    }

    /**
     * Show toast
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static int getNumberType(String type) {
        if (type == null)
            return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        switch (type) {
            case "Mobile":
                return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            case "Home":
                return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
            case "Work":
                return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
            case "Main":
                return ContactsContract.CommonDataKinds.Phone.TYPE_MAIN;
            default:
                return ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void keyboardDown(Activity mActivity) {
        AndroidAppUtils.showLogD(TAG, "keyboardDown");
        try {
            InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            AndroidAppUtils.showLogE(TAG, e.toString());
        }
    }

    public void hideDefaultKeyboard(Context context) {
        AndroidAppUtils.showLogD(TAG, "Calling hideDefaultKeyboard");
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            Utils.keyboardDown((Activity) context);
            SDKUtils.showErrorLog(TAG, "Software Keyboard was shown");
            ((Activity) context).finish();
        } else {
            ((Activity) context).finish();
            SDKUtils.showErrorLog(TAG, "Software Keyboard was not shown");
        }
    }
}



