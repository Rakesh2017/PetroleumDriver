package enhabyto.com.petroleumdriver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class TermAndCondition extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_and_condition);

        WebView browser = findViewById(R.id.terms_webview);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadUrl("http://www.google.com");
        browser.loadUrl("file:///android_asset/disclaimer.html");
       // setContentView(browser);


    }
}
