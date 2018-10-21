package com.embedded.contacts.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.embedded.contacts.model.SecureContact;
import com.embedded.contacts.utils.AndroidAppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * To save all contacts in Database
 */

@SuppressWarnings({"WeakerAccess", "JavaDoc"})
@SuppressLint("StaticFieldLeak")
public class DBHelper extends SQLiteOpenHelper {

    private final static String TAG = DBHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contacts";

    // table name
    private static final String TABLE_CONTACT = "contact";

    //Contact Table Columns names
    private static final String CONTACT_ID = "id";
    private static final String CONTACT_SECURE = "secure";


    // Contact Table Query
    private static final String CREATE_TABLE_CONTACT = "CREATE TABLE "
            + TABLE_CONTACT + "(" + CONTACT_ID + " TEXT PRIMARY KEY ,"
            + CONTACT_SECURE + " INTEGER " + ")";


    private static Context mContext;


    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        mContext = context;
        return Helper.mDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACT);
        AndroidAppUtils.showLogD(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        onCreate(db);
    }

    /**
     * Add all contacts to Database
     */
    public void addAllContact(List<SecureContact> list) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < list.size(); i++) {
            values.put(CONTACT_ID, list.get(i).getContactId());
            values.put(CONTACT_SECURE, list.get(i).getSecure());
            // Inserting Row
            db.insert(TABLE_CONTACT, null, values);
        }

        db.close();
    }

    /**
     * Add contacts to Database
     */
    public void addContact(String id, int secure) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CONTACT_ID, id);
        values.put(CONTACT_SECURE, secure==0?0:1);
        // Inserting Row
        db.insert(TABLE_CONTACT, null, values);

        db.close();
    }

    /**
     * Delete contact based on contact id
     *
     * @param contactId
     */
    public void deleteContact(String contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from  " + TABLE_CONTACT +
                " where " + CONTACT_ID + " = '" + contactId + "'");
        db.close();
    }

    public void updateContact(String contactId,int secure) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_SECURE, secure==0?0:1);
        db.update(TABLE_CONTACT, contentValues, CONTACT_ID + " = ? ", new String[]{contactId});
        db.close();
    }

    public boolean isProfileExist(String id){
        String selectQuery = "SELECT * FROM " + TABLE_CONTACT+ " where " + CONTACT_ID + " = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        boolean isExist;
        if(cursor.moveToFirst()) {
            isExist=true;
        }
        else{
            isExist=false;
        }
        cursor.close();
        db.close();
        return isExist;
    }

    /**
     * Chack table is empty or not
     *
     * @return
     */
    public boolean isTableEmpty() {
        SQLiteDatabase db = getWritableDatabase();
        String count = "SELECT count(*) FROM " + TABLE_CONTACT;
        Cursor mcursor = db.rawQuery(count, null);
        int icount = 0;
        if(mcursor.moveToFirst()) {
            icount = mcursor.getInt(0);
        }
        mcursor.close();
        return icount <= 0;
    }

    /**
     * Retrive all contacts from Database
     *
     * @return
     */
    public List<SecureContact> getAllContacts() {
        List<SecureContact> list = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACT;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        SecureContact contacts;
        while (cursor.moveToNext()) {
            contacts = new SecureContact();
            contacts.setContactId(cursor.getString(0));
            contacts.setSecure(cursor.getInt(1));
            list.add(contacts);
        }
        cursor.close();
        db.close();
        return list;
    }


    private static class Helper {
        private static DBHelper mDBHelper = new DBHelper(mContext);
    }


}