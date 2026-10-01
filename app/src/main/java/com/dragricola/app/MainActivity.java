package com.dragricola.app;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {

    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Solicita câmera e microfone
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(
                new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                },
                100
            );
        }

        // Cria o WebView
        webView = new WebView(this);
        setContentView(webView);

        // Configura o WebView
        WebSettings s = webView.getSettings();

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);

        // Navegação dentro do aplicativo
        webView.setWebViewClient(new WebViewClient());

        // Câmera, microfone e seleção de arquivos
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                fileCallback = callback;

                Intent intent = params.createIntent();

                try {
                    startActivityForResult(intent, 200);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }

                return true;
            }
        });

        // Abre o index.html
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        if (requestCode == 200 && fileCallback != null) {

            Uri[] result = null;

            if (resultCode == RESULT_OK && data != null) {

                if (data.getData() != null) {
                    result = new Uri[]{
                        data.getData()
                    };
                }
            }

            fileCallback.onReceiveValue(result);
            fileCallback =
