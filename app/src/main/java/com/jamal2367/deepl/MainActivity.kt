package com.jamal2367.deepl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyLog.TAG
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var queue: RequestQueue
    private lateinit var webViewClient: MyWebViewClient
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.WebView)
        queue = Volley.newRequestQueue(this)
        createWebView(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        createWebView(intent)
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(intent: Intent?) {
        val floatingText = intent?.getStringExtra("FLOATING_TEXT")
        val shareText = intent?.getStringExtra(Intent.EXTRA_TEXT)
        val receivedText = floatingText ?: (shareText ?: "")
        val defParamValue = "#en/en/"
        val urlParam = getSharedPreferences("config", Context.MODE_PRIVATE).getString("urlParam", defParamValue) ?: defParamValue
        val webView: WebView = findViewById(R.id.WebView)
        webViewClient = MyWebViewClient(this, webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = webViewClient
        webView.addJavascriptInterface(WebAppInterface(this, webView), "Android")
        webView.loadUrl("https://www.deepl.com/translator$urlParam${Uri.encode(receivedText)}")
        Handler(Looper.getMainLooper()).postDelayed({ checkForUpdates(this) }, 1000)
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
        Snackbar.make(findViewById(R.id.WebView), R.string.double_back_to_exit, Snackbar.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        return true
    }

    override fun onPause() {
        super.onPause()
        getSharedPreferences("config", Context.MODE_PRIVATE)
            .edit()
            .putString("urlParam", webViewClient.urlParam)
            .apply()
    }

    override fun onBackPressed() {
        when {
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        queue.cancelAll(TAG)
        super.onDestroy()
    }

    private fun checkForUpdates(context: Context) {
        val url = getString(R.string.github_update_check_url)
        val request = StringRequest(Request.Method.GET, url, { reply ->
            val latestVersion = Gson().fromJson(reply, JsonObject::class.java).get("tag_name").asString
            val current = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (latestVersion != current) {
                // We have an update available, tell our user about it
                Snackbar.make(findViewById(R.id.WebView), getString(R.string.app_name) + " " + latestVersion + " " + getString(R.string.update_available), 10000)
                .setAction(R.string.show) {
                        val releaseurl = getString(R.string.url_app_home_page)
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(releaseurl)
                        // Not sure that does anything
                        i.putExtra("SOURCE", "SELF")
                        startActivity(i)
                    }.show()
            }
        }, { error ->
            Log.w(TAG, "Update check failed", error)
        })
        request.tag = TAG
        queue.add(request)
    }

}
