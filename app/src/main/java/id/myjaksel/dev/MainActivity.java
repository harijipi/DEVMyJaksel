package id.myjaksel.dev;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.net.Uri;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

// MyFirebaseMessagingService.java
import android.content.Intent;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;







@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    WebView myWebView;

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

 //   public String url ="https://uemkaem.id/marketpreneur/ekosistem/gerai70";
//public String url ="https://uemkaem.id/loginapp/gerai70";
 //public String url ="https:/uemkaem.id/loginapp/myjaksel";
 public String url ="https:/develop.uemkaem.com/loginapp/myjaksel";



    public String username = "h";
    public String password = "jpi";

    private static final int NOTIFICATION_PERMISSION_CODE = 123;



    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android 8.0 and above, open the App Notification settings
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivityForResult(intent, NOTIFICATION_PERMISSION_CODE);
        } else {
            // For earlier versions, open the App Info settings
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, NOTIFICATION_PERMISSION_CODE);
        }
    }

    private boolean areNotificationsEnabled() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        showEnableNotificationsPopup();




// Firebase Code
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            System.out.println("Fetching FCM registration token failed");
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        //Log.d(TAG, msg);
                        System.out.println(token);
                        Toast.makeText(MainActivity.this, "Your registration token is"+ token, Toast.LENGTH_SHORT).show();
                       // etToken.setText(token);

                        Log.d("TOKEN", token);

                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("tokenPushNotif", token);
                        editor.apply();



                    }

                });

// End Firebase code
        Objects.requireNonNull(getSupportActionBar()).hide();
        myWebView = findViewById(R.id.iniwebview);


        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setSavePassword(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setLoadWithOverviewMode(false);
        myWebView.getSettings().setDatabaseEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setAllowContentAccess(true);
        myWebView.getSettings().setAllowFileAccess(true);


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");
        password = sharedPref.getString("password", "");

        Log.d("password awal", password);

        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        myWebView.loadUrl(url);
        String tokenNotif = sharedPref.getString("tokenPushNotif", "defaultvalue");
        myWebView.loadUrl("javascript:localStorage.setItem('tokenPushNotif','"+tokenNotif+ "');");

//        myWebView.setInitialScale(50);
        myWebView.setInitialScale(0);
        myWebView.setWebViewClient(new MyWebViewClient());

        myWebView.setWebChromeClient(new WebChromeClient()
        {


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }

                uploadMessage = filePathCallback;

                Intent intent;
                intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }

        });
        if (!areNotificationsEnabled()) {
            // If notifications are not enabled, request permission
            requestNotificationPermission();
        }
//        String htmlContent = "<html><body><form><input type='text' id='username'><input type='password' id='password'><button type='button' onclick='saveCredentials()'>Save Credentials</button></form></body></html>";
//        myWebView.loadData(htmlContent, "text/html", null);



    }






    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            //Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String value = sharedPref.getString("username", "defaultvalue");

            Log.d("nameuser", username);
           // Toast.makeText(mContext, username+password, Toast.LENGTH_SHORT).show();



        }


        /** reset sharepreferences */
        @JavascriptInterface
        public void logoutApp(String toast){
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("sudahlogin", "defaultvalue");
            editor.apply();
            //Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }


        @JavascriptInterface
        public void saveLoginCredentials(String username, String password) {
            // Save the login credentials using SharedPreferences
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

            String valuesudahlogin = sharedPref.getString("sudahlogin", "defaultvalue");
            Log.d("sudahlogin",valuesudahlogin);
            if( "defaultvalue".equals(valuesudahlogin)) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.putString("sudahlogin", "ya");
                editor.apply();
            }
            //Log.d("simpanpassword", password);




        }







    }



    @SuppressLint({"MissingSuperCall", "ObsoleteSdkInt"})
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }


    }





    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
 //           if ("uemkaem.id".equals(request.getUrl().getHost())) {
               if ("develop.uemkaem.com".equals(request.getUrl().getHost())) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }

            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
            startActivity(intent);

            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //view.loadUrl("javascript:document.getElementById('username').value='" + username + "';" +
            //        "document.getElementById('password').value='" + password + "';" +
            //        "document.forms['login'].submit();");
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String valueUserName = sharedPref.getString("username", "defaultvalue");
            String valuePassword = sharedPref.getString("password", "defaultvalue");
            String valuesudahlogin = sharedPref.getString("sudahlogin", "defaultvaluehhhhh");

            Log.d("nameuser", valueUserName);
            Log.d("katasandi",valuePassword);
            Log.d("sudahlogin",valuesudahlogin);
            Log.i("uhui","uhui");
            String tokenNotif = sharedPref.getString("tokenPushNotif", "defaultvalue");
//            view.loadUrl("javascript:alert('"+tokenNotif+"');");

//            view.loadUrl("javascript:localStorage.setItem('tokenPushNotif','"+tokenNotif+ "');"+
//                            "alert('nokennya = '+ localStorage.getItem('tokenPushNotif'));"
//            );

            view.loadUrl("javascript:localStorage.setItem('tokenPushNotif','"+tokenNotif+ "');"
            );

//            view.loadUrl("javascript:alert('nokennya = '+ localStorage.getItem('tokenPushNotif'));"
//            );


            if( "ya".equals(valuesudahlogin)) {
                Log.d("PAK EKO","MASUKKK");
                tokenNotif = sharedPref.getString("tokenPushNotif", "defaultvalue");
                view.loadUrl("javascript:localStorage.setItem('tokenPushNotif','"+tokenNotif+ "');");
                view.loadUrl("javascript:document.getElementById('email').value='" + valueUserName + "';" +
                        "document.getElementById('password').value='" + valuePassword + "';" +
                        "document.forms['submitLoginApp'].submit();");
//                Toast.makeText(MainActivity.this, "Your registration token is"+ tokenNotif, Toast.LENGTH_SHORT).show();
                //tokenNotif = sharedPref.getString("tokenPushNotif", "defaultvalue");
//                view.loadUrl("javascript:alert('"+tokenNotif+"');");
//                view.loadUrl("javascript:alert('nokennya = '+ localStorage.getItem('tokenPushNotif'));"
//                );

            }


        }


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }


}

