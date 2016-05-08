package com.honghaisen.mystudyapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "bHp2pwLo5rYOCm3lKfClHoNmK";
    private static final String TWITTER_SECRET = "qMSVEAhnAoh5WJg85r6kgkpobMWXo2c2U5CevUngKNk7tD6l1G";



    private Button register;
    private Button login;
    private EditText email;
    private EditText password;
    private DBHelper db;
    private LoginButton fbBtn;
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;
    private SharedPreferences sharedPreferences;
    private TwitterLoginButton twitterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk(), new Twitter(authConfig));

        //FaceBook sdk initialization
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        setContentView(R.layout.activity_main);

        //instantiate widgets
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        twitterBtn = (TwitterLoginButton) findViewById(R.id.twitterBtn);
        sharedPreferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        db = new DBHelper(this);
        fbBtn = (LoginButton) findViewById(R.id.fbBtn);
        callbackManager = CallbackManager.Factory.create();

        fbBtn.setReadPermissions("public_profile");
        fbBtn.setReadPermissions("email");

        accessToken = AccessToken.getCurrentAccessToken();

        //start accessToken Tracker
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };
        accessTokenTracker.startTracking();

        //start profile token tracker
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
//                Intent i = new Intent(MainActivity.this, Second.class).putExtra("fb", true);
//                if(currentProfile != null) {
//                    i.putExtra(Values.USER_COLUMN_NAME, currentProfile.getName()).putExtra(Values.USER_COLUMN_EMAIL, currentProfile.);
//                    MainActivity.this.startActivity(i);
//                }

            }
        };
        profileTracker.startTracking();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();
        //clicker for login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (accessToken != null) {
                    Toast.makeText(MainActivity.this, "Login already", Toast.LENGTH_SHORT).show();
                    return;
                }
                String email = MainActivity.this.email.getText().toString();
                String password = MainActivity.this.password.getText().toString();
                if (sharedPreferences.getString(email, null) != null && sharedPreferences.getString(email, null).equals(password)) {
                    Intent i = new Intent(MainActivity.this, Second.class).putExtra("email", email).putExtra("fb", false);
                    MainActivity.this.startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Email or password not match", Toast.LENGTH_SHORT).show();
                }
//                if (db.matches(email, password)) {
//                    Cursor temp = db.getData(email);
//                    temp.moveToFirst();
//
//                    String name = temp.getString(temp.getColumnIndex(Values.USER_COLUMN_NAME));
//                    Intent i = new Intent(MainActivity.this, Second.class).putExtra(Values.USER_COLUMN_NAME, name).putExtra("email", email).putExtra("fb", false);
//                    MainActivity.this.startActivity(i);
//                } else {
//                    Toast.makeText(MainActivity.this, "Email or password not match", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        //clicker for registration
        register.setOnClickListener(new View.OnClickListener() {
            //move to register activity
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Register.class);
                MainActivity.this.startActivity(i);
            }
        });

        //clicker for twitter login
        twitterBtn.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = Twitter.getSessionManager().getActiveSession();
                String name = session.getUserName();
                String email = null;
                TwitterAuthClient authClient = new TwitterAuthClient();
                authClient.requestEmail(session, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        Log.d("twitter", "email");
                        Log.d("twitter", result.data);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("twitter", "deny");
                    }
                });
                Intent i = new Intent(MainActivity.this, Second.class);
                i.putExtra("fb", false);
                i.putExtra(Values.USER_COLUMN_NAME, name);
                //must be modified after get permission
                i.putExtra(Values.USER_COLUMN_EMAIL, "hison7463@gmail.com");
                MainActivity.this.startActivity(i);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("twitter", "failure");
            }
        });

        //clicker for FB login
        fbBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Intent i = new Intent(MainActivity.this, Second.class);
                        Profile profile = Profile.getCurrentProfile();
                        try {
                            i.putExtra(Values.USER_COLUMN_NAME, profile.getName()).putExtra("fb", true).putExtra(Values.USER_COLUMN_EMAIL, object.getString(Values.USER_COLUMN_EMAIL));
                        } catch (Exception e) {
                            Log.e("JsonException", e.toString());
                        }
                        MainActivity.this.startActivity(i);
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i("fbBtn", "cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("fbBtn", "error");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterBtn.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

}
