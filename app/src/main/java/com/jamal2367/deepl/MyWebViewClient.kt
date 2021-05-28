package com.jamal2367.deepl

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton


class MyWebViewClient(private val activity: MainActivity, private val webView: WebView) : WebViewClient() {
    private var param: String = ""
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
                        $('.dl_header_menu_v2__links__item').hide();
                        $('.dl_header_menu_v2__separator').hide();
                        $('.lmt__bottom_text--mobile').hide();
                        $('.lmt__docTrans-tab-container').hide();
                        $('#dl_cookieBanner').hide();
                        $('.lmt__language_container_sec').hide();
                        $('.docTrans_translator_upload_button__inner_button').hide();
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

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        if (errorCode == ERROR_HOST_LOOKUP) {
            activity.setContentView(R.layout.error_page)
            val button: ImageButton = activity.findViewById(R.id.reload)
            val listener = ReloadButtonListener()
            button.setOnClickListener(listener)
        } else {
            return
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
