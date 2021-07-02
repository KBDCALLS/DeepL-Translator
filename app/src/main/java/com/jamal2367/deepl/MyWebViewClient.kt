package com.jamal2367.deepl

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import com.google.android.material.snackbar.Snackbar


class MyWebViewClient(private val activity: MainActivity, private val webView: WebView) : WebViewClient() {
    private var param: String = "#en/en/"
    val urlParam: String get() = param

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)
        return true
    }

    override fun onPageFinished(view: WebView, url: String) {
        view.loadUrl(
            "javascript:" +
                    """
                        $('button').css('-webkit-tap-highlight-color','rgba(0, 0, 0, 0)');
                        $('#dl_translator').siblings().hide();
                        $('.dl_header_menu_v2__buttons__menu').hide();
                        $('.dl_header_menu_v2__buttons__item').hide();
                        $('.dl_header_menu_v2__links').children().not('#dl_menu_translator_simplified').hide();
                        $('.dl_header_menu_v2__separator').hide();
                        $('.lmt__bottom_text--mobile').hide();
                        $('.lmt__formalitySwitch__smaller__select_toggler').hide();
                        $('#dl_cookieBanner').hide();
                        $('.lmt__language_container_sec').hide();
                        $('.lmt__target_toolbar__save').hide();
                        $('.lmt__rating').hide();
                        $('footer').hide();
                        $('a').css('pointer-events','none');
                        $('.lmt__sides_container').css('margin-bottom','32px');
                        $('.lmt__translations_as_text__copy_button, .lmt__target_toolbar__copy').on('click',function() {
                            const text = $('.lmt__translations_as_text__text_btn').eq(0).text();
                            Android.copyClipboard(text);
                        });
                    """
        )
        webView.alpha = 1.0F
        Regex("""#(.+?)/(.+?)/""").find(webView.url!!)?.let { param = it.value }
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest, error: WebResourceError?) {
        if (request.isForMainFrame) {
            activity.setContentView(R.layout.error_page)
            val button: ImageButton = activity.findViewById(R.id.reload)
            val listener = ReloadButtonListener()
            button.setOnClickListener(listener)
            val errorDescription = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) error?.description.toString() else ""
            Snackbar.make(activity.findViewById(R.id.errText), errorDescription, Snackbar.LENGTH_LONG).show()
            Log.e("onReceivedError", errorDescription)
        }
    }

    private inner class ReloadButtonListener : View.OnClickListener {
        override fun onClick(view: View) {
            val i = Intent(activity, MainActivity::class.java)
            activity.finish()
            activity.overridePendingTransition(0, 0)
            activity.startActivity(i)
        }
    }

}
