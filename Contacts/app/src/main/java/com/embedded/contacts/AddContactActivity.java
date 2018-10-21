package com.embedded.contacts;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.embedded.contacts.adapter.PhoneAdapter;
import com.embedded.contacts.adapter.PublicKeyAdapter;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.model.Mobile;
import com.embedded.contacts.model.PublicKey;
import com.embedded.contacts.task.OnProfileLoadListener;
import com.embedded.contacts.task.ProfileLoader;
import com.embedded.contacts.utils.AndroidAppUtils;
import com.embedded.contacts.utils.BitVaultFont;
import com.embedded.contacts.utils.ContactPreference;
import com.embedded.contacts.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import qrcode.ScanQRCode;

import static com.embedded.contacts.MainActivity.ADD_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.ADD_PROFILE_CODE;
import static com.embedded.contacts.MainActivity.UPDATE_CONTACT_CODE;
import static com.embedded.contacts.MainActivity.mainContactList;

/**
 * Created Dheeraj Bansal root on 12/6/17.
 * version 1.0.0
 * Create new contact
 */

@SuppressWarnings({"JavaDoc", "UnnecessaryReturnStatement"})
public class AddContactActivity extends AppCompatActivity implements View.OnClickListener, OnProfileLoadListener {

    public static final int CROPING_CODE = 401;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String TAG = AddContactActivity.class.getCanonicalName();
    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int ACTION_REQUEST_GALLERY = 201;
    private static final int ACTION_CAPTURE_IMAGE = 301;
    private static final int REQUEST_SCAN_PRIVATE_KEY = 501;
    private BitVaultFont imageTxt;
    private EditText firstNameEdt;
    private EditText lastNameEdt;
    private TextView titleTxt;
    private ImageView conactBgImage;
    private BitVaultFont backTxt;
    private BitVaultFont imagetakeTxt;
    private TextView saveTxt;
    private boolean isClicked =true;

    private ArrayList<PublicKey> publicKeyList;
    private ArrayList<Mobile> mobileList;
    private Uri pickedPhotoForContactUri;

    private PhoneAdapter phoneAdapter;
    private PublicKeyAdapter keyAdapter;

    private File outPutFile = null;

    private String contactId;
    private int keyPosition = -1;

    private String title;
    private Contacts contacts;
    private boolean isUpdatedMob = true, isUpdatedKey = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.addcontact);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        outPutFile = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
        if (outPutFile.exists()) {
            AndroidAppUtils.showLogD(TAG, " file deleted " + outPutFile.delete());
        }
        initViews();
        textwatcherFirstName();
        getDataFromIntent();
        setListners();

    }

    /**
     * Initialize views
     */
    private void initViews() {
        backTxt = (BitVaultFont) findViewById(R.id.back_icon);
        titleTxt = (TextView) findViewById(R.id.header_center_tv);
        conactBgImage = (ImageView) findViewById(R.id.id_add_contact_img);
        imageTxt = (BitVaultFont) findViewById(R.id.id_add_contact_txt);
        imagetakeTxt = (BitVaultFont) findViewById(R.id.id_add_contact_take_image_txt);
        firstNameEdt = (EditText) findViewById(R.id.contactFirstName);
        lastNameEdt = (EditText) findViewById(R.id.contactLastName);
        firstNameEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        lastNameEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        saveTxt = (TextView) findViewById(R.id.id_save_txt);
        RecyclerView phoneRecyclerView = (RecyclerView) findViewById(R.id.id_phone_container);
        phoneRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        RecyclerView keyRecyclerView = (RecyclerView) findViewById(R.id.id_publickey_container);
        keyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        mobileList = new ArrayList<>();
        publicKeyList = new ArrayList<>();

        phoneAdapter = new PhoneAdapter(this, mobileList);
        phoneRecyclerView.setAdapter(phoneAdapter);

        keyAdapter = new PublicKeyAdapter(this, publicKeyList);
        keyRecyclerView.setAdapter(keyAdapter);
    }

    /**
     * Setting onClickListners for views
     */
    private void setListners() {
        backTxt.setOnClickListener(this);
        imagetakeTxt.setOnClickListener(this);
        conactBgImage.setOnClickListener(this);
        saveTxt.setOnClickListener(this);
    }

    /**
     * This method check whether user first name contaion space or not. If space is
     * there in first name then show error, "Space is not allowed".
     */
    private void textwatcherFirstName() {
        firstNameEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                String str = s.toString();
                if (str.length() > 1 && str.contains(" ")) {
                    firstNameEdt.setError("Space is not allowed");
                    if(str.matches("[a-zA-Z]+")){
                    firstNameEdt.setText(str.split(" ")[0]);
                    }else{
                        firstNameEdt.setText("");
                    }
                }
                firstNameEdt.setSelection(firstNameEdt.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
                firstNameEdt.setSelection(firstNameEdt.getText().length());
            }


        });
    }

    /**
     * Display data from intent pass by another activity
     */
    private void getDataFromIntent() {
        contacts = new Contacts();
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.hasExtra(getString(R.string.saveContactByMessaging))) {
            String key = intent.getStringExtra(getString(R.string.saveContactByMessaging));
            if (key != null && key != "") {
                publicKeyList.add(new PublicKey(key));
                keyAdapter.notifyDataSetChanged();
            }
        } else if (intent.getExtras() != null && intent.hasExtra(getResources().getString(R.string.intent_title)) &&
                intent.getStringExtra(getResources().getString(R.string.intent_title))
                        .equalsIgnoreCase(getResources().getString(R.string.my_profile))) {
            title = intent.getStringExtra(getResources().getString(R.string.intent_title));
            titleTxt.setText(title);
            new ProfileLoader(AddContactActivity.this, AddContactActivity.this);
            loadContact(intent, ContactsContract.Profile.CONTENT_URI, true);
            addNewRow();
        } else if (intent.hasExtra(getResources().getString(R.string.intent_public_key))) {
            String key = intent.getStringExtra(getResources().getString(R.string.intent_public_key));
            if (key != null) {
                publicKeyList.add(new PublicKey(key));
                keyAdapter.notifyDataSetChanged();
            }
        } else {
            if (intent.hasExtra(getResources().getString(R.string.intent_title))) {
                title = intent.getStringExtra(getResources().getString(R.string.intent_title));
                titleTxt.setText(title);
            }
            if (intent.hasExtra(getResources().getString(R.string.intent_contact_id))) {

                contactId = intent.getStringExtra(getResources().getString(R.string.intent_contact_id));
                contacts.setContactId(contactId);
                AndroidAppUtils.showLogD(TAG, contactId);
                contacts.setListNumber(Utils.getMobileList(this, contactId));
                loadContact(intent, ContactsContract.Contacts.CONTENT_URI, false);
                contacts.setListPublicKey(Utils.getKeyList(this, contactId, contacts.getName()));
                setMobileAndKey();
            }
            if (intent.hasExtra(getResources().getString(R.string.intent_edit))) {
                titleTxt.setText(getResources().getString(R.string.edit_contact));
            }
        }
        addNewRow();
    }

    /*
    Load contacts list
     */
    private void loadContact(Intent intent, Uri contentUri, boolean isProfile) {
        // Load contact list
        Cursor pCursor;
        if (isProfile) {
            pCursor = getContentResolver().query(contentUri, null, null, null, null);
        } else {
            pCursor = getContentResolver().query(contentUri, null,
                    ContactsContract.Contacts._ID + " = ?",
                    new String[]{contacts.getContactId()}, null);
        }

        assert pCursor != null;
        pCursor.moveToFirst();
        AndroidAppUtils.showLogD(TAG, " count : " + pCursor.getCount() + "  " + contacts.getContactId());
        if (pCursor.getCount() > 0) {

            String name = pCursor.getString(pCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String uri = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            contacts.setName(name);
            if (uri != null && uri.length() > 4) {
                conactBgImage.setVisibility(View.VISIBLE);
                contacts.setPhotoUri(uri);
                imageTxt.setVisibility(View.GONE);
                conactBgImage.setImageURI(Uri.parse(uri));
            } else if (intent.hasExtra(getResources().getString(R.string.intent_color))) {
                contacts.setColorPos(intent.getIntExtra(getResources().getString(R.string.intent_color), 0));
                conactBgImage.setBackgroundColor(Utils.getColorCode(this, contacts.getColorPos()));
            }
            pCursor.close();
        }

        Cursor nameCursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = "
                        + contacts.getContactId(), null, null);
        if (nameCursor != null) {
            if (nameCursor.moveToNext()) {
                if (nameCursor.getString(nameCursor.getColumnIndex("mimetype")).contains("name")) {
                    String fName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.
                            CommonDataKinds.StructuredName.DATA2));
                    String lName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.
                            CommonDataKinds.StructuredName.DATA3));
                    String name = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.
                            CommonDataKinds.StructuredName.DATA5));
                    String fullName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.
                            CommonDataKinds.StructuredName.DATA1));

                    if (fullName != null) {
                        contacts.setName(fullName);
                    }
                    String lName1 = "";
                    if (lName != null && lName.length() > 0 && name != null) {
                        lName1 = name + " " + lName;
                        contacts.setLastName(lName1);
                    } else {
                        lName1 = lName;

                        contacts.setLastName(lName1);
                    }

                    contacts.setFirstName(fName);
                }
            }
            nameCursor.close();
            if (contacts.getFirstName() != null) {
                String name = contacts.getFirstName();
                firstNameEdt.append(name.replace("!9@v", ","));
            }
            if (contacts.getLastName() != null) {
                lastNameEdt.append(contacts.getLastName().replace("!9@v", ","));
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_icon:
                new Utils().hideDefaultKeyboard(this);
                break;
            case R.id.id_add_contact_take_image_txt:
                openPermissionDialog();
                break;
            case R.id.id_save_txt:
                onClickSaveButton();
                break;
            case R.id.id_publickey_barcodeTxt:
                startActivityForResult(new Intent(AddContactActivity.this, ScanQRCode.class), REQUEST_SCAN_PRIVATE_KEY);
                break;
        }
    }

    /**
     * Event occur on clicking save button
     */
    private void onClickSaveButton() {
        if(isClicked){
            isClicked = false;
        if (firstNameEdt.getText().toString().equals(" ") ){
                Utils.showToast(this, getResources().getString(R.string.empty_phone));
                isClicked=true;
                return;
            }
        int publickeysize = publicKeyList.size();
        for (int j = 0; j <= publickeysize - 1; j++) {
            if (publicKeyList.get(j).getPublicKey().length() < 33 && publicKeyList.get(j).getPublicKey().length() > 0) {
                Utils.showToast(this, getString(R.string.public_key_not_valid));
                isClicked=true;
                return;
            }
        }
        contacts.setName(firstNameEdt.getText().toString() + " " + lastNameEdt.getText().toString().trim());
        contacts.setFirstName(firstNameEdt.getText().toString());
        contacts.setLastName(lastNameEdt.getText().toString());
        if (contacts.getName().length() > 2) {
            contacts.setName(contacts.getName().trim());
        }

        if (pickedPhotoForContactUri != null) {
            contacts.setPhotoUri(String.valueOf(pickedPhotoForContactUri));
        }
        int mobilelistsize = mobileList.size();
        if (isUpdatedMob && mobilelistsize > 0) {
            isUpdatedMob = false;
            mobileList.remove(mobilelistsize - 1);
        }
        if (isUpdatedKey && publickeysize > 0) {
            isUpdatedKey = false;
            publicKeyList.remove(publickeysize - 1);
        }

        contacts.setListNumber(mobileList);
        contacts.setListPublicKey(publicKeyList);

        int mpublickeysize = publicKeyList.size();
        for (int i = 0; i < mpublickeysize; i++) {
            if (publicKeyList.get(i).getPublicKey() == null ||
                    publicKeyList.get(i).getPublicKey().length() == 0)
                publicKeyList.remove(i);
        }
        int mobilekeysize = mobileList.size();
        for (int i = 0; i < mobilekeysize; i++) {
            if (mobileList.get(i).getNumber() == null ||
                    mobileList.get(i).getNumber().length() == 0)
                mobileList.remove(i);
        }


        Intent intent = new Intent();
        intent.putExtra(getResources().getString(R.string.intent_contact_id), String.valueOf(contactId));

        if (!(contacts.getPhotoUri() != null && contacts.getPhotoUri().length() > 4)) {
            intent.putExtra(getResources().getString(R.string.intent_color), contacts.getColorPos());
        }

        if (title != null && title.equals(getResources().getString(R.string.my_profile))) {
            ContactHelper.createOrUpdateUserProfile(this, contacts);
            intent.putExtra(getResources().getString(R.string.intent_code), ADD_PROFILE_CODE);
            intent.putExtra(getResources().getString(R.string.intent_title), getResources().getString(R.string.my_profile));
            setResult(ADD_CONTACT_CODE, intent);
        } else if (getIntent().hasExtra(getResources().getString(R.string.intent_edit))) {
            //   contacts.setContactId(contactId);
            if (android.os.Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
                ContactHelper.update(this, contacts);
            } else {
                ContactHelper.updateExceptMI(this, contacts);
            }
            contactId = String.valueOf(ContactPreference.getLong(this, ContactPreference.KEY));
            intent.putExtra(getResources().getString(R.string.intent_contact_id), contactId);
            intent.putExtra(getResources().getString(R.string.intent_code), UPDATE_CONTACT_CODE);
            setResult(ADD_CONTACT_CODE, intent);
        } else {
            int mainListSize = mainContactList.size();
            for (int k = 0; k < mainListSize; k++) {
                String name = contacts.getName().trim();
                if (name != null && name.equals(mainContactList.get(k).getName())) {
                    Utils.showToast(this, getString(R.string.Contacts_already_exists) + name);
                    isClicked=true;
                    return;
                }
            }
            contactId = ContactHelper.addContact(this, contacts);
            contactId = String.valueOf(ContactPreference.getLong(this, ContactPreference.KEY));
            intent.putExtra(getResources().getString(R.string.intent_contact_id), contactId);
            intent.putExtra(getResources().getString(R.string.intent_code), ADD_CONTACT_CODE);
            setResult(ADD_CONTACT_CODE, intent);
        }
        new Utils().hideDefaultKeyboard(this);
    }
    }

    /**
     * Ask for run time permissions
     */
    private void openPermissionDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            pickImage();
        }
    }

    /**
     * Request for run time permission
     */
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
            AndroidAppUtils.showLogD(TAG, "permission already granted");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                    , PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    AndroidAppUtils.showLogD(TAG, " permission granted");
                    pickImage();
                } else {
                    AndroidAppUtils.showLogD(TAG, " permission not granted");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SCAN_PRIVATE_KEY:
                    if (data != null) {
                        String scannedResult = data.getStringExtra("data");
                        scannedResult = scannedResult.replace("bitcoin:", "");
                        if (keyPosition != -1 && publicKeyList.size() > keyPosition && scannedResult.length() > 33) {
                            try {
                                publicKeyList.get(keyPosition).setPublicKey(scannedResult);
                                keyAdapter.notifyItemChanged(keyPosition);
                                if (PublicKey.emptyCounter(publicKeyList) < 1) {
                                    publicKeyList.add(new PublicKey());
                                    keyAdapter.notifyItemInserted(publicKeyList.size() - 1);
                                }
                                keyPosition = -1;
                            } catch (Exception ignored) {
                            }
                        } else {
                            Utils.showToast(this, "not valid public key");
                        }
                    }
                    break;
                case CROPING_CODE:
                    if (outPutFile.exists()) {
                        Bitmap photo = decodeFile(outPutFile);
                        conactBgImage.setImageBitmap(photo);
                        imageTxt.setVisibility(View.GONE);
                        pickedPhotoForContactUri = Uri.fromFile(outPutFile);
                    }

                    break;
                case ACTION_REQUEST_GALLERY:
                    if (data != null && data.getData() != null) {
                        pickedPhotoForContactUri = data.getData();
                        File file = new File((pickedPhotoForContactUri.getPath()));
                        Log.e("absolute path..", file.getAbsolutePath().toString());
                        Bitmap photo = decodeFile(file);
                        conactBgImage.setImageBitmap(photo);
                        imageTxt.setVisibility(View.GONE);
                        //cropingIMG();
                    }
                    break;
                case ACTION_CAPTURE_IMAGE:
                    if (outPutFile.exists()) {
                        Bitmap photo = decodeFile(outPutFile);
                        conactBgImage.setImageBitmap(photo);
                        imageTxt.setVisibility(View.GONE);
                        pickedPhotoForContactUri = Uri.fromFile(outPutFile);
                    }
                    cropingIMG();
                    break;
            }
        } else {
            if (requestCode == CROPING_CODE) {
                pickedPhotoForContactUri = null;
                //noinspection ResultOfMethodCallIgnored
                outPutFile.delete();
            }
        }

    }

    /**
     * Pass image uri to crop app
     */
    private void cropingIMG() {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            AndroidAppUtils.showLogW(TAG, " App not found");
            return;
        } else {
            intent.setData(pickedPhotoForContactUri);
            intent.putExtra("outputX", 400);
            intent.putExtra("outputY", 400);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);

            //Create output file here
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROPING_CODE);
            } else {
                if (size == 2) {
                    intent.setComponent(new ComponentName(list.get(1).activityInfo.packageName, list.get(1).activityInfo.name));
                } else {
                    intent.setComponent(new ComponentName(list.get(0).activityInfo.packageName, list.get(0).activityInfo.name));
                }
                startActivityForResult(intent, CROPING_CODE);
            }
        }
    }

    /**
     * Get bitmap form Intent
     */
    private void pickImage() {
        final CharSequence[] items;
        if ((outPutFile != null && outPutFile.exists()) && pickedPhotoForContactUri != null &&
                imageTxt.getVisibility() == View.GONE) {
            items = new String[]{getResources().getString(R.string.take_photo),
                    getResources().getString(R.string.choose_photo), getResources().getString(R.string.remove_photo)};
        } else {
            items = new String[]{getResources().getString(R.string.take_photo),
                    getResources().getString(R.string.choose_photo)};
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.change_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (position == 0) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        try {
                            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                            m.invoke(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pickedPhotoForContactUri = Uri.fromFile(getOutputMediaFileUri(MEDIA_TYPE_IMAGE));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, pickedPhotoForContactUri);
                    startActivityForResult(intent, ACTION_CAPTURE_IMAGE);
                } else if (position == 1) {
                   /* Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, ACTION_REQUEST_GALLERY);*/
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    i.setClassName("com.bitvault.mediavault",
                            "com.bitvault.mediavault.multifileselector.MultiFileSelectActivity");
                    startActivityForResult(i, ACTION_REQUEST_GALLERY);
                } else {
                    conactBgImage.setImageDrawable(null);
                    pickedPhotoForContactUri = null;
                    contacts.setPhotoUri(null);
                    imageTxt.setVisibility(View.VISIBLE);
                    //noinspection ResultOfMethodCallIgnored
                    outPutFile.delete();
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                builder.create().dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*
    Create Media file Name
     */
    private File getOutputMediaFileUri(int type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = null;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }
        return mediaFile;

    }

    /**
     * convert file to bitmap
     *
     * @param f
     * @return
     */
    private Bitmap decodeFile(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 512;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            AndroidAppUtils.showLogE(TAG, e.toString());
        }
        return null;
    }

    @Override
    public void onProfileLoadComplete(Cursor cursor) {
        Utils.setContactData(contacts, cursor);
        cursor.close();
        setMobileAndKey();
        firstNameEdt.setText(contacts.getFirstName());
        lastNameEdt.setText(contacts.getLastName());
    }

    /**
     * Add mobile and key edit control
     */
    private void setMobileAndKey() {
        if (contacts.getListNumber() != null && contacts.getListNumber().size() > 0) {
            mobileList.clear();
            mobileList.addAll(contacts.getListNumber());
            phoneAdapter.notifyDataSetChanged();
        }
        if (contacts.getListPublicKey() != null && contacts.getListPublicKey().size() > 0) {
            publicKeyList.clear();
            publicKeyList.addAll(contacts.getListPublicKey());
        }
        addNewRow();
    }

    /**
     * Add empty row
     */
    private void addNewRow() {
        int mobilelistSize = mobileList.size();
        if (Mobile.emptyCounter(mobileList) > 1) {
            mobileList.remove(mobilelistSize - 1);
        } else if (Mobile.emptyCounter(mobileList) < 1) {
            mobileList.add(new Mobile());
        }
        if (PublicKey.emptyCounter(publicKeyList) > 1) {
            publicKeyList.remove(publicKeyList.size() - 1);
        } else if (PublicKey.emptyCounter(publicKeyList) < 1) {
            publicKeyList.add(new PublicKey());
        }
        keyAdapter.notifyDataSetChanged();
        phoneAdapter.notifyDataSetChanged();
    }

    /**
     * Call QR Code Scan Activity
     *
     * @param layoutPosition
     */
    public void scanBarCode(int layoutPosition) {
        keyPosition = layoutPosition;
        startActivityForResult(new Intent(AddContactActivity.this, ScanQRCode.class), REQUEST_SCAN_PRIVATE_KEY);
    }
}