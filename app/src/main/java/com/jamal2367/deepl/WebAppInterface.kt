package com.jamal2367.deepl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.android.material.snackbar.Snackbar

class WebAppInterface(
    private val context: Context,
    private var webView: WebView) {

    @JavascriptInterface
    fun copyClipboard(text: String) {
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("translation_text", text)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(webView, context.getString(R.string.copy_clipboard), Snackbar.LENGTH_SHORT).show()
    }

}
