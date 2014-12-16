package com.google.devrel.training.conference.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import com.appspot.sachi_test_1224.conference.Conference;

import javax.annotation.Nullable;

/**
 * Created by sachi on 12/3/2014.
 */
public class AppConstants {

        public static final String WEB_CLIENT_ID = "884445916662-84gmtobm7l0qsuolt8hikkkjt54rvfk0.apps.googleusercontent.com";

        public static final String AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;

        public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

        public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

        public static int countGoogleAccounts(Context context) {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            if (accounts == null || accounts.length < 1) {
                return 0;
            } else {
                return accounts.length;
            }
        }

        public static Conference getApiServiceHandle(@Nullable GoogleAccountCredential credential) {
            // Use a builder to help formulate the API request.
            Conference.Builder conf = new Conference.Builder(AppConstants.HTTP_TRANSPORT,
                    AppConstants.JSON_FACTORY,credential);
            return conf.build();
        }

        public static boolean checkGooglePlayServicesAvailable(Activity activity) {
            final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
            if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
                showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode);
                return false;
            }
            return true;
        }

        public static void showGooglePlayServicesAvailabilityErrorDialog(final Activity activity,
                                                                         final int connectionStatusCode) {
            final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionStatusCode, activity, REQUEST_GOOGLE_PLAY_SERVICES);
                    dialog.show();
                }
            });
        }


}
