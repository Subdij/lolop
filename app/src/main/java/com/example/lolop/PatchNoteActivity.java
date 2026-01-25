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
import com.example.lolop.utils.LocaleHelper;
import android.content.Context;

public class PatchNoteActivity extends BaseActivity {

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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private void setupWebView() {
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // Optimisation: Cache
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
                     view.loadUrl(getPatchNotesParams(true));
                }
            }
            
            @Override
            public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                if (request.getUrl().toString().equals(constructPatchUrl(currentVersion)) && errorResponse.getStatusCode() == 404) {
                    view.post(() -> view.loadUrl(getPatchNotesParams(true)));
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
                     "#riotbar-account, .footer-container, #osano-cm-window, [data-testid='riotbar'], .riotbar-content-container, " + 
                     "#riotbar-footer-container, div[data-testid='footer'] " + 
                     "{ display: none !important; } " +
                     "html, body { margin-top: -90px !important; padding-top: 0 !important; background-color: #010a13 !important; } ";
        
        String js = "(function() {" +
                    "    var css = \"" + css + "\";" +
                    "    var style = document.createElement('style');" +
                    "    style.innerHTML = css;" +
                    "    document.head.appendChild(style);" +
                    "    " +
                    "    function hideElements() {" +
                    "        var elements = document.getElementsByTagName('*');" +
                    "        for (var i = 0; i < elements.length; i++) {" +
                    "            var el = elements[i];" +
                    "            if (el.innerText) {" +
                    "               var text = el.innerText.toUpperCase();" +
                    "               if (text.includes('ABOUT LEAGUE OF LEGENDS') || text.includes('A PROPOS DE LEAGUE OF LEGENDS') ||" +
                    "                   text.includes('HELP US IMPROVE') || text.includes('AIDEZ-NOUS') ||" +
                    "                   text.includes('PRIVACY NOTICE') || text.includes('POLITIQUE DE CONFIDENTIALITE')) {" +
                    "                   var target = el.closest('footer'); " +
                    "                   if (!target) target = el.closest('div[id*=\"footer\"]'); " +
                    "                   if (!target) target = el.closest('div[class*=\"footer\"]'); " +
                    "                   if (target) { target.style.display = 'none'; target.style.visibility = 'hidden'; }" +
                    "               }" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "    " +
                    "    /* Initial Run */" +
                    "    hideElements();" +
                    "    " +
                    "    /* Mutation Observer for dynamic content */" +
                    "    var observer = new MutationObserver(function(mutations) {" +
                    "        hideElements();" +
                    "    });" +
                    "    observer.observe(document.body, { childList: true, subtree: true });" +
                    "})();";

        view.evaluateJavascript(js, null);
    }

    private void loadPatchNotes() {
        String url = constructPatchUrl(currentVersion);
        binding.webView.loadUrl(url);
    }

    private String constructPatchUrl(String version) {
        return getPatchNotesParams(false);
    }

    private String getPatchNotesParams(boolean tagsOnly) {
        String lang = LocaleHelper.getLanguage(this);
        // Use "en-gb" for English to match EU-style paths often used, or "en-us". 
        // "en-gb" is generally safer for global consistency with FR structure on LoL site.
        String region = lang.equals("fr") ? "fr-fr" : "en-gb";
        String baseUrl = "https://www.leagueoflegends.com/" + region + "/news/";
        
        if (tagsOnly) {
            return baseUrl + "tags/patch-notes/";
        }

        try {
            String[] parts = currentVersion.split("\\.");
            if (parts.length >= 2) {
                String major = parts[0];
                String minor = parts[1];
                
                if (lang.equals("fr")) {
                    return baseUrl + "game-updates/notes-de-patch-" + major + "-" + minor + "/";
                } else {
                    return baseUrl + "game-updates/patch-" + major + "-" + minor + "-notes/";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baseUrl + "tags/patch-notes/";
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
