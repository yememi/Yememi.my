package com.example.yememi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements OnBackPressedDispatcherOwner {

    private ProgressBar progressBar;
    private ValueCallback<Uri[]> mUploadMessage;
    private WebView myWeb; // Declare WebView as a class-level variable

    @SuppressLint({"SetJavaScriptEnabled", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWeb = findViewById(R.id.myWeb); // Initialize WebView
        progressBar = findViewById(R.id.progressbar);

        // Set the status bar color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.your_status_bar_color));

        // Remove flags that make the app full screen and hide the navigation bar
        getWindow().clearFlags(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Set progress bar loading color programmatically
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.your_loading_color), android.graphics.PorterDuff.Mode.MULTIPLY);

        WebSettings webSettings = myWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true); // Enable file access
        webSettings.setDomStorageEnabled(true); // Enable DOM storage

        myWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        myWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                chooseFile.launch(intent);
                return true;
            }
        });

        myWeb.loadUrl("https://yememi.my");

        // Handle back button press using OnBackPressedCallback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (myWeb.canGoBack()) {
                    myWeb.goBack();
                } else {
                    // If there's no history, proceed with default back button behavior
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // For Android 4.1 - 4.4
    private final ActivityResultLauncher<Intent> chooseFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri resultUri = result.getData() == null ? null : result.getData().getData();
                    if (mUploadMessage != null) {
                        mUploadMessage.onReceiveValue(new Uri[]{resultUri});
                        mUploadMessage = null;
                    }
                }
            });
}
