package com.bangnv.cafeorder.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.databinding.ActivityWebViewBinding
import java.io.FileOutputStream


class WebViewActivity : AppCompatActivity() {

    companion object {
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"
        private const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
    }

    private lateinit var mActivityWebViewBinding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivityWebViewBinding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(mActivityWebViewBinding.root)

        initToolbar()
        initWebView()


        val startUrl =
            intent?.extras?.getString(Constant.KEY_INTENT_URL) ?: "https://example.com"

        mActivityWebViewBinding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Kiểm tra xem URL có phải là link 1 không
                if (url != null && url.startsWith(startUrl)) {
                    // Chuyển hướng WebView sang link 2 (link cung cấp bởi MoMo)
                    view?.loadUrl(url)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

        mActivityWebViewBinding.webView.loadUrl(startUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val webView = mActivityWebViewBinding.webView
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true // Cho phép thực thi JavaScript
        settings.userAgentString = DESKTOP_USER_AGENT // Thiết lập user agent cho WebView
        webView.isHorizontalScrollBarEnabled = false
        webView.isHorizontalFadingEdgeEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun initToolbar() {
        mActivityWebViewBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityWebViewBinding.toolbar.tvTitle.text =
            getString(R.string.payment_detail)
        mActivityWebViewBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

}