package com.jamal2367.deepl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var webView: WebView

    private var currentUrl: String = ""
    private var doubleBackToExitPressedOnce = false
    private var urlLoaded = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        context = applicationContext
        activity = this
        webView = findViewById(R.id.WebView)
        configureWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView()
    {
        webView.webViewClient = MyWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://www.deepl.com/translator")
        swipe = findViewById(R.id.swipeContainer)
        swipe.setOnRefreshListener {
            webView.loadUrl("https://www.deepl.com/translator")
            swipe.isRefreshing = false
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onReceivedError(webview: WebView, errorCode: Int, error: String, failingUrl: String) {
            if(errorCode != -1) {
                Thread.sleep(500)
                val reloadCode = "window.location.href = '$failingUrl';"
                val title = activity.getString(R.string.error_title)
                val reload = activity.getString(R.string.error_reload)
                val check = activity.getString(R.string.error_check)
                webview.loadUrl("about:blank")
                webview.loadDataWithBaseURL(failingUrl, buildErrorPage(title, error, check, reload, true, reloadCode), "text/html", "UTF-8", null)
                currentUrl = failingUrl
                urlLoaded = failingUrl
            }
        }
    }

    fun buildErrorPage(title: String?, error: String?, check: String?, reload: String?, showButton: Boolean, reloadCode: String = "window.history.back();"): String {
        var reloadButtonCode = "<button onclick=\"reload();\" id=\"reload-button\" class=\"blue-button text-button reload\">$reload</button>"

        when(showButton){
            false -> reloadButtonCode = ""
        }

        return "<html>" +
                "<head>" +
                "<script language=\"javascript\"> " +
                "function reload(){setTimeout(function(){$reloadCode}, 500);" +
                "};</script>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>html{-webkit-text-size-adjust: 100%;font-size: 125%;}body{background-color: #FFFFFF; color: #000000; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\"; font-size: 75%;}img{pointer-events: none;}.unselectable{-webkit-user-select: none; -webkit-touch-callout: none; -moz-user-select: none; -ms-user-select: none; user-select: none;}div{display:block;}p{color: #000000;}h1{margin-top: 0; color: #000000; font-size: 1.6em; font-weight: normal; line-height: 1.25em; margin-bottom: 16px;}button{border: solid 1px; border-radius: 4px; border-color: #F44336; padding: 0 16px; min-width: 64px; line-height: 34px; background-color: transparent; -webkit-user-select: none; text-transform: uppercase; color: #F44336; box-sizing: border-box; cursor: pointer; font-size: .875em; margin: 0; font-weight: 500;}button:hover{box-shadow: 0 1px 2px rgba(1, 1, 1, 0.5);}.error-code{color: #000000; display: inline; font-size: .86667em; margin-top: 15px; opacity: .5; text-transform: uppercase;}.interstitial-wrapper{box-sizing: border-box;font-size: 1em;margin: 100px auto 0;max-width: 600px;width: 100%;}.offline .interstitial-wrapper{color: #2b2b2b;font-size: 1em;line-height: 1.55;margin: 0 auto;max-width: 600px;padding-top: 100px;width: 100%;}.hidden{display: none;}.nav-wrapper{margin-top: 51px; display:inline-block;}#buttons::after{clear: both; content: ''; display: block; width: 100%;}.nav-wrapper::after{clear: both; content: ''; display: table; width: 100%;}.small-link{color: #696969; font-size: .875em;}@media (max-width: 640px), (max-height: 640px){h1{margin: 0 0 15px;}button{width: 100%;}}" +
                "</style>" +
                "</head>" +
                "<center>" +
                "<body class=\"offline\">" +
                "<div class=\"interstitial-wrapper\">" +
                "<div id=\"main-content\">" +
                "<img src=\"file:///android_asset/warning.webp\" height=\"128\" width=\"128\"><br><br>" +
                "<div class=\"icon icon-offline\"></div>" +
                "<div id=\"main-message\">" +
                "<h1 class=\"unselectable\">$title</h1>" +
                "<p class=\"unselectable\">$check</p>" +
                "<p>&nbsp;</p>" +
                "</h1><p></p><div class=\"error-code\">$error" +
                "</div></div></div><div id=\"buttons\" class=\"nav-wrapper\"><div id=\"control-buttons\">$reloadButtonCode" +
                "</div></div></div></body></center></html>"
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }

        if (doubleBackToExitPressedOnce) {
            return super.onKeyDown(keyCode, event)
        }

        this.doubleBackToExitPressedOnce = true
        Snackbar.make(findViewById(android.R.id.content), R.string.double_back_to_exit, Snackbar.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        return true
    }

    override fun onBackPressed() {
        when {
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }
}
