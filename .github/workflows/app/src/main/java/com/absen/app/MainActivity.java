package com.absen.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    // GANTI dengan alamat sistem absensi Anda (harus https)
    private static final String URL_APLIKASI = "https://GANTI-ALAMAT-ANDA";

    private WebView web;
    private PermissionRequest pending;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        final String id = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
        s.setUserAgentString(s.getUserAgentString() + " AbsenApp/1.0 AID/" + id);

        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public String getDeviceId() { return id; }
        }, "AndroidBridge");

        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest r) {
                runOnUiThread(() -> {
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        r.grant(r.getResources());
                    } else {
                        pending = r;
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                    }
                });
            }
        });

        web.loadUrl(URL_APLIKASI);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] p, int[] res) {
        super.onRequestPermissionsResult(code, p, res);
        if (pending != null) {
            if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
                pending.grant(pending.getResources());
            } else {
                pending.deny();
            }
            pending = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
