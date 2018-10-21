package com.embedded.contacts;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by Dheeraj Bansal on 30/8/17.
 * Application class
 */
@ReportsCrashes(
        mailTo = "dheeraj.bansal@vvdntech.in , divyanshi.parashar@vvdntech.in",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.app_name)
public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
