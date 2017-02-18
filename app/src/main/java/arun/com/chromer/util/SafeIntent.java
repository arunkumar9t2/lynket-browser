package arun.com.chromer.util;

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * External applications can pass values into Intents that can cause us to crash: in defense,
 * we wrap {@link Intent} and catch the exceptions they may force us to throw.
 * <p>
 * Source taken from Firefox.
 * https://hg.mozilla.org/releases/mozilla-aurora/file/46c6e8bb7f6f/mobile/android/base/java/org/mozilla/gecko/mozglue/SafeIntent.java
 */
public class SafeIntent {
    private final Intent intent;

    public SafeIntent(final Intent intent) {
        this.intent = intent;
    }

    public boolean hasExtra(String name) {
        try {
            return intent.hasExtra(name);
        } catch (OutOfMemoryError e) {
            Timber.w("Couldn't determine if intent had an extra: OOM. Malformed?");
            return false;
        } catch (RuntimeException e) {
            Timber.w(e, "Couldn't determine if intent had an extra. ");
            return false;
        }
    }

    public boolean getBooleanExtra(final String name, final boolean defaultValue) {
        try {
            return intent.getBooleanExtra(name, defaultValue);
        } catch (OutOfMemoryError e) {
            Timber.w("Couldn't get intent extras: OOM. Malformed?");
            return defaultValue;
        } catch (RuntimeException e) {
            Timber.w(e, "Couldn't get intent extras.");
            return defaultValue;
        }
    }

    public String getStringExtra(final String name) {
        try {
            return intent.getStringExtra(name);
        } catch (OutOfMemoryError e) {
            Timber.w("Couldn't get intent extras: OOM. Malformed?");
            return null;
        } catch (RuntimeException e) {
            Timber.w(e, "Couldn't get intent extras.");
            return null;
        }
    }

    public Bundle getBundleExtra(final String name) {
        try {
            return intent.getBundleExtra(name);
        } catch (OutOfMemoryError e) {
            Timber.w("Couldn't get intent extras: OOM. Malformed?");
            return null;
        } catch (RuntimeException e) {
            Timber.w(e, "Couldn't get intent extras.");
            return null;
        }
    }

    public String getAction() {
        return intent.getAction();
    }

    public String getDataString() {
        try {
            return intent.getDataString();
        } catch (OutOfMemoryError e) {
            Timber.w("Couldn't get intent data string: OOM. Malformed?");
            return null;
        } catch (RuntimeException e) {
            Timber.w(e, "Couldn't get intent data string.");
            return null;
        }
    }

    public ArrayList<String> getStringArrayListExtra(final String name) {
        try {
            return intent.getStringArrayListExtra(name);
        } catch (OutOfMemoryError e) {
            Timber.w("Couldn't get intent data string: OOM. Malformed?");
            return null;
        } catch (RuntimeException e) {
            Timber.w(e, "Couldn't get intent data string.");
            return null;
        }
    }

    public Uri getData() {
        try {
            return intent.getData();
        } catch (OutOfMemoryError e) {
            Timber.w("Couldn't get intent data: OOM. Malformed?");
            return null;
        } catch (RuntimeException e) {
            Timber.w(e, "Couldn't get intent data.");
            return null;
        }
    }

    public Intent getUnsafe() {
        return intent;
    }
}