package com.example.androidwebviewtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
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

        //버그인가? write external storage 권한이 있는데 외부저장소에 파일을 쓰면 강종됨
        //무조건 한번 권한요청을 해야 하고 그러면 다음 실행할 때에는 무시됨
        handleExternalStoragePermission();


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
        browser.setWebViewClient(new WebViewClient());
        browser.setWebChromeClient(new WebChromeClient());
        browser.addJavascriptInterface(new AndroidBridge(), "android");
        browser.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimeType,
                                        long contentLength) {

                startDownloadManager(url, userAgent,
                           contentDisposition, mimeType,
                           contentLength);


              //  handleExternalStoragePermission();
//                if (hasExternalStoragePermission()) {
//                    startDownloadManager(url, userAgent,
//                            contentDisposition, mimeType,
//                            contentLength);
//                } else {
//                    handleExternalStoragePermission();
//                }
            }
        });
        browser.loadUrl("https://torrentview15.net");

    }

    @Override
    public void onBackPressed() {
        if (browser.canGoBack()) {
            browser.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void startDownloadManager(String url, String userAgent,
                                      String contentDisposition, String mimeType,
                                      long contentLength) {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(url));


        request.setMimeType(mimeType);


        String cookies = CookieManager.getInstance().getCookie(url);


        request.addRequestHeader("cookie", cookies);


        request.addRequestHeader("User-Agent", userAgent);


        request.setDescription("Downloading file...");


        request.setTitle(URLUtil.guessFileName(url, contentDisposition,
                mimeType));


        request.allowScanningByMediaScanner();


        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                        url, contentDisposition, mimeType));
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);
        Toast.makeText(getApplicationContext(), "Downloading File",
                Toast.LENGTH_LONG).show();
    }

    private void handleExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )) {
            showPermissionRationalePopup();
        } else {
            requestWriteExternalStoragePermission();
        }


    }

    private boolean hasExternalStoragePermission() {
        return ActivityCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
    }



    private static final int REQUEST_CODE_REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 4359;

    private void requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                REQUEST_CODE_REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION
        );
    }

    private void showPermissionRationalePopup() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                MainActivity.this);
        alertDialogBuilder
                .setMessage("저장용량에 액세스하도록 허용하시겠습니까?")
                .setCancelable(false)
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        requestWriteExternalStoragePermission();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
