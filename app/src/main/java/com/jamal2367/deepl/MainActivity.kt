package com.jamal2367.deepl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyLog.TAG
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject

class MainActivity : AppCompatActivity() {
    private lateinit var webViewClient: MyWebViewClient
    private lateinit var queue: RequestQueue
    private var doubleBackToExitPressedOnce = false
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val startUrl by lazy {
        val urlParam = getSharedPreferences("config", Context.MODE_PRIVATE).getString(
            "urlParam",
            defParamValue
        ) ?: defParamValue
        return@lazy "https://www.deepl.com/translator$urlParam"
    }

    companion object {
        private const val REQUEST_SELECT_FILE = 100
        private const val defParamValue = "#en/en/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        queue = Volley.newRequestQueue(this)
        createWebView(intent, savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        createWebView(intent)
    }


    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun createWebView(intent: Intent?, savedInstanceState: Bundle? = null) {
        val floatingText = intent?.getStringExtra("FLOATING_TEXT")
        val shareText = intent?.getStringExtra(Intent.EXTRA_TEXT)
        val savedText = savedInstanceState?.getString("SavedText")
        val receivedText = savedText ?: (floatingText ?: (shareText ?: ""))
        val webView: WebView = findViewById(R.id.webview)
        webViewClient = MyWebViewClient(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = webViewClient
        webView.webChromeClient = MyWebChromeClient()
        webView.addJavascriptInterface(WebAppInterface(this, webView), "Android")
        webView.loadUrl(startUrl + Uri.encode(receivedText.replace("/", "\\/")))
        Handler(Looper.getMainLooper()).postDelayed({ checkForUpdates(this) }, 1000)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val webView: WebView = findViewById(R.id.webview)

        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }

        if (doubleBackToExitPressedOnce) {
            return super.onKeyDown(keyCode, event)
        }

        this.doubleBackToExitPressedOnce = true
        Snackbar.make(findViewById(R.id.webview), R.string.double_back_to_exit, Snackbar.LENGTH_LONG).show()
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
        val webView: WebView = findViewById(R.id.webview)

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
                Snackbar.make(findViewById(R.id.webview), getString(R.string.app_name) + " " + latestVersion + " " + getString(R.string.update_available), 10000)
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

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_FILE -> uploadMessage?.let { message ->
                var result = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                if (result == null) {
                    result = if (intent.data != null) arrayOf(intent.data) else null
                }
                message.onReceiveValue(result)
                uploadMessage = null
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val webView: WebView = findViewById(R.id.webview)
        val url = webView.url ?: ""
        if (url.length > startUrl.length) {
            val inputText = Uri.decode(url.substring(startUrl.length)).replace("\\/", "/")
            outState.putString("SavedText", inputText)
        }
    }

    @Suppress("DEPRECATION")
    inner class MyWebChromeClient : WebChromeClient() {
        override fun onShowFileChooser(
            mWebView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (uploadMessage != null) {
                uploadMessage?.onReceiveValue(null)
                uploadMessage = null
            }

            uploadMessage = filePathCallback

            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }

            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE)
            } catch (e: Exception) {
                uploadMessage = null
                Snackbar.make(findViewById(R.id.webview), R.string.cannot_open, Snackbar.LENGTH_LONG).show()
                return false
            }

            return true
        }
    }

}
