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
import com.google.firebase.analytics.FirebaseAnalytics;


public class LauncherActivity
        extends com.google.androidbrowserhelper.trusted.LauncherActivity {

    private String mAppInstanceId;



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

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.logEvent("boot", new Bundle());
        // Start the asynchronous task to get the Firebase application instance id.
        firebaseAnalytics.getAppInstanceId().addOnCompleteListener(task -> {
            // Once the task is complete, save the instance id so it can be used by
            // getLaunchingUrl().
            mAppInstanceId = task.getResult();
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
        Uri uri = super.getLaunchingUrl();

        return uri.buildUpon()
                .appendQueryParameter("aiid", mAppInstanceId)
                .build();
    }
}
