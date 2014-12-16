package com.google.devrel.training.conference.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.common.base.Strings;

import com.appspot.sachi_test_1224.conference.model.Conference;
import com.appspot.sachi_test_1224.conference.model.ConferenceCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


public class MainActivity extends Activity {

    private static final String TAG = "ConfClientApp";
    private static final String LOG_TAG = "MainActivity";
    private static final int ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION = 2222;

    private ConfDataAdapter mListAdapter;
    private AuthorizationCheckTask mAuthTask;

    private String mEmailAccount = "";
    private SharedPreferences settings;
    private String accountName;

    static final String PREF_ACCOUNT_NAME = "accountName";
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final String PREF_AUTH_TOKEN = "authToken";

    private GoogleAccountCredential mGoogleAccountCredential;
    private com.appspot.sachi_test_1224.conference.Conference mService;

    boolean signedIn = false;
    boolean waitingForMove = false;

    /**
     * Handles logic for clicking the sign in button.
     *
     * @param v current view within the application, for rendering updates
     */
    public void signIn(View v) {
        if (!this.signedIn) {
            chooseAccount();
        } else {
            forgetAccount();
        }
    }


    private void chooseAccount() {
        startActivityForResult(mGoogleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private void setAccountName(String accountName) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
        mGoogleAccountCredential.setSelectedAccountName(accountName);
        this.accountName = accountName;
    }

    private void onSignIn() {
        this.signedIn = true;
    }

    private void forgetAccount() {
        this.signedIn = false;
        SharedPreferences.Editor editor2 = settings.edit();
        editor2.remove(PREF_AUTH_TOKEN);
        editor2.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(TAG, 0);
        mGoogleAccountCredential = GoogleAccountCredential.usingAudience(this,AppConstants.AUDIENCE);
        setAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = AppConstants.getApiServiceHandle(mGoogleAccountCredential);

        if (mGoogleAccountCredential.getSelectedAccountName() != null) {
            onSignIn();
        } else {
            signIn(findViewById(R.id.greetings_list_view));
        }

        Log.d(LOG_TAG,"In OnCreate");

        // Prevent the keyboard from being visible upon startup.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ListView listView = (ListView) findViewById(R.id.greetings_list_view);
        mListAdapter = new ConfDataAdapter((Application) getApplication());
        listView.setAdapter(mListAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthTask!=null) {
            mAuthTask.cancel(true);
            mAuthTask = null;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is invoked when the "Get Greeting" button is clicked. See activity_main.xml for
     * the dynamic reference to this method.
     */
    public void onClickGetGreeting(View view) {
        View rootView = view.getRootView();

        // @see http://goo.gl/fN1fuE @26:00 for a great explanation.
        new ConferenceAsyncTask(this).execute();
    }
/*
    public void onClickSignIn(View view) {
        TextView emailAddressTV = (TextView) view.getRootView().findViewById(R.id.email_address_tv);
        // Check to see how many Google accounts are registered with the device.
        int googleAccounts = AppConstants.countGoogleAccounts(this);
        if (googleAccounts == 0) {
            // No accounts registered, nothing to do.
            Toast.makeText(this, R.string.toast_no_google_accounts_registered,
                    Toast.LENGTH_LONG).show();
        } else if (googleAccounts == 1) {
            // If only one account then select it.
            Toast.makeText(this, R.string.toast_only_one_google_account_registered,
                    Toast.LENGTH_LONG).show();
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            if (accounts != null && accounts.length > 0) {
                // Select account and perform authorization check.
                emailAddressTV.setText(accounts[0].name);
                mEmailAccount = accounts[0].name;
                performAuthCheck(accounts[0].name);
            }
        } else {
            // More than one Google Account is present, a chooser is necessary.

            // Reset selected account.
            emailAddressTV.setText("");

            // Invoke an {@code Intent} to allow the user to select a Google account.
            Intent accountSelector = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false,
                    "Select the account to access the HelloEndpoints API.", null, null, null);
            startActivityForResult(accountSelector,
                    ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION);
        }

    }
*/
    private class ConferenceAsyncTask extends AsyncTask<Void, Void, ConferenceCollection> {
        private ProgressDialog pd;
        Context context;

        public ConferenceAsyncTask(Context context) {
            this.context = context;
        }

        protected void onPreExecute(){
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Retrieving Conferences...");
            pd.show();
        }

        private boolean isSignedIn() {
            if (!Strings.isNullOrEmpty(mEmailAccount)) {
                return true;
            } else {
                return false;
            }
        }

        private void setAccountName(String accountName) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, accountName);
            editor.commit();
            mGoogleAccountCredential.setSelectedAccountName(accountName);
            mEmailAccount = accountName;
        }

        @Override
        protected ConferenceCollection doInBackground(Void... unused) {

            settings = getSharedPreferences(TAG,MODE_PRIVATE);
            setAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

            try {
                ConferenceCollection confs = mService.getConferencesCreated().execute();
                return confs;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Exception during API call", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ConferenceCollection confs) {
            pd.dismiss();
            if (confs!=null) {
                List<Conference> confList = confs.getItems();
                ArrayList<String> descs = new ArrayList<String>();
                for (Conference conf : confList) {
                    descs.add(conf.getName());
                }
                //((Application)(getApplication())).confs = descs;
                mListAdapter.replaceData((String[])(descs.toArray(new String[descs.size()])));
            } else {
                Log.e(LOG_TAG, "No conferences were returned by the API.");
            }
        }
    }
    /**
     * Simple use of an ArrayAdapter but we're using a static class to ensure no references to the
     * Activity exists.
     */
    static class ConfDataAdapter extends ArrayAdapter {
        ConfDataAdapter(Application application) {
            super(application.getApplicationContext(), R.layout.list_black_text,R.id.list_content, application.confs);
        }

        void replaceData(String[] confDescription) {
            clear();
            for (String name : confDescription) {
                add(name);
            }
        }
   }


    public void performAuthCheck(String emailAccount) {
        // Cancel previously running tasks.
        if (mAuthTask != null) {
            try {
                mAuthTask.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new AuthorizationCheckTask().execute(emailAccount);
    }

    class AuthorizationCheckTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... emailAccounts) {
            Log.i(LOG_TAG, "Background task started.");

            if (!AppConstants.checkGooglePlayServicesAvailable(MainActivity.this)) {
                return false;
            }

            String emailAccount = emailAccounts[0];
            // Ensure only one task is running at a time.
            mAuthTask = this;

            // Ensure an email was selected.
            if (Strings.isNullOrEmpty(emailAccount)) {
                publishProgress(R.string.toast_no_google_account_selected);
                // Failure.
                return false;
            }

            Log.d(LOG_TAG, "Attempting to get AuthToken for account: " + mEmailAccount);

            try {
                // If the application has the appropriate access then a token will be retrieved, otherwise
                // an error will be thrown.
                mGoogleAccountCredential = GoogleAccountCredential.usingAudience(MainActivity.this, AppConstants.AUDIENCE);
                mGoogleAccountCredential.setSelectedAccountName(emailAccount);

                //String accessToken = GoogleAuthUtil.getToken(MainActivity.this,emailAccount, AppConstants.AUDIENCE);
                String accessToken = mGoogleAccountCredential.getToken();

                Log.d(LOG_TAG, "AccessToken retrieved");

                // Success.
                return true;
            } catch (GoogleAuthException unrecoverableException) {
                Log.e(LOG_TAG, "Exception checking OAuth2 authentication.", unrecoverableException);
                publishProgress(R.string.toast_exception_checking_authorization);
                // Failure.
                return false;
            } catch (IOException ioException) {
                Log.e(LOG_TAG, "Exception checking OAuth2 authentication.", ioException);
                publishProgress(R.string.toast_exception_checking_authorization);
                // Failure or cancel request.
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... stringIds) {
            // Toast only the most recent.
            Integer stringId = stringIds[0];
            Toast.makeText(MainActivity.this, stringId, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            mAuthTask = this;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //TextView emailAddressTV = (TextView) MainActivity.this.findViewById(R.id.email_address_tv);
            if (success) {
                // Authorization check successful, set internal variable.
              //  mEmailAccount = emailAddressTV.getText().toString();
            } else {
                // Authorization check unsuccessful, reset TextView to empty.
                //emailAddressTV.setText("");
            }
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION && resultCode == RESULT_OK) {
            // This path indicates the account selection activity resulted in the user selecting a
            // Google account and clicking OK.

            // Set the selected account.
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            TextView emailAccountTextView = (TextView)this.findViewById(R.id.email_address_tv);
            emailAccountTextView.setText(accountName);

            // Fire off the authorization check for this account and OAuth2 scopes.
            performAuthCheck(accountName);
        }
    }
*/

    @Override
    protected void onResume() {
        super.onResume();
        checkGooglePlayServicesAvailable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        onSignIn();
                    }
                }
                break;
        }
    }


    /**
     * Check that Google Play services APK is installed and up to date.
     */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    /**
     * Called if the device does not have Google Play Services installed.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

}
