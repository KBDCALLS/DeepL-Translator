package com.jamal2367.deepl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.Keep
import com.google.android.material.snackbar.Snackbar

@Keep
class WebAppInterface(private val context: Context, private var webView: WebView) {
    @JavascriptInterface
    fun copyClipboard(text: String) {
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("translation_text", text)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(webView, context.getString(R.string.copy_clipboard), Snackbar.LENGTH_SHORT).show()
    }
	
	@JavascriptInterface
    fun stringToBase64String(s: String): String {
        return Base64.encodeToString(s.toByteArray(), Base64.DEFAULT)
    }

    @JavascriptInterface
    fun getAssetsText(fileName: String): String {
        return context.assets.open(fileName).reader(Charsets.UTF_8).use { it.readText() }
    }
}
