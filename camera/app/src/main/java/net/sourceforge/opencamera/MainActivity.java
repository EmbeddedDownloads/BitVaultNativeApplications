package net.sourceforge.opencamera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import net.sourceforge.opencamera.CameraController.CameraController;
import net.sourceforge.opencamera.CameraController.CameraControllerManager2;
import net.sourceforge.opencamera.Preview.Preview;
import net.sourceforge.opencamera.Render.MergeVideosTask;
import net.sourceforge.opencamera.UI.FolderChooserDialog;
import net.sourceforge.opencamera.UI.PopupView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    public static final int ACTIVITY_CHOOSE_SOUND = 9999;
    public final static String APP_ID = "44d01b9b72aad53837ba5675184b6a01";
    private static final String TAG = "MainActivity";
    public static View viewS;
    // for testing:
    public boolean is_test = false;
    public Bitmap gallery_bitmap = null;
    public boolean test_low_memory = false;
    public boolean test_have_angle = false;
    public float test_angle = 0.0f;
    public String test_last_saved_image = null;
    public int overlayIdx = 0;
    int[] overLayGroup = {0, 5, 10, 13, 16};
    String[] overlays = {
            "0/Close_001.svg", "0/Close_002.svg", "0/Close_003.svg", "0/Close_004.svg", "0/Close_005.svg",
            "1/Detail_001.svg", "1/Detail_002.svg", "1/Detail_003.svg", "1/Detail_004.svg", "1/Detail_005.svg",
            "2/Long_001.svg", "2/Long_002.svg", "2/Long_003.svg",
            "3/Medium_001.svg", "3/Medium_002.svg", "3/Medium_003.svg",
            "4/Wide_001.svg", "4/Wide_002.svg", "4/Wide_003.svg"


    };
    private SensorManager mSensorManager = null;
    private Sensor mSensorAccelerometer = null;
    private Sensor mSensorMagnetic = null;
    private MyApplicationInterface applicationInterface = null;
    private Preview preview = null;
    private int current_orientation = 0;
    private OrientationEventListener orientationEventListener = null;
    private boolean supports_auto_stabilise = false;
    private boolean supports_force_video_4k = false;
    private boolean supports_camera2 = false;
    private ArrayList<String> save_location_history = new ArrayList<String>();
    private boolean camera_in_background = false; // whether the camera is covered by a fragment/dialog (such as settings or folder picker)
    private GestureDetector gestureDetector;
    private boolean screen_is_locked = false;
    private Map<Integer, Bitmap> preloaded_bitmap_resources = new Hashtable<Integer, Bitmap>();
    private PopupView popup_view = null;
    private SoundPool sound_pool = null;
    private SparseIntArray sound_ids = null;
    private TextToSpeech textToSpeech = null;
    private boolean textToSpeechSuccess = false;
    private boolean ui_placement_right = true;
    private ToastBoxer switch_camera_toast = new ToastBoxer();
    private ToastBoxer switch_video_toast = new ToastBoxer();
    private ToastBoxer screen_locked_toast = new ToastBoxer();
    private ToastBoxer changed_auto_stabilise_toast = new ToastBoxer();
    private ToastBoxer exposure_lock_toast = new ToastBoxer();
    private boolean block_startup_toast = false;
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            preview.onAccelerometerSensorChanged(event);
        }
    };
    private SensorEventListener magneticListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            preview.onMagneticSensorChanged(event);
        }
    };
    private Handler immersive_timer_handler = null;
    private Runnable immersive_timer_runnable = null;
    private String mLastAudioPath;

    private static double seekbarScaling(double frac) {
        // For various seekbars, we want to use a non-linear scaling, so user has more control over smaller values
        double scaling = (Math.pow(100.0, frac) - 1.0) / 99.0;
        return scaling;
    }

    private static double seekbarScalingInverse(double scaling) {
        double frac = Math.log(99.0 * scaling + 1.0) / Math.log(100.0);
        return frac;
    }

    static private void putBundleExtra(Bundle bundle, String key, List<String> values) {
        if (values != null) {
            String[] values_arr = new String[values.size()];
            int i = 0;
            for (String value : values) {
                values_arr[i] = value;
                i++;
            }
            bundle.putStringArray(key, values_arr);
        }
    }

    public static String getDonateLink() {
        return "https://play.google.com/store/apps/details?id=harman.mark.donation";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MyDebug.LOG) {
            Log.d(TAG, "onCreate");
        }
        long time_s = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        viewS = findViewById(R.id.settings);
        if (getIntent() != null && getIntent().getExtras() != null) {
            is_test = getIntent().getExtras().getBoolean("test_project");
            if (MyDebug.LOG)
                Log.d(TAG, "is_test: " + is_test);
        }
        if (getIntent() != null && getIntent().getExtras() != null) {
            boolean take_photo = getIntent().getExtras().getBoolean(TakePhoto.TAKE_PHOTO);
            if (MyDebug.LOG)
                Log.d(TAG, "take_photo?: " + take_photo);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (MyDebug.LOG) {
            Log.d(TAG, "standard max memory = " + activityManager.getMemoryClass() + "MB");
            Log.d(TAG, "large max memory = " + activityManager.getLargeMemoryClass() + "MB");
        }
        //if( activityManager.getMemoryClass() >= 128 ) { // test
        if (activityManager.getLargeMemoryClass() >= 128) {
            supports_auto_stabilise = true;
        }
        if (MyDebug.LOG)
            Log.d(TAG, "supports_auto_stabilise? " + supports_auto_stabilise);

        // hack to rule out phones unlikely to have 4K video, so no point even offering the option!
        // both S5 and Note 3 have 128MB standard and 512MB large heap (tested via Samsung RTL), as does Galaxy K Zoom
        // also added the check for having 128MB standard heap, to support modded LG G2, which has 128MB standard, 256MB large - see https://sourceforge.net/p/opencamera/tickets/9/
        if (activityManager.getMemoryClass() >= 128 || activityManager.getLargeMemoryClass() >= 512) {
            supports_force_video_4k = true;
        }
        if (MyDebug.LOG)
            Log.d(TAG, "supports_force_video_4k? " + supports_force_video_4k);

        applicationInterface = new MyApplicationInterface(this, savedInstanceState);

        initCamera2Support();

        setWindowFlagsForCamera();

        // read save locations
        save_location_history.clear();
        int save_location_history_size = sharedPreferences.getInt("save_location_history_size", 0);
        if (MyDebug.LOG)
            Log.d(TAG, "save_location_history_size: " + save_location_history_size);
        for (int i = 0; i < save_location_history_size; i++) {
            String string = sharedPreferences.getString("save_location_history_" + i, null);
            if (string != null) {
                if (MyDebug.LOG)
                    Log.d(TAG, "save_location_history " + i + ": " + string);
                save_location_history.add(string);
            }
        }
        // also update, just in case a new folder has been set; this is also necessary to update the gallery icon
        updateFolderHistory();
        //updateFolderHistory("/sdcard/Pictures/OpenCameraTest");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "found accelerometer");
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "no support for accelerometer");
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "found magnetic sensor");
            mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "no support for magnetic sensor");
        }

        clearSeekBar();

        preview = new Preview(applicationInterface, savedInstanceState, ((ViewGroup) this.findViewById(R.id.preview)));

        View switchCameraButton = (View) findViewById(R.id.switch_camera);
        switchCameraButton.setVisibility(preview.getCameraControllerManager().getNumberOfCameras() > 1 ? View.VISIBLE : View.GONE);
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                MainActivity.this.onOrientationChanged(orientation);
            }
        };

        View galleryButton = (View) findViewById(R.id.gallery);
        galleryButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //preview.showToast(null, "Long click");
                longClickedGallery();
                return true;
            }
        });

        gestureDetector = new GestureDetector(this, new MyGestureDetector());

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if (!usingKitKatImmersiveMode())
                            return;
                        if (MyDebug.LOG)
                            Log.d(TAG, "onSystemUiVisibilityChange: " + visibility);
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            if (MyDebug.LOG)
                                Log.d(TAG, "system bars now visible");
                            // The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            applicationInterface.setImmersiveMode(false);
                            setImmersiveTimer();
                        } else {
                            if (MyDebug.LOG)
                                Log.d(TAG, "system bars now NOT visible");
                            // The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                            applicationInterface.setImmersiveMode(true);
                        }
                    }
                });

        boolean has_done_first_time = sharedPreferences.contains(PreferenceKeys.getFirstTimePreferenceKey());
        if (!has_done_first_time && !is_test) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(R.string.intro_text);
            alertDialog.setPositiveButton(R.string.intro_ok, null);
            alertDialog.show();

            setFirstTimeFlag();
        }

        preloadIcons(R.array.flash_icons);
        preloadIcons(R.array.focus_mode_icons);

        textToSpeechSuccess = false;
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (MyDebug.LOG)
                    Log.d(TAG, "TextToSpeech initialised");
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechSuccess = true;
                    if (MyDebug.LOG)
                        Log.d(TAG, "TextToSpeech succeeded");
                } else {
                    if (MyDebug.LOG)
                        Log.d(TAG, "TextToSpeech failed");
                }
            }
        });

        if (MyDebug.LOG)
            Log.d(TAG, "time for Activity startup: " + (System.currentTimeMillis() - time_s));

        //  checkForUpdates();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2Support() {
        if (MyDebug.LOG)
            Log.d(TAG, "initCamera2Support");
        supports_camera2 = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraControllerManager2 manager2 = new CameraControllerManager2(this);
            supports_camera2 = true;
            if (manager2.getNumberOfCameras() == 0) {
                if (MyDebug.LOG)
                    Log.d(TAG, "Camera2 reports 0 cameras");
                supports_camera2 = false;
            }
            for (int i = 0; i < manager2.getNumberOfCameras() && supports_camera2; i++) {
                if (!manager2.allowCamera2Support(i)) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "camera " + i + " doesn't have limited or full support for Camera2 API");
                    supports_camera2 = false;
                }
            }
        }
        if (MyDebug.LOG)
            Log.d(TAG, "supports_camera2? " + supports_camera2);
    }

    private void preloadIcons(int icons_id) {
        long time_s = System.currentTimeMillis();
        String[] icons = getResources().getStringArray(icons_id);
        for (int i = 0; i < icons.length; i++) {
            int resource = getResources().getIdentifier(icons[i], null, this.getApplicationContext().getPackageName());
            if (MyDebug.LOG)
                Log.d(TAG, "load resource: " + resource);
            Bitmap bm = BitmapFactory.decodeResource(getResources(), resource);
            this.preloaded_bitmap_resources.put(resource, bm);
        }
        if (MyDebug.LOG) {
            Log.d(TAG, "time for preloadIcons: " + (System.currentTimeMillis() - time_s));
            Log.d(TAG, "size of preloaded_bitmap_resources: " + preloaded_bitmap_resources.size());
        }
    }

    @Override
    protected void onDestroy() {
        if (MyDebug.LOG) {
            Log.d(TAG, "onDestroy");
            Log.d(TAG, "size of preloaded_bitmap_resources: " + preloaded_bitmap_resources.size());
        }
        // Need to recycle to avoid out of memory when running tests - probably good practice to do anyway
        for (Map.Entry<Integer, Bitmap> entry : preloaded_bitmap_resources.entrySet()) {
            if (MyDebug.LOG)
                Log.d(TAG, "recycle: " + entry.getKey());
            entry.getValue().recycle();
        }
        preloaded_bitmap_resources.clear();
        if (textToSpeech != null) {
            // http://stackoverflow.com/questions/4242401/tts-error-leaked-serviceconnection-android-speech-tts-texttospeech-solved
            Log.d(TAG, "free textToSpeech");
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        //    unregisterManagers();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            /*case R.id.action_settings:
                //openSettings();;
				break;
			/*case R.id.action_render:
				makeMovie();
				break;
            case R.id.action_render_reset:
                applicationInterface.clearSessionCaptureHistory();
                mLastAudioPath = null;
                break;
            case R.id.action_select_audio:
                selectAudio();
                break;
            case R.id.action_record_audio:
                recordAudio();
                break;*/
            default:
                break;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void setFirstTimeFlag() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferenceKeys.getFirstTimePreferenceKey(), true);
        editor.apply();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (MyDebug.LOG)
            Log.d(TAG, "onKeyDown: " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String volume_keys = sharedPreferences.getString(PreferenceKeys.getVolumeKeysPreferenceKey(), "volume_take_photo");
                if (volume_keys.equals("volume_take_photo")) {
                    takePicture();
                    return true;
                } else if (volume_keys.equals("volume_focus")) {
                    if (preview.getCurrentFocusValue() != null && preview.getCurrentFocusValue().equals("focus_mode_manual2")) {
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                            this.changeFocusDistance(-1);
                        else
                            this.changeFocusDistance(1);
                    } else {
                        preview.requestAutoFocus();
                    }
                    return true;
                } else if (volume_keys.equals("volume_zoom")) {
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                        this.zoomIn();
                    else
                        this.zoomOut();
                    return true;
                } else if (volume_keys.equals("volume_exposure")) {
                    if (preview.getCameraController() != null) {
                        String value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), preview.getCameraController().getDefaultISO());
                        boolean manual_iso = !value.equals(preview.getCameraController().getDefaultISO());
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            if (manual_iso) {
                                if (preview.supportsISORange())
                                    this.changeISO(1);
                            } else
                                this.changeExposure(1);
                        } else {
                            if (manual_iso) {
                                if (preview.supportsISORange())
                                    this.changeISO(-1);
                            } else
                                this.changeExposure(-1);
                        }
                    }
                    return true;
                } else if (volume_keys.equals("volume_auto_stabilise")) {
                    if (this.supports_auto_stabilise) {
                        boolean auto_stabilise = sharedPreferences.getBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), false);
                        auto_stabilise = !auto_stabilise;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), auto_stabilise);
                        editor.apply();
                        String message = getResources().getString(R.string.preference_auto_stabilise) + ": " + getResources().getString(auto_stabilise ? R.string.on : R.string.off);
                        preview.showToast(changed_auto_stabilise_toast, message);
                    } else {
                        preview.showToast(changed_auto_stabilise_toast, R.string.auto_stabilise_not_supported);
                    }
                    return true;
                } else if (volume_keys.equals("volume_really_nothing")) {
                    // do nothing, but still return true so we don't change volume either
                    return true;
                }
                // else do nothing here, but still allow changing of volume (i.e., the default behaviour)
                break;
            }/*
        case KeyEvent.KEYCODE_MENU:
			{
	        	// needed to support hardware menu button
	        	// tested successfully on Samsung S3 (via RTL)
	        	// see http://stackoverflow.com/questions/8264611/how-to-detect-when-user-presses-menu-key-on-their-android-device
				openSettings();
	            return true;
			}*/
            case KeyEvent.KEYCODE_CAMERA: {
                if (event.getRepeatCount() == 0) {
                    takePicture();
                    return true;
                }
            }
            case KeyEvent.KEYCODE_FOCUS: {
                preview.requestAutoFocus();
                return true;
            }
            case KeyEvent.KEYCODE_ZOOM_IN: {
                this.zoomIn();
                return true;
            }
            case KeyEvent.KEYCODE_ZOOM_OUT: {
                this.zoomOut();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    void setSeekbarZoom() {
        if (MyDebug.LOG)
            Log.d(TAG, "setSeekbarZoom");
        SeekBar zoomSeekBar = (SeekBar) findViewById(R.id.zoom_seekbar);
        zoomSeekBar.setProgress(preview.getMaxZoom() - preview.getCameraController().getZoom());
        if (MyDebug.LOG)
            Log.d(TAG, "progress is now: " + zoomSeekBar.getProgress());
    }

    public void zoomIn() {
        changeSeekbar((SeekBar) findViewById(R.id.zoom_seekbar), -1);
    }

    public void zoomOut() {
        changeSeekbar((SeekBar) findViewById(R.id.zoom_seekbar), 1);
    }

    public void changeExposure(int change) {
        changeSeekbar((SeekBar) findViewById(R.id.exposure_seekbar), change);
    }

    public void changeISO(int change) {
        changeSeekbar((SeekBar) findViewById(R.id.iso_seekbar), change);
    }

    void changeFocusDistance(int change) {
        changeSeekbar((SeekBar) findViewById(R.id.focus_seekbar), change);
    }

    private void changeSeekbar(SeekBar seekBar, int change) {
        if (MyDebug.LOG)
            Log.d(TAG, "changeSeekbar: " + change);
        int value = seekBar.getProgress();
        int new_value = value + change;
        if (new_value < 0)
            new_value = 0;
        else if (new_value > seekBar.getMax())
            new_value = seekBar.getMax();
        if (MyDebug.LOG) {
            Log.d(TAG, "value: " + value);
            Log.d(TAG, "new_value: " + new_value);
            Log.d(TAG, "max: " + seekBar.getMax());
        }
        if (new_value != value) {
            seekBar.setProgress(new_value);
        }
    }

    @Override
    protected void onResume() {
        if (MyDebug.LOG)
            Log.d(TAG, "onResume");
        super.onResume();

        // Set black window background; also needed if we hide the virtual buttons in immersive mode
        // Note that we do it here rather than customising the theme's android:windowBackground, so this doesn't affect other views - in particular, the MyPreferenceFragment settings
        getWindow().getDecorView().getRootView().setBackgroundColor(Color.BLACK);

        mSensorManager.registerListener(accelerometerListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(magneticListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        orientationEventListener.enable();

        applicationInterface.getLocationSupplier().setupLocationListener();

        initSound();
        loadSound(R.raw.beep);
        loadSound(R.raw.beep_hi);

        layoutUI(); // set layout

        updateGalleryIcon(); // update in case images deleted whilst idle

        preview.onResume();


        //preview.switchVideo(true, true);
        // checkForCrashes();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (MyDebug.LOG)
            Log.d(TAG, "onWindowFocusChanged: " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        if (!this.camera_in_background && hasFocus) {
            // low profile mode is cleared when app goes into background
            // and for Kit Kat immersive mode, we want to set up the timer
            // we do in onWindowFocusChanged rather than onResume(), to also catch when window lost focus due to notification bar being dragged down (which prevents resetting of immersive mode)
            initImmersiveMode();
        }
    }

    @Override
    protected void onPause() {
        if (MyDebug.LOG)
            Log.d(TAG, "onPause");
        super.onPause();
        closePopup();
        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(magneticListener);
        orientationEventListener.disable();
        applicationInterface.getLocationSupplier().freeLocationListeners();
        releaseSound();
        preview.onPause();
        //  unregisterManagers();
    }

    void layoutUI() {
        if (MyDebug.LOG)
            Log.d(TAG, "layoutUI");
        //this.preview.updateUIPlacement();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ui_placement = sharedPreferences.getString(PreferenceKeys.getUIPlacementPreferenceKey(), "ui_right");
        // we cache the preference_ui_placement to save having to check it in the draw() method
        this.ui_placement_right = ui_placement.equals("ui_right");
        if (MyDebug.LOG)
            Log.d(TAG, "ui_placement: " + ui_placement);
        // new code for orientation fixed to landscape
        // the display orientation should be locked to landscape, but how many degrees is that?
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        // getRotation is anti-clockwise, but current_orientation is clockwise, so we add rather than subtract
        // relative_orientation is clockwise from landscape-left
        //int relative_orientation = (current_orientation + 360 - degrees) % 360;
        int relative_orientation = (current_orientation + degrees) % 360;
        if (MyDebug.LOG) {
            Log.d(TAG, "    current_orientation = " + current_orientation);
            Log.d(TAG, "    degrees = " + degrees);
            Log.d(TAG, "    relative_orientation = " + relative_orientation);
        }
        int ui_rotation = (360 - relative_orientation) % 360;
        preview.setUIRotation(ui_rotation);
        int align_left = RelativeLayout.ALIGN_LEFT;
        int align_right = RelativeLayout.ALIGN_RIGHT;
        int align_top = RelativeLayout.ALIGN_TOP;
        int align_bottom = RelativeLayout.ALIGN_BOTTOM;
        int left_of = RelativeLayout.LEFT_OF;
        int right_of = RelativeLayout.RIGHT_OF;
        int above = RelativeLayout.ABOVE;
        int below = RelativeLayout.BELOW;
        int align_parent_left = RelativeLayout.ALIGN_PARENT_LEFT;
        int align_parent_right = RelativeLayout.ALIGN_PARENT_RIGHT;
        int align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;
        int align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;
        if (!ui_placement_right) {
            align_top = RelativeLayout.ALIGN_BOTTOM;
            align_bottom = RelativeLayout.ALIGN_TOP;
            above = RelativeLayout.BELOW;
            below = RelativeLayout.ABOVE;
            align_parent_top = RelativeLayout.ALIGN_PARENT_BOTTOM;
            align_parent_bottom = RelativeLayout.ALIGN_PARENT_TOP;
        }
        {
            // we use a dummy button, so that the GUI buttons keep their positioning even if the Settings button is hidden (visibility set to View.GONE)
            View view = findViewById(R.id.gui_anchor);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_left, 0);
            layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, 0);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);
            view.setRotation(ui_rotation);

            view = findViewById(R.id.settings);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.gui_anchor);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);
            view.setRotation(ui_rotation);

            view = findViewById(R.id.gallery);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            /*layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(left_of, R.id.settings);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			*/
            view.setRotation(ui_rotation);

            view = findViewById(R.id.popup);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.settings);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);
            view.setRotation(ui_rotation);

            view = findViewById(R.id.popup1);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(above, R.id.popup);
            layoutParams.addRule(left_of, R.id.settings);
            view.setLayoutParams(layoutParams);
            view.setRotation(ui_rotation);


//			view = findViewById(R.id.exposure_lock);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.popup);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			view.setRotation(ui_rotation);
//			view.setVisibility(View.GONE);
            if (preview.isVideo()) {
                view = findViewById(R.id.exposure);
                layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
                layoutParams.addRule(align_parent_bottom, 0);
                layoutParams.addRule(left_of, R.id.popup1);
                layoutParams.addRule(right_of, 0);
                view.setLayoutParams(layoutParams);
                view.setRotation(ui_rotation);
            } else {
                view = findViewById(R.id.exposure);
                layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
                layoutParams.addRule(align_parent_bottom, 0);
                layoutParams.addRule(left_of, R.id.popup);
                layoutParams.addRule(right_of, 0);
                view.setLayoutParams(layoutParams);
                view.setRotation(ui_rotation);

            }

            view = findViewById(R.id.switch_video);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.exposure);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);
            view.setRotation(ui_rotation);


            view = findViewById(R.id.switch_camera);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            /*layoutParams.addRule(align_parent_left, 0);
            layoutParams.addRule(align_parent_right, 0);
			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(left_of, R.id.switch_video);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);*/
            view.setRotation(ui_rotation);

            view = findViewById(R.id.trash);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.switch_video);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);
            view.setRotation(ui_rotation);

//			view = findViewById(R.id.share);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.trash);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			view.setRotation(ui_rotation);

            view = findViewById(R.id.take_photo);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            /*layoutParams.addRule(align_parent_left, 0);
            layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
			view.setLayoutParams(layoutParams);*/
            view.setRotation(ui_rotation);

            view = findViewById(R.id.zoom);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_left, 0);
            layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_top, 0);
            layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
            view.setLayoutParams(layoutParams);
            view.setRotation(180.0f); // should always match the zoom_seekbar, so that zoom in and out are in the same directions

            view = findViewById(R.id.zoom_seekbar);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_left, 0);
            layoutParams.addRule(align_right, R.id.zoom);
            layoutParams.addRule(above, R.id.zoom);
            layoutParams.addRule(below, 0);
            view.setLayoutParams(layoutParams);

            view = findViewById(R.id.focus_seekbar);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_left, R.id.preview);
            layoutParams.addRule(align_right, 0);
            layoutParams.addRule(left_of, R.id.zoom_seekbar);
            layoutParams.addRule(right_of, 0);
            layoutParams.addRule(align_top, 0);
            layoutParams.addRule(align_bottom, R.id.zoom_seekbar);
            view.setLayoutParams(layoutParams);
        }

        {
            // set seekbar info
            int width_dp = 0;
            if (ui_rotation == 0 || ui_rotation == 180) {
                width_dp = 300;
            } else {
                width_dp = 200;
            }
            int height_dp = 50;
            final float scale = getResources().getDisplayMetrics().density;
            int width_pixels = (int) (width_dp * scale + 0.5f); // convert dps to pixels
            int height_pixels = (int) (height_dp * scale + 0.5f); // convert dps to pixels

            View view = findViewById(R.id.exposure_seekbar);
            view.setRotation(ui_rotation);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);

//			view = findViewById(R.id.exposure_seekbar_zoom);
//			view.setRotation(ui_rotation);
//			view.setAlpha(0.5f);

            // n.b., using left_of etc doesn't work properly when using rotation (as the amount of space reserved is based on the UI elements before being rotated)
            if (ui_rotation == 0) {
                view.setTranslationX(0);
                view.setTranslationY(height_pixels);
            } else if (ui_rotation == 90) {
                view.setTranslationX(-height_pixels);
                view.setTranslationY(0);
            } else if (ui_rotation == 180) {
                view.setTranslationX(0);
                view.setTranslationY(-height_pixels);
            } else if (ui_rotation == 270) {
                view.setTranslationX(height_pixels);
                view.setTranslationY(0);
            }

            view = findViewById(R.id.iso_seekbar);
            view.setRotation(ui_rotation);
            lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);

            view = findViewById(R.id.exposure_time_seekbar);
            view.setRotation(ui_rotation);
            lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);
            if (ui_rotation == 0) {
                view.setTranslationX(0);
                view.setTranslationY(height_pixels);
            } else if (ui_rotation == 90) {
                view.setTranslationX(-height_pixels);
                view.setTranslationY(0);
            } else if (ui_rotation == 180) {
                view.setTranslationX(0);
                view.setTranslationY(-height_pixels);
            } else if (ui_rotation == 270) {
                view.setTranslationX(height_pixels);
                view.setTranslationY(0);
            }

        }
        View view = findViewById(R.id.popup_container);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 1000;
        view.setLayoutParams(layoutParams);
        view.setRotation(ui_rotation);
        setTakePhotoIcon();

        setupOverlay();
    }

    private void nextOverlaySet() {
        overlayIdx++;

        if (overlayIdx == overLayGroup.length)
            overlayIdx = 0;

        ViewPager viewPager = (ViewPager) findViewById(R.id.overlayPager);
        viewPager.setCurrentItem(overLayGroup[overlayIdx], false);


    }

    private void setupOverlay() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.overlayPager);
        ImageAdapter adapter = new ImageAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(overLayGroup[overlayIdx], false);

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//viewS.setVisibility(View.GONE);
                return preview.touchEvent(motionEvent);


            }
        });

    }

    public void clickedPopupVedio(View view) {
        ImageButton popup = (ImageButton) findViewById(R.id.popup1);
        String flash_value = preview.getCurrentFlashValue();
//		if( MyDebug.LOG )
//			Log.d(TAG, "flash_value: " + flash_value);
        if (flash_value != null && !flash_value.equals("flash_torch")) {
            popup.setImageResource(R.drawable.popup_flash_torch);
            preview.updateFlash("flash_torch");
        } else {
            popup.setImageResource(R.drawable.popup);
            preview.updateFlash("flash_off");
        }

    }

    private void setTakePhotoIcon() {
        // set icon for taking photos vs videos
        ImageButton view = (ImageButton) findViewById(R.id.take_photo);
        if (preview != null) {
            int resource = 0;
            if (preview.isVideo()) {
                resource = preview.isVideoRecording() ? R.drawable.take_video_recording : R.drawable.take_video_selector;
                findViewById(R.id.popup).setVisibility(View.GONE);
                findViewById(R.id.popup1).setVisibility(View.VISIBLE);

            } else {
                findViewById(R.id.popup1).setVisibility(View.GONE);
                resource = R.drawable.take_photo_selector;
            }
            view.setImageResource(resource);
            view.setTag(resource); // for testing
        }
    }

    boolean getUIPlacementRight() {
        return this.ui_placement_right;
    }

    private void onOrientationChanged(int orientation) {
        /*if( MyDebug.LOG ) {
			Log.d(TAG, "onOrientationChanged()");
			Log.d(TAG, "orientation: " + orientation);
			Log.d(TAG, "current_orientation: " + current_orientation);
		}*/
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
            return;
        int diff = Math.abs(orientation - current_orientation);
        if (diff > 180)
            diff = 360 - diff;
        // only change orientation when sufficiently changed
        if (diff > 60) {
            orientation = (orientation + 45) / 90 * 90;
            orientation = orientation % 360;
            if (orientation != current_orientation) {
                this.current_orientation = orientation;
                if (MyDebug.LOG) {
                    Log.d(TAG, "current_orientation is now: " + current_orientation);
                }
                layoutUI();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (MyDebug.LOG)
            Log.d(TAG, "onConfigurationChanged()");
        // configuration change can include screen orientation (landscape/portrait) when not locked (when settings is open)
        // needed if app is paused/resumed when settings is open and device is in portrait mode
        preview.setCameraDisplayOrientation();
        super.onConfigurationChanged(newConfig);
    }

    public void clickedTakePhoto(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedTakePhoto");
        this.takePicture();

    }

    public void clickedSwitchCamera(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedSwitchCamera");
        this.closePopup();
        if (this.preview.canSwitchCamera()) {
            int cameraId = preview.getCameraId();
            int n_cameras = preview.getCameraControllerManager().getNumberOfCameras();
            cameraId = (cameraId + 1) % n_cameras;
            if (preview.getCameraControllerManager().isFrontFacing(cameraId)) {
                preview.showToast(switch_camera_toast, R.string.front_camera);
            } else {
                preview.showToast(switch_camera_toast, R.string.back_camera);
            }
            View switchCameraButton = (View) findViewById(R.id.switch_camera);
            switchCameraButton.setEnabled(false); // prevent slowdown if user repeatedly clicks
            this.preview.setCamera(cameraId);
            switchCameraButton.setEnabled(true);
        }
    }

    public void clickedSwitchVideo(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedSwitchVideo");
        this.closePopup();
        View switchVideoButton = (View) findViewById(R.id.switch_video);
        switchVideoButton.setEnabled(false); // prevent slowdown if user repeatedly clicks
        this.preview.switchVideo(true, true);
        switchVideoButton.setEnabled(true);
        layoutUI();
        setTakePhotoIcon();
        if (!block_startup_toast) {
            this.showPhotoVideoToast();
        }
    }

    public void setPopupIcon() {
        if (MyDebug.LOG)
            Log.d(TAG, "setPopupIcon");
        ImageButton popup = (ImageButton) findViewById(R.id.popup);
        String flash_value = preview.getCurrentFlashValue();
        if (MyDebug.LOG)
            Log.d(TAG, "flash_value: " + flash_value);
        if (flash_value != null && flash_value.equals("flash_torch")) {
            popup.setImageResource(R.drawable.popup_flash_torch);
        } else if (flash_value != null && flash_value.equals("flash_auto")) {
            popup.setImageResource(R.drawable.popup_flash_auto);
        } else if (flash_value != null && flash_value.equals("flash_on")) {
            popup.setImageResource(R.drawable.popup_flash_on);
        } else if (flash_value != null && flash_value.equals("flash_red_eye")) {
            popup.setImageResource(R.drawable.popup_flash_red_eye);
        } else {
            popup.setImageResource(R.drawable.popup);
        }
    }

    void clearSeekBar() {
        View view = findViewById(R.id.exposure_seekbar);
        view.setVisibility(View.GONE);
        view = findViewById(R.id.iso_seekbar);
        view.setVisibility(View.GONE);
        view = findViewById(R.id.exposure_time_seekbar);
        view.setVisibility(View.GONE);
        //view = findViewById(R.id.exposure_seekbar_zoom);
        //view.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void clickedExposure(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedExposure");
        this.closePopup();
        SeekBar exposure_seek_bar = ((SeekBar) findViewById(R.id.exposure_seekbar));
        int exposure_visibility = exposure_seek_bar.getVisibility();
        SeekBar iso_seek_bar = ((SeekBar) findViewById(R.id.iso_seekbar));
        int iso_visibility = iso_seek_bar.getVisibility();
        SeekBar exposure_time_seek_bar = ((SeekBar) findViewById(R.id.exposure_time_seekbar));
        int exposure_time_visibility = iso_seek_bar.getVisibility();
        boolean is_open = exposure_visibility == View.VISIBLE || iso_visibility == View.VISIBLE || exposure_time_visibility == View.VISIBLE;
        if (is_open) {
            clearSeekBar();
        } else if (preview.getCameraController() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), preview.getCameraController().getDefaultISO());
            if (preview.usingCamera2API() && !value.equals(preview.getCameraController().getDefaultISO())) {
                // with Camera2 API, when using non-default ISO we instead show sliders for ISO range and exposure time
                if (preview.supportsISORange()) {
                    iso_seek_bar.setVisibility(View.VISIBLE);
                    if (preview.supportsExposureTime()) {
                        exposure_time_seek_bar.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (preview.supportsExposures()) {
                    exposure_seek_bar.setVisibility(View.VISIBLE);
                    //ZoomControls seek_bar_zoom = (ZoomControls)findViewById(R.id.exposure_seekbar_zoom);
                    //seek_bar_zoom.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setProgressSeekbarScaled(SeekBar seekBar, double min_value, double max_value, double value) {
        seekBar.setMax(100);
        double scaling = (value - min_value) / (max_value - min_value);
        double frac = MainActivity.seekbarScalingInverse(scaling);
        int percent = (int) (frac * 100.0 + 0.5); // add 0.5 for rounding
        if (percent < 0)
            percent = 0;
        else if (percent > 100)
            percent = 100;
        seekBar.setProgress(percent);
    }

    public void clickedExposureLock(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedExposureLock");
        this.preview.toggleExposureLock();

        //ImageButton exposureLockButton = (ImageButton) findViewById(R.id.exposure_lock);
        //exposureLockButton.setImageResource(preview.isExposureLocked() ? R.drawable.exposure_locked : R.drawable.exposure_unlocked);

        //preview.showToast(exposure_lock_toast, preview.isExposureLocked() ? R.string.exposure_locked : R.string.exposure_unlocked);


        nextOverlaySet();
        setupOverlay();
    }

    public void clickedSettings(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedSettings");
        openSettings();
//        PopupMenu popupMenu = new PopupMenu(this, view);
//
//        popupMenu.inflate(R.menu.main);
//
//        popupMenu.setOnMenuItemClickListener(
//                new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//
//                        return onMenuItemSelected(1, item);
//                    }
//                });
//
//
//        popupMenu.show();
    }

    public boolean popupIsOpen() {
        if (popup_view != null) {
            return true;
        }
        return false;
    }

    // for testing
    public View getPopupButton(String key) {
        return popup_view.getPopupButton(key);
    }

    public void closePopup() {
        if (MyDebug.LOG)
            Log.d(TAG, "close popup");
        if (popupIsOpen()) {
            ViewGroup popup_container = (ViewGroup) findViewById(R.id.popup_container);
            popup_container.removeAllViews();
            popup_view.close();
            popup_view = null;
            popup_container.setVisibility(View.GONE);
            initImmersiveMode(); // to reset the timer when closing the popup
        }
    }

    public Bitmap getPreloadedBitmap(int resource) {
        Bitmap bm = this.preloaded_bitmap_resources.get(resource);
        return bm;
    }

    public void clickedPopupSettings(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedPopupSettings");
        final ViewGroup popup_container = (ViewGroup) findViewById(R.id.popup_container);
        if (popupIsOpen()) {
            closePopup();
            return;
        }
        if (preview.getCameraController() == null) {
            if (MyDebug.LOG)
                Log.d(TAG, "camera not opened!");
            return;
        }

        if (MyDebug.LOG)
            Log.d(TAG, "open popup");

        clearSeekBar();
        preview.cancelTimer(); // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings

        final long time_s = System.currentTimeMillis();

        {
            // prevent popup being transparent
            popup_container.setBackgroundColor(Color.BLACK);
            popup_container.setAlpha(0.9f);
        }

        popup_view = new PopupView(this);
        popup_container.setVisibility(View.VISIBLE);
        popup_container.addView(popup_view);

        // need to call layoutUI to make sure the new popup is oriented correctly
        // but need to do after the layout has been done, so we have a valid width/height to use
        popup_container.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @SuppressWarnings("deprecation")
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        if (MyDebug.LOG)
                            Log.d(TAG, "onGlobalLayout()");
                        if (MyDebug.LOG)
                            Log.d(TAG, "time after global layout: " + (System.currentTimeMillis() - time_s));
                        layoutUI();
                        if (MyDebug.LOG)
                            Log.d(TAG, "time after layoutUI: " + (System.currentTimeMillis() - time_s));
                        // stop listening - only want to call this once!
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                            popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            popup_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String ui_placement = sharedPreferences.getString(PreferenceKeys.getUIPlacementPreferenceKey(), "ui_right");
                        boolean ui_placement_right = ui_placement.equals("ui_right");
                        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, ui_placement_right ? 0.0f : 1.0f);
                        animation.setDuration(100);
                        popup_container.setAnimation(animation);
                    }
                }
        );

        if (MyDebug.LOG)
            Log.d(TAG, "time to create popup: " + (System.currentTimeMillis() - time_s));
    }

    private void openSettings() {
        if (MyDebug.LOG)
            Log.d(TAG, "openSettings");
        closePopup();
        preview.cancelTimer(); // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings
        preview.stopVideo(false); // important to stop video, as we'll be changing camera parameters when the settings window closes

        Bundle bundle = new Bundle();
        bundle.putInt("cameraId", this.preview.getCameraId());
        bundle.putString("camera_api", this.preview.getCameraAPI());
        bundle.putBoolean("using_android_l", this.preview.usingCamera2API());
        bundle.putBoolean("supports_auto_stabilise", this.supports_auto_stabilise);
        bundle.putBoolean("supports_force_video_4k", this.supports_force_video_4k);
        bundle.putBoolean("supports_camera2", this.supports_camera2);
        bundle.putBoolean("supports_face_detection", this.preview.supportsFaceDetection());
        bundle.putBoolean("supports_video_stabilization", this.preview.supportsVideoStabilization());
        bundle.putBoolean("can_disable_shutter_sound", this.preview.canDisableShutterSound());

        putBundleExtra(bundle, "color_effects", this.preview.getSupportedColorEffects());
        putBundleExtra(bundle, "scene_modes", this.preview.getSupportedSceneModes());
        putBundleExtra(bundle, "white_balances", this.preview.getSupportedWhiteBalances());
        putBundleExtra(bundle, "isos", this.preview.getSupportedISOs());
        bundle.putString("iso_key", this.preview.getISOKey());
        if (this.preview.getCameraController() != null) {
            bundle.putString("parameters_string", preview.getCameraController().getParametersString());
        }

        List<CameraController.Size> preview_sizes = this.preview.getSupportedPreviewSizes();
        if (preview_sizes != null) {
            int[] widths = new int[preview_sizes.size()];
            int[] heights = new int[preview_sizes.size()];
            int i = 0;
            for (CameraController.Size size : preview_sizes) {
                widths[i] = size.width;
                heights[i] = size.height;
                i++;
            }
            bundle.putIntArray("preview_widths", widths);
            bundle.putIntArray("preview_heights", heights);
        }
        bundle.putInt("preview_width", preview.getCurrentPreviewSize().width);
        bundle.putInt("preview_height", preview.getCurrentPreviewSize().height);

        List<CameraController.Size> sizes = this.preview.getSupportedPictureSizes();
        if (sizes != null) {
            int[] widths = new int[sizes.size()];
            int[] heights = new int[sizes.size()];
            int i = 0;
            for (CameraController.Size size : sizes) {
                widths[i] = size.width;
                heights[i] = size.height;
                i++;
            }
            bundle.putIntArray("resolution_widths", widths);
            bundle.putIntArray("resolution_heights", heights);
        }
        if (preview.getCurrentPictureSize() != null) {
            bundle.putInt("resolution_width", preview.getCurrentPictureSize().width);
            bundle.putInt("resolution_height", preview.getCurrentPictureSize().height);
        }

        List<String> video_quality = this.preview.getSupportedVideoQuality();
        if (video_quality != null && this.preview.getCameraController() != null) {
            String[] video_quality_arr = new String[video_quality.size()];
            String[] video_quality_string_arr = new String[video_quality.size()];
            int i = 0;
            for (String value : video_quality) {
                video_quality_arr[i] = value;
                video_quality_string_arr[i] = this.preview.getCamcorderProfileDescription(value);
                i++;
            }
            bundle.putStringArray("video_quality", video_quality_arr);
            bundle.putStringArray("video_quality_string", video_quality_string_arr);
        }
        if (preview.getCurrentVideoQuality() != null) {
            bundle.putString("current_video_quality", preview.getCurrentVideoQuality());
        }
        CamcorderProfile camcorder_profile = preview.getCamcorderProfile();
        bundle.putInt("video_frame_width", camcorder_profile.videoFrameWidth);
        bundle.putInt("video_frame_height", camcorder_profile.videoFrameHeight);
        bundle.putInt("video_bit_rate", camcorder_profile.videoBitRate);
        bundle.putInt("video_frame_rate", camcorder_profile.videoFrameRate);

        List<CameraController.Size> video_sizes = this.preview.getSupportedVideoSizes();
        if (video_sizes != null) {
            int[] widths = new int[video_sizes.size()];
            int[] heights = new int[video_sizes.size()];
            int i = 0;
            for (CameraController.Size size : video_sizes) {
                widths[i] = size.width;
                heights[i] = size.height;
                i++;
            }
            bundle.putIntArray("video_widths", widths);
            bundle.putIntArray("video_heights", heights);
        }

        putBundleExtra(bundle, "flash_values", this.preview.getSupportedFlashValues());
        putBundleExtra(bundle, "focus_values", this.preview.getSupportedFocusValues());

        setWindowFlagsForSettings();
        MyPreferenceFragment fragment = new MyPreferenceFragment();
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.prefs_container, fragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commit();
    }

    public void updateForSettings() {
        updateForSettings(null);
    }

    public void updateForSettings(String toast_message) {
        if (MyDebug.LOG) {
            Log.d(TAG, "updateForSettings()");
            if (toast_message != null) {
                Log.d(TAG, "toast_message: " + toast_message);
            }
        }
        String saved_focus_value = null;
        if (preview.getCameraController() != null && preview.isVideo() && !preview.focusIsVideo()) {
            saved_focus_value = preview.getCurrentFocusValue(); // n.b., may still be null
            // make sure we're into continuous video mode
            // workaround for bug on Samsung Galaxy S5 with UHD, where if the user switches to another (non-continuous-video) focus mode, then goes to Settings, then returns and records video, the preview freezes and the video is corrupted
            // so to be safe, we always reset to continuous video mode, and then reset it afterwards
            preview.updateFocusForVideo(false);
        }
        if (MyDebug.LOG)
            Log.d(TAG, "saved_focus_value: " + saved_focus_value);

        updateFolderHistory();

        // update camera for changes made in prefs - do this without closing and reopening the camera app if possible for speed!
        // but need workaround for Nexus 7 bug, where scene mode doesn't take effect unless the camera is restarted - I can reproduce this with other 3rd party camera apps, so may be a Nexus 7 issue...
        boolean need_reopen = false;
        if (preview.getCameraController() != null) {
            String scene_mode = preview.getCameraController().getSceneMode();
            if (MyDebug.LOG)
                Log.d(TAG, "scene mode was: " + scene_mode);
            String key = PreferenceKeys.getSceneModePreferenceKey();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String value = sharedPreferences.getString(key, preview.getCameraController().getDefaultSceneMode());
            if (!value.equals(scene_mode)) {
                if (MyDebug.LOG)
                    Log.d(TAG, "scene mode changed to: " + value);
                need_reopen = true;
            }
        }

        layoutUI(); // needed in case we've changed left/right handed UI
        applicationInterface.getLocationSupplier().setupLocationListener(); // in case we've enabled GPS
        if (toast_message != null)
            block_startup_toast = true;
        if (need_reopen || preview.getCameraController() == null) { // if camera couldn't be opened before, might as well try again
            preview.onPause();
            preview.onResume();
        } else {
            preview.setCameraDisplayOrientation(); // need to call in case the preview rotation option was changed
            preview.pausePreview();
            preview.setupCamera(false);
        }
        block_startup_toast = false;
        if (toast_message != null && toast_message.length() > 0)
            preview.showToast(null, toast_message);

        if (saved_focus_value != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "switch focus back to: " + saved_focus_value);
            preview.updateFocus(saved_focus_value, true, false);
        }
    }

    MyPreferenceFragment getPreferenceFragment() {
        MyPreferenceFragment fragment = (MyPreferenceFragment) getFragmentManager().findFragmentByTag("PREFERENCE_FRAGMENT");
        return fragment;
    }

    @Override
    public void onBackPressed() {
        final MyPreferenceFragment fragment = getPreferenceFragment();
        if (screen_is_locked) {
            preview.showToast(screen_locked_toast, R.string.screen_is_locked);
            return;
        }
        if (fragment != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "close settings");
            setWindowFlagsForCamera();
            updateForSettings();
        } else {
            if (popupIsOpen()) {
                closePopup();
                return;
            }
        }
        super.onBackPressed();
    }

    boolean usingKitKatImmersiveMode() {
        // whether we are using a Kit Kat style immersive mode (either hiding GUI, or everything)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String immersive_mode = sharedPreferences.getString(PreferenceKeys.getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
            if (immersive_mode.equals("immersive_mode_gui") || immersive_mode.equals("immersive_mode_everything"))
                return true;
        }
        return false;
    }

    private void setImmersiveTimer() {
        if (immersive_timer_handler != null && immersive_timer_runnable != null) {
            immersive_timer_handler.removeCallbacks(immersive_timer_runnable);
        }
        immersive_timer_handler = new Handler();
        immersive_timer_handler.postDelayed(immersive_timer_runnable = new Runnable() {
            @Override
            public void run() {
                if (MyDebug.LOG)
                    Log.d(TAG, "setImmersiveTimer: run");
                if (!camera_in_background && !popupIsOpen() && usingKitKatImmersiveMode())
                    setImmersiveMode(true);
            }
        }, 500);
    }

    void initImmersiveMode() {
        if (!usingKitKatImmersiveMode()) {
            setImmersiveMode(true);
        } else {
            // don't start in immersive mode, only after a timer
            setImmersiveTimer();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    void setImmersiveMode(boolean on) {
        if (MyDebug.LOG)
            Log.d(TAG, "setImmersiveMode: " + on);
        // n.b., preview.setImmersiveMode() is called from onSystemUiVisibilityChange()
        if (on) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && usingKitKatImmersiveMode()) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String immersive_mode = sharedPreferences.getString(PreferenceKeys.getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
                if (immersive_mode.equals("immersive_mode_low_profile"))
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                else
                    getWindow().getDecorView().setSystemUiVisibility(0);
            }
        } else
            getWindow().getDecorView().setSystemUiVisibility(0);
    }

    private void setWindowFlagsForCamera() {
        if (MyDebug.LOG)
            Log.d(TAG, "setWindowFlagsForCamera");
        {
            Intent intent = new Intent(this, MyWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
            ComponentName widgetComponent = new ComponentName(this, MyWidgetProvider.class);
            int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
            sendBroadcast(intent);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // force to landscape mode
        //	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // keep screen active - see http://stackoverflow.com/questions/2131948/force-screen-on
        if (sharedPreferences.getBoolean(PreferenceKeys.getKeepDisplayOnPreferenceKey(), true)) {
            if (MyDebug.LOG)
                Log.d(TAG, "do keep screen on");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "don't keep screen on");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (sharedPreferences.getBoolean(PreferenceKeys.getShowWhenLockedPreferenceKey(), true)) {
            if (MyDebug.LOG)
                Log.d(TAG, "do show when locked");
            // keep Open Camera on top of screen-lock (will still need to unlock when going to gallery or settings)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "don't show when locked");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        // set screen to max brightness - see http://stackoverflow.com/questions/11978042/android-screen-brightness-max-value
        // done here rather than onCreate, so that changing it in preferences takes effect without restarting app
        {
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            if (sharedPreferences.getBoolean(PreferenceKeys.getMaxBrightnessPreferenceKey(), true)) {
                layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            } else {
                layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            }
            getWindow().setAttributes(layout);
        }

        initImmersiveMode();
        camera_in_background = false;
    }

    private void setWindowFlagsForSettings() {
        // allow screen rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        // revert to standard screen blank behaviour
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // settings should still be protected by screen lock
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        {
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            getWindow().setAttributes(layout);
        }

        setImmersiveMode(false);
        camera_in_background = true;
    }

    private void showPreview(boolean show) {
        if (MyDebug.LOG)
            Log.d(TAG, "showPreview: " + show);
        final ViewGroup container = (ViewGroup) findViewById(R.id.hide_container);
        container.setBackgroundColor(Color.BLACK);
        container.setAlpha(show ? 0.0f : 1.0f);
    }

    public void updateGalleryIconToBlank() {
        if (MyDebug.LOG)
            Log.d(TAG, "updateGalleryIconToBlank");
        ImageButton galleryButton = (ImageButton) this.findViewById(R.id.gallery);
        int bottom = galleryButton.getPaddingBottom();
        int top = galleryButton.getPaddingTop();
        int right = galleryButton.getPaddingRight();
        int left = galleryButton.getPaddingLeft();
	    /*if( MyDebug.LOG )
			Log.d(TAG, "padding: " + bottom);*/
        galleryButton.setImageBitmap(null);
        galleryButton.setImageResource(R.drawable.gallery);
        // workaround for setImageResource also resetting padding, Android bug
        galleryButton.setPadding(left, top, right, bottom);
        gallery_bitmap = null;
    }

    void updateGalleryIcon(Bitmap thumbnail) {
        if (MyDebug.LOG)
            Log.d(TAG, "updateGalleryIcon: " + thumbnail);
        ImageButton galleryButton = (ImageButton) this.findViewById(R.id.gallery);
        galleryButton.setImageBitmap(thumbnail);
        gallery_bitmap = thumbnail;
    }

    public void updateGalleryIcon() {
        if (MyDebug.LOG)
            Log.d(TAG, "updateGalleryIcon");
        long time_s = System.currentTimeMillis();
        StorageUtils.Media media = applicationInterface.getStorageUtils().getLatestMedia();
        Bitmap thumbnail = null;
        if (media != null && getContentResolver() != null) {
            // check for getContentResolver() != null, as have had reported Google Play crashes
            if (media.video) {
                thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
            } else {
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            }
            if (thumbnail != null) {
                if (media.orientation != 0) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "thumbnail size is " + thumbnail.getWidth() + " x " + thumbnail.getHeight());
                    Matrix matrix = new Matrix();
                    matrix.setRotate(media.orientation, thumbnail.getWidth() * 0.5f, thumbnail.getHeight() * 0.5f);
                    try {
                        Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
                        // careful, as rotated_thumbnail is sometimes not a copy!
                        if (rotated_thumbnail != thumbnail) {
                            thumbnail.recycle();
                            thumbnail = rotated_thumbnail;
                        }
                    } catch (Throwable t) {
                        if (MyDebug.LOG)
                            Log.d(TAG, "failed to rotate thumbnail");
                    }
                }
            }
        }
        // since we're now setting the thumbnail to the latest media on disk, we need to make sure clicking the Gallery goes to this
        applicationInterface.getStorageUtils().clearLastMediaScanned();
        if (thumbnail != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "set gallery button to thumbnail");
            updateGalleryIcon(thumbnail);
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "set gallery button to blank");
            updateGalleryIconToBlank();
        }
        if (MyDebug.LOG)
            Log.d(TAG, "time to update gallery icon: " + (System.currentTimeMillis() - time_s));
    }

    public void clickedGallery(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedGallery");
        //Intent intent = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Uri uri = applicationInterface.getStorageUtils().getLastMediaScanned();
        if (uri == null) {
            if (MyDebug.LOG)
                Log.d(TAG, "go to latest media");
            StorageUtils.Media media = applicationInterface.getStorageUtils().getLatestMedia();
            if (media != null) {
                uri = media.uri;
            }
        }

        if (uri != null) {
            // check uri exists
            if (MyDebug.LOG)
                Log.d(TAG, "found most recent uri: " + uri);
            try {
                ContentResolver cr = getContentResolver();
                ParcelFileDescriptor pfd = cr.openFileDescriptor(uri, "r");
                if (pfd == null) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "uri no longer exists (1): " + uri);
                    uri = null;
                }
                pfd.close();
            } catch (IOException e) {
                if (MyDebug.LOG)
                    Log.d(TAG, "uri no longer exists (2): " + uri);
                uri = null;
            }
        }
        if (uri == null) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        if (!is_test) {
            // don't do if testing, as unclear how to exit activity to finish test (for testGallery())
            if (MyDebug.LOG)
                Log.d(TAG, "launch uri:" + uri);
            final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
            try {
                // REVIEW_ACTION means we can view video files without autoplaying
                Intent intent = new Intent(REVIEW_ACTION, uri);
                this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                if (MyDebug.LOG)
                    Log.d(TAG, "REVIEW_ACTION intent didn't work, try ACTION_VIEW");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // from http://stackoverflow.com/questions/11073832/no-activity-found-to-handle-intent - needed to fix crash if no gallery app installed
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("blah")); // test
                if (intent.resolveActivity(getPackageManager()) != null) {
                    this.startActivity(intent);
                } else {
                    preview.showToast(null, R.string.no_gallery_app);
                }
            }
        }
    }

    private void updateFolderHistory() {
        String folder_name = applicationInterface.getStorageUtils().getSaveLocation();
        updateFolderHistory(folder_name);
        updateGalleryIcon(); // if the folder has changed, need to update the gallery icon
    }

    private void updateFolderHistory(String folder_name) {
        if (MyDebug.LOG) {
            Log.d(TAG, "updateFolderHistory: " + folder_name);
            Log.d(TAG, "save_location_history size: " + save_location_history.size());
            for (int i = 0; i < save_location_history.size(); i++) {
                Log.d(TAG, save_location_history.get(i));
            }
        }
        while (save_location_history.remove(folder_name)) {
        }
        save_location_history.add(folder_name);
        while (save_location_history.size() > 6) {
            save_location_history.remove(0);
        }
        writeSaveLocations();
        if (MyDebug.LOG) {
            Log.d(TAG, "updateFolderHistory exit:");
            Log.d(TAG, "save_location_history size: " + save_location_history.size());
            for (int i = 0; i < save_location_history.size(); i++) {
                Log.d(TAG, save_location_history.get(i));
            }
        }
    }

    public void clearFolderHistory() {
        if (MyDebug.LOG)
            Log.d(TAG, "clearFolderHistory");
        save_location_history.clear();
        updateFolderHistory(); // to re-add the current choice, and save
    }

    private void writeSaveLocations() {
        if (MyDebug.LOG)
            Log.d(TAG, "writeSaveLocations");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("save_location_history_size", save_location_history.size());
        if (MyDebug.LOG)
            Log.d(TAG, "save_location_history_size = " + save_location_history.size());
        for (int i = 0; i < save_location_history.size(); i++) {
            String string = save_location_history.get(i);
            editor.putString("save_location_history_" + i, string);
        }
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void openFolderChooserDialogSAF() {
        if (MyDebug.LOG)
            Log.d(TAG, "openFolderChooserDialogSAF");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 42);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (MyDebug.LOG)
            Log.d(TAG, "onActivityResult: " + requestCode);
        if (requestCode == 42 && resultCode == RESULT_OK && resultData != null) {
            Uri treeUri = resultData.getData();
            if (MyDebug.LOG)
                Log.d(TAG, "returned treeUri: " + treeUri);
            ContentResolver contentResolver = this.getContentResolver();
            contentResolver.takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), treeUri.toString());
            editor.apply();
            String filename = applicationInterface.getStorageUtils().getImageFolderNameSAF();
            if (filename != null) {
                preview.showToast(null, getResources().getString(R.string.changed_save_location) + "\n" + filename);
            }
        } else if (requestCode == 42) {
            if (MyDebug.LOG)
                Log.d(TAG, "SAF dialog cancelled");
            // cancelled - if the user had yet to set a save location, make sure we switch SAF back off
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String uri = sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "");
            if (uri.length() == 0) {
                if (MyDebug.LOG)
                    Log.d(TAG, "no SAF save location was set");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false);
                editor.apply();
                preview.showToast(null, R.string.saf_cancelled);
            }
        } else if (requestCode == ACTIVITY_CHOOSE_SOUND && resultCode == RESULT_OK) {
            // Sound recorder does not support EXTRA_OUTPUT
            Uri uri = resultData.getData();

            if (uri != null) {
                mLastAudioPath = uri.toString();

                if (mLastAudioPath != null && mLastAudioPath.startsWith("file://"))
                    mLastAudioPath = mLastAudioPath.substring(7);

                if (!new File(mLastAudioPath).exists()) {
                    mLastAudioPath = getAudioFilePathFromUri(uri);

                    if (mLastAudioPath == null)
                        mLastAudioPath = getRealPathFromURI(uri);
                }

            }
        }
    }

    private String getAudioFilePathFromUri(Uri uri) {
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            return cursor.getString(index);
        } else
            return null;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        CursorLoader cursorLoader = new CursorLoader(
                this,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void openFolderChooserDialog() {
        if (MyDebug.LOG)
            Log.d(TAG, "openFolderChooserDialog");
        showPreview(false);
        setWindowFlagsForSettings();
        final String orig_save_location = applicationInterface.getStorageUtils().getSaveLocation();
        FolderChooserDialog fragment = new FolderChooserDialog() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (MyDebug.LOG)
                    Log.d(TAG, "FolderChooserDialog dismissed");
                setWindowFlagsForCamera();
                showPreview(true);
                final String new_save_location = applicationInterface.getStorageUtils().getSaveLocation();
                if (!orig_save_location.equals(new_save_location)) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "changed save_folder to: " + applicationInterface.getStorageUtils().getSaveLocation());
                    updateFolderHistory();
                    preview.showToast(null, getResources().getString(R.string.changed_save_location) + "\n" + applicationInterface.getStorageUtils().getSaveLocation());
                }
                super.onDismiss(dialog);
            }
        };
        fragment.show(getFragmentManager(), "FOLDER_FRAGMENT");
    }

    private void longClickedGallery() {
        if (MyDebug.LOG)
            Log.d(TAG, "longClickedGallery");
        if (applicationInterface.getStorageUtils().isUsingSAF()) {
            // SAF doesn't support history yet, so go straight to dialog
            openFolderChooserDialogSAF();
            return;
        }
        if (save_location_history.size() <= 1) {
            // go straight to choose folder dialog
            openFolderChooserDialog();
            return;
        }

        showPreview(false);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.choose_save_location);
        CharSequence[] items = new CharSequence[save_location_history.size() + 2];
        int index = 0;
        // save_location_history is stored in order most-recent-last
        for (int i = 0; i < save_location_history.size(); i++) {
            items[index++] = save_location_history.get(save_location_history.size() - 1 - i);
        }
        final int clear_index = index;
        items[index++] = getResources().getString(R.string.clear_folder_history);
        final int new_index = index;
        items[index++] = getResources().getString(R.string.choose_another_folder);
        alertDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == clear_index) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "selected clear save history");
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.clear_folder_history)
                            .setMessage(R.string.clear_folder_history_question)
                            .setPositiveButton(R.string.answer_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (MyDebug.LOG)
                                        Log.d(TAG, "confirmed clear save history");
                                    clearFolderHistory();
                                    setWindowFlagsForCamera();
                                    showPreview(true);
                                }
                            })
                            .setNegativeButton(R.string.answer_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (MyDebug.LOG)
                                        Log.d(TAG, "don't clear save history");
                                    setWindowFlagsForCamera();
                                    showPreview(true);
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface arg0) {
                                    if (MyDebug.LOG)
                                        Log.d(TAG, "cancelled clear save history");
                                    setWindowFlagsForCamera();
                                    showPreview(true);
                                }
                            })
                            .show();
                } else if (which == new_index) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "selected choose new folder");
                    openFolderChooserDialog();
                } else {
                    if (MyDebug.LOG)
                        Log.d(TAG, "selected: " + which);
                    if (which >= 0 && which < save_location_history.size()) {
                        String save_folder = save_location_history.get(save_location_history.size() - 1 - which);
                        if (MyDebug.LOG)
                            Log.d(TAG, "changed save_folder from history to: " + save_folder);
                        preview.showToast(null, getResources().getString(R.string.changed_save_location) + "\n" + save_folder);
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(PreferenceKeys.getSaveLocationPreferenceKey(), save_folder);
                        editor.apply();
                        updateFolderHistory(); // to move new selection to most recent
                    }
                    setWindowFlagsForCamera();
                    showPreview(true);
                }
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                setWindowFlagsForCamera();
                showPreview(true);
            }
        });
        alertDialog.show();
        //getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        setWindowFlagsForSettings();
    }

    public void clickedShare(View view) {
//		if( MyDebug.LOG )
//			Log.d(TAG, "clickedShare");
//		applicationInterface.shareLastImage();
    }

    public void clickedTrash(View view) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedTrash");
        applicationInterface.trashLastImage();
    }

    private void takePicture() {
        if (MyDebug.LOG)
            Log.d(TAG, "takePicture");
        closePopup();
        this.preview.takePicturePressed();

    }

    void lockScreen() {
        ((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
                //return true;
            }
        });
        screen_is_locked = true;
    }

    void unlockScreen() {
        ((ViewGroup) findViewById(R.id.locker)).setOnTouchListener(null);
        screen_is_locked = false;
    }

    boolean isScreenLocked() {
        return screen_is_locked;
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (MyDebug.LOG)
            Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(state);
        if (this.preview != null) {
            preview.onSaveInstanceState(state);
        }
        if (this.applicationInterface != null) {
            applicationInterface.onSaveInstanceState(state);
        }
    }

    boolean supportsExposureButton() {
        if (preview.getCameraController() == null)
            return false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String iso_value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), preview.getCameraController().getDefaultISO());
        boolean manual_iso = !iso_value.equals(preview.getCameraController().getDefaultISO());
        boolean supports_exposure = preview.supportsExposures() || (manual_iso && preview.supportsISORange());
        return supports_exposure;
    }

    void cameraSetup() {
        if (MyDebug.LOG)
            Log.d(TAG, "cameraSetup");
        if (this.supportsForceVideo4K() && preview.usingCamera2API()) {
            if (MyDebug.LOG)
                Log.d(TAG, "using Camera2 API, so can disable the force 4K option");
            this.disableForceVideo4K();
        }
        if (this.supportsForceVideo4K() && preview.getSupportedVideoSizes() != null) {
            for (CameraController.Size size : preview.getSupportedVideoSizes()) {
                if (size.width >= 3840 && size.height >= 2160) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "camera natively supports 4K, so can disable the force option");
                    this.disableForceVideo4K();
                }
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        {
            if (MyDebug.LOG)
                Log.d(TAG, "set up zoom");
            if (MyDebug.LOG)
                Log.d(TAG, "has_zoom? " + preview.supportsZoom());
            ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom);
            SeekBar zoomSeekBar = (SeekBar) findViewById(R.id.zoom_seekbar);

            if (preview.supportsZoom()) {
                if (sharedPreferences.getBoolean(PreferenceKeys.getShowZoomControlsPreferenceKey(), false)) {
                    zoomControls.setIsZoomInEnabled(true);
                    zoomControls.setIsZoomOutEnabled(true);
                    zoomControls.setZoomSpeed(20);

                    zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            zoomIn();
                        }
                    });
                    zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            zoomOut();
                        }
                    });
                    if (!applicationInterface.inImmersiveMode()) {
                        zoomControls.setVisibility(View.GONE);
                    }
                } else {
                    zoomControls.setVisibility(View.INVISIBLE); // must be INVISIBLE not GONE, so we can still position the zoomSeekBar relative to it
                }

                zoomSeekBar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
                zoomSeekBar.setMax(preview.getMaxZoom());
                zoomSeekBar.setProgress(preview.getMaxZoom() - preview.getCameraController().getZoom());
                zoomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (MyDebug.LOG)
                            Log.d(TAG, "zoom onProgressChanged: " + progress);
                        preview.zoomTo(preview.getMaxZoom() - progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                if (sharedPreferences.getBoolean(PreferenceKeys.getShowZoomSliderControlsPreferenceKey(), true)) {
                    if (!applicationInterface.inImmersiveMode()) {
                        zoomSeekBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    zoomSeekBar.setVisibility(View.INVISIBLE);
                }
            } else {
                zoomControls.setVisibility(View.GONE);
                zoomSeekBar.setVisibility(View.GONE);
            }
        }
        {
            if (MyDebug.LOG)
                Log.d(TAG, "set up manual focus");
            SeekBar focusSeekBar = (SeekBar) findViewById(R.id.focus_seekbar);
            focusSeekBar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
            setProgressSeekbarScaled(focusSeekBar, 0.0, preview.getMinimumFocusDistance(), preview.getCameraController().getFocusDistance());
            focusSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    double frac = progress / (double) 100.0;
                    double scaling = MainActivity.seekbarScaling(frac);
                    float focus_distance = (float) (scaling * preview.getMinimumFocusDistance());
                    preview.setFocusDistance(focus_distance);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            final int visibility = preview.getCurrentFocusValue() != null && this.getPreview().getCurrentFocusValue().equals("focus_mode_manual2") ? View.VISIBLE : View.INVISIBLE;
            focusSeekBar.setVisibility(visibility);
        }
        {
            if (preview.supportsISORange()) {
                if (MyDebug.LOG)
                    Log.d(TAG, "set up iso");
                SeekBar iso_seek_bar = ((SeekBar) findViewById(R.id.iso_seekbar));
                iso_seek_bar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
                setProgressSeekbarScaled(iso_seek_bar, preview.getMinimumISO(), preview.getMaximumISO(), preview.getCameraController().getISO());
                iso_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (MyDebug.LOG)
                            Log.d(TAG, "iso seekbar onProgressChanged: " + progress);
                        double frac = progress / (double) 100.0;
                        if (MyDebug.LOG)
                            Log.d(TAG, "exposure_time frac: " + frac);
                        double scaling = MainActivity.seekbarScaling(frac);
                        if (MyDebug.LOG)
                            Log.d(TAG, "exposure_time scaling: " + scaling);
                        int min_iso = preview.getMinimumISO();
                        int max_iso = preview.getMaximumISO();
                        int iso = min_iso + (int) (scaling * (max_iso - min_iso));
                        preview.setISO(iso);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                if (preview.supportsExposureTime()) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "set up exposure time");
                    SeekBar exposure_time_seek_bar = ((SeekBar) findViewById(R.id.exposure_time_seekbar));
                    exposure_time_seek_bar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
                    setProgressSeekbarScaled(exposure_time_seek_bar, preview.getMinimumExposureTime(), preview.getMaximumExposureTime(), preview.getCameraController().getExposureTime());
                    exposure_time_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (MyDebug.LOG)
                                Log.d(TAG, "exposure_time seekbar onProgressChanged: " + progress);
                            double frac = progress / (double) 100.0;
                            if (MyDebug.LOG)
                                Log.d(TAG, "exposure_time frac: " + frac);
                            //long exposure_time = min_exposure_time + (long)(frac * (max_exposure_time - min_exposure_time));
                            //double exposure_time_r = min_exposure_time_r + (frac * (max_exposure_time_r - min_exposure_time_r));
                            //long exposure_time = (long)(1.0 / exposure_time_r);
                            // we use the formula: [100^(percent/100) - 1]/99.0 rather than a simple linear scaling
                            double scaling = MainActivity.seekbarScaling(frac);
                            if (MyDebug.LOG)
                                Log.d(TAG, "exposure_time scaling: " + scaling);
                            long min_exposure_time = preview.getMinimumExposureTime();
                            long max_exposure_time = preview.getMaximumExposureTime();
                            long exposure_time = min_exposure_time + (long) (scaling * (max_exposure_time - min_exposure_time));
                            preview.setExposureTime(exposure_time);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                }
            }
        }
        {
            if (preview.supportsExposures()) {
                if (MyDebug.LOG)
                    Log.d(TAG, "set up exposure compensation");
                final int min_exposure = preview.getMinimumExposure();
                SeekBar exposure_seek_bar = ((SeekBar) findViewById(R.id.exposure_seekbar));
                exposure_seek_bar.setOnSeekBarChangeListener(null); // clear an existing listener - don't want to call the listener when setting up the progress bar to match the existing state
                exposure_seek_bar.setMax(preview.getMaximumExposure() - min_exposure);
                exposure_seek_bar.setProgress(preview.getCurrentExposure() - min_exposure);
                exposure_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (MyDebug.LOG)
                            Log.d(TAG, "exposure seekbar onProgressChanged: " + progress);
                        preview.setExposure(min_exposure + progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                /**
                 ZoomControls seek_bar_zoom = (ZoomControls)findViewById(R.id.exposure_seekbar_zoom);
                 seek_bar_zoom.setOnZoomInClickListener(new View.OnClickListener(){
                 public void onClick(View v){
                 changeExposure(1);
                 }
                 });
                 seek_bar_zoom.setOnZoomOutClickListener(new View.OnClickListener(){
                 public void onClick(View v){
                 changeExposure(-1);
                 }
                 });*/
            }
        }


        View exposureButton = (View) findViewById(R.id.exposure);
        exposureButton.setVisibility(supportsExposureButton() && !applicationInterface.inImmersiveMode() ? View.VISIBLE : View.GONE);

//        ImageButton exposureLockButton = (ImageButton) findViewById(R.id.exposure_lock);
//        exposureLockButton.setVisibility(View.VISIBLE);
/*
	    ImageButton exposureLockButton = (ImageButton) findViewById(R.id.exposure_lock);
	    exposureLockButton.setVisibility(preview.supportsExposureLock() && !applicationInterface.inImmersiveMode() ? View.VISIBLE : View.GONE);
	    if( preview.supportsExposureLock() ) {
			exposureLockButton.setImageResource(preview.isExposureLocked() ? R.drawable.exposure_locked : R.drawable.exposure_unlocked);
	    }*/

        setPopupIcon(); // needed so that the icon is set right even if no flash mode is set when starting up camera (e.g., switching to front camera with no flash)

        setTakePhotoIcon();

        if (!block_startup_toast) {
            this.showPhotoVideoToast();
        }
    }

    public boolean supportsAutoStabilise() {
        return this.supports_auto_stabilise;
    }

    public boolean supportsForceVideo4K() {
        return this.supports_force_video_4k;
    }

    public boolean supportsCamera2() {
        return this.supports_camera2;
    }

    void disableForceVideo4K() {
        this.supports_force_video_4k = false;
    }

    /*public static String getDonateMarketLink() {
    	return "market://details?id=harman.mark.donation";
    }*/

    @SuppressWarnings("deprecation")
    public long freeMemory() { // return free memory in MB
        try {
            File folder = applicationInterface.getStorageUtils().getImageFolder();
            if (folder == null) {
                throw new IllegalArgumentException(); // so that we fall onto the backup
            }
            StatFs statFs = new StatFs(folder.getAbsolutePath());
            // cast to long to avoid overflow!
            long blocks = statFs.getAvailableBlocks();
            long size = statFs.getBlockSize();
            long free = (blocks * size) / 1048576;
			/*if( MyDebug.LOG ) {
				Log.d(TAG, "freeMemory blocks: " + blocks + " size: " + size + " free: " + free);
			}*/
            return free;
        } catch (IllegalArgumentException e) {
            // this can happen if folder doesn't exist, or don't have read access
            // if the save folder is a subfolder of DCIM, we can just use that instead
            try {
                String folder_name = applicationInterface.getStorageUtils().getSaveLocation();
                if (!folder_name.startsWith("/")) {
                    File folder = StorageUtils.getBaseFolder();
                    StatFs statFs = new StatFs(folder.getAbsolutePath());
                    // cast to long to avoid overflow!
                    long blocks = statFs.getAvailableBlocks();
                    long size = statFs.getBlockSize();
                    long free = (blocks * size) / 1048576;
        			/*if( MyDebug.LOG ) {
        				Log.d(TAG, "freeMemory blocks: " + blocks + " size: " + size + " free: " + free);
        			}*/
                    return free;
                }
            } catch (IllegalArgumentException e2) {
                // just in case
            }
        }
        return -1;
    }

    public Preview getPreview() {
        return this.preview;
    }

    public LocationSupplier getLocationSupplier() {
        return this.applicationInterface.getLocationSupplier();
    }

    public StorageUtils getStorageUtils() {
        return this.applicationInterface.getStorageUtils();
    }

    public File getImageFolder() {
        return this.applicationInterface.getStorageUtils().getImageFolder();
    }

    public ToastBoxer getChangedAutoStabiliseToastBoxer() {
        return changed_auto_stabilise_toast;
    }

    private void showPhotoVideoToast() {
        if (MyDebug.LOG)
            Log.d(TAG, "showPhotoVideoToast");
        CameraController camera_controller = preview.getCameraController();
        if (camera_controller == null || this.camera_in_background)
            return;
        String toast_string = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preview.isVideo()) {
            CamcorderProfile profile = preview.getCamcorderProfile();
            String bitrate_string = "";
            if (profile.videoBitRate >= 10000000)
                bitrate_string = profile.videoBitRate / 1000000 + "Mbps";
            else if (profile.videoBitRate >= 10000)
                bitrate_string = profile.videoBitRate / 1000 + "Kbps";
            else
                bitrate_string = profile.videoBitRate + "bps";

            String timer_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0");
            //toast_string = getResources().getString(R.string.video) + ": " + profile.videoFrameWidth + "x" + profile.videoFrameHeight + ", " + profile.videoFrameRate + "fps, " + bitrate_string;
            boolean record_audio = sharedPreferences.getBoolean(PreferenceKeys.getRecordAudioPreferenceKey(), true);
            if (!record_audio) {
                //	toast_string += "\n" + getResources().getString(R.string.audio_disabled);
            }
            if (timer_value.length() > 0 && !timer_value.equals("0")) {
                String[] entries_array = getResources().getStringArray(R.array.preference_video_max_duration_entries);
                String[] values_array = getResources().getStringArray(R.array.preference_video_max_duration_values);
                int index = Arrays.asList(values_array).indexOf(timer_value);
                if (index != -1) { // just in case!
                    String entry = entries_array[index];
                    //	toast_string += "\n" + getResources().getString(R.string.max_duration) +": " + entry;
                }
            }
            if (sharedPreferences.getBoolean(PreferenceKeys.getVideoFlashPreferenceKey(), false) && preview.supportsFlash()) {
                //toast_string += "\n" + getResources().getString(R.string.preference_video_flash);
            }
        } else {
            //toast_string = getResources().getString(R.string.photo);
            CameraController.Size current_size = preview.getCurrentPictureSize();
            //toast_string += " " + current_size.width + "x" + current_size.height;
            if (preview.supportsFocus() && preview.getSupportedFocusValues().size() > 1) {
                String focus_value = preview.getCurrentFocusValue();
                if (focus_value != null && !focus_value.equals("focus_mode_auto")) {
                    String focus_entry = preview.findFocusEntryForValue(focus_value);
                    if (focus_entry != null) {
                        toast_string += "\n" + focus_entry;
                    }
                }
            }
        }
        String iso_value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), camera_controller.getDefaultISO());
        if (!iso_value.equals(camera_controller.getDefaultISO())) {
            //toast_string += "\nISO: " + iso_value;
            if (preview.supportsExposureTime()) {
                long exposure_time_value = sharedPreferences.getLong(PreferenceKeys.getExposureTimePreferenceKey(), camera_controller.getDefaultExposureTime());
                //toast_string += " " + preview.getExposureTimeString(exposure_time_value);
            }
        }
        int current_exposure = camera_controller.getExposureCompensation();
        if (current_exposure != 0) {
            //	toast_string += "\n" + preview.getExposureCompensationString(current_exposure);
        }
        String scene_mode = camera_controller.getSceneMode();
        if (scene_mode != null && !scene_mode.equals(camera_controller.getDefaultSceneMode())) {
            //toast_string += "\n" + getResources().getString(R.string.scene_mode) + ": " + scene_mode;
        }
        String white_balance = camera_controller.getWhiteBalance();
        if (white_balance != null && !white_balance.equals(camera_controller.getDefaultWhiteBalance())) {
            //	toast_string += "\n" + getResources().getString(R.string.white_balance) + ": " + white_balance;
        }
        String color_effect = camera_controller.getColorEffect();
        if (color_effect != null && !color_effect.equals(camera_controller.getDefaultColorEffect())) {
            //toast_string += "\n" + getResources().getString(R.string.color_effect) + ": " + color_effect;
        }
        String lock_orientation = sharedPreferences.getString(PreferenceKeys.getLockOrientationPreferenceKey(), "none");
        if (!lock_orientation.equals("none")) {
            String[] entries_array = getResources().getStringArray(R.array.preference_lock_orientation_entries);
            String[] values_array = getResources().getStringArray(R.array.preference_lock_orientation_values);
            int index = Arrays.asList(values_array).indexOf(lock_orientation);
            if (index != -1) { // just in case!
                String entry = entries_array[index];
                //toast_string += "\n" + entry;
            }
        }
        String timer = sharedPreferences.getString(PreferenceKeys.getTimerPreferenceKey(), "0");
        if (!timer.equals("0")) {
            String[] entries_array = getResources().getStringArray(R.array.preference_timer_entries);
            String[] values_array = getResources().getStringArray(R.array.preference_timer_values);
            int index = Arrays.asList(values_array).indexOf(timer);
            if (index != -1) { // just in case!
                String entry = entries_array[index];
                //toast_string += "\n" + getResources().getString(R.string.preference_timer) + ": " + entry;
            }
        }
        String repeat = applicationInterface.getRepeatPref();
        if (!repeat.equals("1")) {
            String[] entries_array = getResources().getStringArray(R.array.preference_burst_mode_entries);
            String[] values_array = getResources().getStringArray(R.array.preference_burst_mode_values);
            int index = Arrays.asList(values_array).indexOf(repeat);
            if (index != -1) { // just in case!
                String entry = entries_array[index];
                //	toast_string += "\n" + getResources().getString(R.string.preference_burst_mode) + ": " + entry;
            }
        }

        //preview.showToast(switch_video_toast, toast_string);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initSound() {
        if (sound_pool == null) {
            if (MyDebug.LOG)
                Log.d(TAG, "create new sound_pool");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audio_attributes = new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_SYSTEM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                sound_pool = new SoundPool.Builder()
                        .setMaxStreams(1)
                        .setAudioAttributes(audio_attributes)
                        .build();
            } else {
                sound_pool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
            }
            sound_ids = new SparseIntArray();
        }
    }

    private void releaseSound() {
        if (sound_pool != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "release sound_pool");
            sound_pool.release();
            sound_pool = null;
            sound_ids = null;
        }
    }

    // must be called before playSound (allowing enough time to load the sound)
    void loadSound(int resource_id) {
        if (sound_pool != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "loading sound resource: " + resource_id);
            int sound_id = sound_pool.load(this, resource_id, 1);
            if (MyDebug.LOG)
                Log.d(TAG, "    loaded sound: " + sound_id);
            sound_ids.put(resource_id, sound_id);
        }
    }

    // must call loadSound first (allowing enough time to load the sound)
    void playSound(int resource_id) {
        if (sound_pool != null) {
            if (sound_ids.indexOfKey(resource_id) < 0) {
                if (MyDebug.LOG)
                    Log.d(TAG, "resource not loaded: " + resource_id);
            } else {
                int sound_id = sound_ids.get(resource_id);
                if (MyDebug.LOG)
                    Log.d(TAG, "play sound: " + sound_id);
                sound_pool.play(sound_id, 1.0f, 1.0f, 0, 0, 1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    void speak(String text) {
        if (textToSpeech != null && textToSpeechSuccess) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // for testing:
    public ArrayList<String> getSaveLocationHistory() {
        return this.save_location_history;
    }

    public void usedFolderPicker() {
        updateFolderHistory();
    }

    public boolean hasThumbnailAnimation() {
        return this.applicationInterface.hasThumbnailAnimation();
    }

    private void makeMovie() {
        try {
            ArrayList<String> videos = applicationInterface.getSessionCaptureHistory();

            if (videos.size() > 0) {
                File fileMerge = applicationInterface.createOutputVideoFile();

                new MergeVideosTask(this, fileMerge, videos, mLastAudioPath).execute();
            } else {
                Toast.makeText(this, "Please record some videos first, then make a movie!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private void recordAudio() {

        try {
            File fileAudio = new File("/sdcard/storycam/audio" + new java.util.Date().getTime() + ".aac");
            fileAudio.getParentFile().mkdirs();
            mLastAudioPath = fileAudio.getAbsolutePath();
            AudioRecorder ar = new AudioRecorder();
            ar.showAudioRecording(this, fileAudio);
        } catch (Exception ioe) {
            ioe.printStackTrace();
            mLastAudioPath = null;
        }
    }

    private void selectAudio() {

        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, ACTIVITY_CHOOSE_SOUND);
    }

    class ImageAdapter extends PagerAdapter {
        Context context;

        ImageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return overlays.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ImageView) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            ViewPager viewPager = (ViewPager) findViewById(R.id.overlayPager);

            View preview = findViewById(R.id.preview);

            Log.d("ViewPager", "preview size: " + preview.getWidth() + "x" + preview.getHeight());

            if (preview.getWidth() > 0 && preview.getHeight() > 0) {

                int oWidth = preview.getWidth();
                int oHeight = preview.getHeight();

                //viewPager.setMinimumHeight(oHeight);
                //viewPager.setMinimumWidth(oWidth);
                // imageView.setMinimumHeight(oHeight);
                //imageView.setMinimumWidth(oWidth);

                Bitmap bitmap = Bitmap.createBitmap(preview.getWidth(), preview.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                PorterDuffColorFilter greyFilter = new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

                try {
                    SVG svg = new SVGBuilder()
                            .readFromAsset(getAssets(), overlays[position])           // if svg in assets

                            //   .setFillColorFilter(ColorFilter.)
                            // .setWhiteMode(true) // draw fills in white, doesn't draw strokes
                            //  .setColorSwap(0xFFFFFF, 0x55AAAAAA) // swap a single colour
                            // .setColorFilter(filter) // run through a colour filter
                            // .set[Stroke|Fill]ColorFilter(filter) // apply a colour filter to only the stroke or fill
                            .build();

                    Rect rBounds = new Rect(0, 0, oWidth, oHeight);
                    Picture p = svg.getPicture();
                    canvas.drawPicture(p, rBounds);

                    imageView.setImageBitmap(bitmap);
                    imageView.setAlpha(0.65f);

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            ((ViewPager) container).addView(imageView, 0);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((ImageView) object);
        }
    }

	/*private void checkForCrashes() {
		CrashManager.register(this, APP_ID);
	}

	private void checkForUpdates() {
		// Remove this for store builds!
		UpdateManager.register(this, APP_ID);
	}

    private void unregisterManagers() {
        UpdateManager.unregister();
        // unregister other managers if necessary...
    }*/

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (MyDebug.LOG)
                    Log.d(TAG, "from " + e1.getX() + " , " + e1.getY() + " to " + e2.getX() + " , " + e2.getY());
                final ViewConfiguration vc = ViewConfiguration.get(MainActivity.this);
                //final int swipeMinDistance = 4*vc.getScaledPagingTouchSlop();
                final float scale = getResources().getDisplayMetrics().density;
                final int swipeMinDistance = (int) (160 * scale + 0.5f); // convert dps to pixels
                final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
                if (MyDebug.LOG) {
                    Log.d(TAG, "from " + e1.getX() + " , " + e1.getY() + " to " + e2.getX() + " , " + e2.getY());
                    Log.d(TAG, "swipeMinDistance: " + swipeMinDistance);
                }
                float xdist = e1.getX() - e2.getX();
                float ydist = e1.getY() - e2.getY();
                float dist2 = xdist * xdist + ydist * ydist;
                float vel2 = velocityX * velocityX + velocityY * velocityY;
                if (dist2 > swipeMinDistance * swipeMinDistance && vel2 > swipeThresholdVelocity * swipeThresholdVelocity) {
                    preview.showToast(screen_locked_toast, R.string.unlocked);
                    unlockScreen();
                }
            } catch (Exception e) {
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            preview.showToast(screen_locked_toast, R.string.screen_is_locked);
            return true;
        }
    }
}