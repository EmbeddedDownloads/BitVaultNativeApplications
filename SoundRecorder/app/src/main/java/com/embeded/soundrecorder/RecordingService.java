package com.embeded.soundrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.embeded.soundrecorder.activities.MainActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private static int count = 0;
    private String mFileName = null;
    private String mFilePath = null;
    private MediaRecorder mRecorder = null;
    private DBHelper mDatabase;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private SharedPreferences prefs;
    private int mElapsedSeconds = 0;
    private OnTimerChangedListener onTimerChangedListener = null;
    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void startRecording() {
        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(192000);
        }

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void setSharePref() {
       prefs = getSharedPreferences("SoundRecorder", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getInt("id", 1)>=1) {
            editor.putInt("id", (prefs.getInt("id", 1)) + 1);
        }
        editor.commit();

    }

    public void setFileNameAndPath() {
        File f;
        prefs = getSharedPreferences("SoundRecorder", MODE_PRIVATE);
        count = prefs.getInt("id", 1);
        // do {
        mFileName = getString(R.string.default_file_name) + "_" + (count) + ".mp3";
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + mFileName;
        f = new File(mFilePath);
        // } while (f.exists() && !f.isDirectory());

    }

    public void stopRecording() {
        try {
            mRecorder.stop();
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
            mRecorder.release();
            setSharePref();
            final Toast toast = Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 300);
            toast.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 500);

            //remove notification
            if (mIncrementTimerTask != null) {
                mIncrementTimerTask.cancel();
                mIncrementTimerTask = null;
            }

            mRecorder = null;
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(f));
            sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception", e);
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    //TODO:
    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));

        return mBuilder.build();
    }

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }
}