package com.example.androidwebviewtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    WebView browser;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //WebView Object

        browser=(WebView)findViewById(R.id.webkit);
        WebSettings websettings = browser.getSettings();
        websettings.setDomStorageEnabled(true);  // Open DOM storage function
        websettings.setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        websettings.setAppCachePath(appCachePath);
        websettings.setAllowFileAccess(true);    // Readable file cache
        websettings.setAppCacheEnabled(true);    //Turn on the H5(APPCache) caching function
        websettings.setJavaScriptEnabled(true);

        //alert가 작동하려면 아래 WebChromeClient 지정해야 함
        browser.setWebChromeClient(new WebChromeClient());
        browser.addJavascriptInterface(new AndroidBridge(), "android");

        browser.loadUrl("file:///android_asset/www/test.html");

    }



    /**
     @brief 자바스크립트 인터페이스 클래스
     **/
    private class AndroidBridge {
        private final Handler jsHandler = new Handler();

        /**
     * 20.07.02
     * 박정규
     @brief 앱 버전코드 가져오기
     **/
    @JavascriptInterface
    public void getAppVersionCode() {
        jsHandler.post(new Runnable() {
            @Override
            public void run() {
                PackageInfo pInfo = null;
                try {
                    pInfo = MainActivity.this.getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                int versionCode;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    versionCode = (int) pInfo.getLongVersionCode(); // avoid huge version numbers and you will be ok
                } else {
                    //noinspection deprecation
                    versionCode = pInfo.versionCode;
                }

                final String versionCodeString = String.valueOf(versionCode);
                browser.loadUrl("javascript:onGetAppVersionCode('" + versionCodeString + "')");
            }
        });
    }
}


}
