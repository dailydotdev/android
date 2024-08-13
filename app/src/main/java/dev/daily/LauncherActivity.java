/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.daily;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.util.ArrayList;


public class LauncherActivity
        extends com.google.androidbrowserhelper.trusted.LauncherActivity {

    private ArrayList<String[]> mQueryParams = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // `super.onCreate()` may have called `finish()`. In this case, we don't do any work.
        if (isFinishing()) {
            return;
        }

        // Setting an orientation crashes the app due to the transparent background on Android 8.0
        // Oreo and below. We only set the orientation on Oreo and above. This only affects the
        // splash screen and Chrome will still respect the orientation.
        // See https://github.com/GoogleChromeLabs/bubblewrap/issues/496 for details.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        String fbAnonymousId = AppEventsLogger.getAnonymousAppDeviceGUID(getApplicationContext());
        mQueryParams.add(new String[]{"fb_anon_id", fbAnonymousId});

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.logEvent("boot", new Bundle());

        firebaseAnalytics.getAppInstanceId().addOnCompleteListener(task -> {
            mQueryParams.add(new String[]{"aiid", task.getResult()});
            AppLinkData appLinkData = AppLinkData.createFromActivity(this);
            if (appLinkData != null) {
                Uri targetUri = appLinkData.getTargetUri();
                if (targetUri != null) {
                    String fbclid = targetUri.getQueryParameter("fbclid");
                    if (fbclid != null) {
                        mQueryParams.add(new String[]{"fbclid", fbclid});
                    }
                }
            }
            launchTwa();
        });
    }

    @Override
    protected boolean shouldLaunchImmediately() {
        // launchImmediately() returns `false` so we can wait until Firebase Analytics is ready
        // and then launch the Trusted Web Activity with `launch()`.
        return false;
    }

    @Override
    protected Uri getLaunchingUrl() {
        // Get the original launch Url.
        Uri baseUri = super.getLaunchingUrl();

        Uri.Builder uriBuilder = baseUri.buildUpon();
        for (String[] queryParam : mQueryParams) {
            uriBuilder.appendQueryParameter(queryParam[0], queryParam[1]);
        }

        return uriBuilder.build();
    }
}
