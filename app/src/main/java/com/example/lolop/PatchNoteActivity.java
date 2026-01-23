package com.example.lolop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import com.example.lolop.databinding.ActivityPatchNoteBinding;

public class PatchNoteActivity extends AppCompatActivity {

    private ActivityPatchNoteBinding binding;
    private String currentVersion = "14.5.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatchNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("CURRENT_VERSION")) {
            currentVersion = getIntent().getStringExtra("CURRENT_VERSION");
        }

        setupWebView();
        setupNavigation();
        loadPatchNotes();
        
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupWebView() {
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        binding.webView.setBackgroundColor(getResources().getColor(R.color.lol_blue_deep));
        
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                binding.progressBar.setVisibility(View.GONE);
                injectCSS(view);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl.equals(constructPatchUrl(currentVersion)) && !failingUrl.contains("tags/patch-notes")) {
                     view.loadUrl("https://www.leagueoflegends.com/fr-fr/news/tags/patch-notes/");
                }
            }
            
            @Override
            public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                if (request.getUrl().toString().equals(constructPatchUrl(currentVersion)) && errorResponse.getStatusCode() == 404) {
                    view.post(() -> view.loadUrl("https://www.leagueoflegends.com/fr-fr/news/tags/patch-notes/"));
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("leagueoflegends.com") && 
                   (url.contains("/news/game-updates/") || url.contains("/news/tags/patch-notes"))) {
                    return false;
                }
                return true; 
            }
        });
    }

    private void injectCSS(WebView view) {
        String css = "nav, header, footer, #riotbar-bar, #riotbar-header, #riotbar-footer, .riotbar-navbar, .riotbar-mobile-bar, " +
                     "#riotbar-account, .footer-container, #osano-cm-window, [data-testid='riotbar'] " + 
                     "{ display: none !important; } " +
                     "html, body { margin-top: -90px !important; padding-top: 0 !important; background-color: #010a13 !important; } ";
        
        String js = "var css = \"" + css + "\"; " +
                    "var style = document.createElement('style'); " +
                    "style.innerHTML = css; " +
                    "document.head.appendChild(style); " +
                    "setInterval(function() { " +
                    "  var style = document.createElement('style'); " +
                    "  style.innerHTML = css; " +
                    "  document.head.appendChild(style); " +
                    "  /* Keep text-based footer hiding as it was working */ " +
                    "  var elements = document.getElementsByTagName('*'); " +
                    "  for (var i = 0; i < elements.length; i++) { " +
                    "    var el = elements[i]; " +
                    "    if (el.innerText && (el.innerText === 'A PROPOS DE LEAGUE OF LEGENDS' || el.innerText === 'CONDITIONS D\\'UTILISATION')) { " +
                    "       el.closest('footer, div[class*=\"footer\"], div[id*=\"footer\"]').style.display = 'none'; " +
                    "    } " +
                    "  } " +
                    "}, 500);"; 

        view.evaluateJavascript(js, null);
    }

    private void loadPatchNotes() {
        String url = "https://www.leagueoflegends.com/fr-fr/news/tags/patch-notes/";
        binding.webView.loadUrl(url);
    }

    private String constructPatchUrl(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                String major = parts[0];
                String minor = parts[1];
                return "https://www.leagueoflegends.com/fr-fr/news/game-updates/notes-de-patch-" + major + "-" + minor + "/";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "https://www.leagueoflegends.com/fr-fr/news/tags/patch-notes/";
    }

    private void setupNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_patch);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_champions) {
                startActivity(new Intent(PatchNoteActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_items) {
                startActivity(new Intent(PatchNoteActivity.this, ItemsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_patch) {
                return true;
            }
            return false;
        });
    }
}
