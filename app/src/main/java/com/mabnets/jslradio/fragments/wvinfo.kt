package com.mabnets.jslradio.fragments

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.mabnets.jslradio.R
import com.mabnets.jslradio.Utils.visible
import com.mabnets.jslradio.databinding.FragmentWvinfoBinding


class wvinfo : Fragment(R.layout.fragment_wvinfo) {
    private  var _binding : FragmentWvinfoBinding?=null
    private val binding get() = _binding!!
    private lateinit var adView: AdView

    private var mUploadMessage: ValueCallback<Uri>? = null
    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    private val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding= FragmentWvinfoBinding.bind(view)
        var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            //val data: Intent? = result.data
            val intent = result.data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // There are no request codes
                uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, intent ))
                uploadMessage = null;
            }else{
                val result: Uri? =
                    if (intent == null || result.resultCode !== Activity.RESULT_OK) null else intent?.getData()
                mUploadMessage!!.onReceiveValue(result)
                mUploadMessage = null

            }
        }
        if (binding.wvvs!=null) {
            val url= arguments?.getString("web")
            binding.wvvs.settings.javaScriptEnabled = true
            binding.wvvs.webViewClient = WebViewClient()
            binding.wvvs.webChromeClient = WebChromeClient()
            if (url != null) {
                binding.wvvs.loadUrl(url)
            }
            binding.wvvs.settings.setSupportMultipleWindows(false);
            binding.wvvs.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    try {
                        binding.pgbar.visible(true)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }

                    super.onPageStarted(view, url, favicon)

                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    try {
                        binding.pgbar.visible(false)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }

                    super.onPageFinished(view, url)
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    try {
                        binding.pgbar.visible(false)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                    val myerrorpage = "file:///android_asset/android/errorpage.html";
                    binding.wvvs.loadUrl(myerrorpage)
                    /*super.onReceivedError(view, errorCode, description, failingUrl)*/

                }
            }
            binding.wvvs.setDownloadListener(object : DownloadListener {
                override fun onDownloadStart(
                    url: String, userAgent: String,
                    contentDisposition: String, mimetype: String,
                    contentLength: Long
                ) {

                    //getting file name from url
                    val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
                    //DownloadManager.Request created with url.
                    val request = DownloadManager.Request(Uri.parse(url))
                    //cookie
                    val cookie = CookieManager.getInstance().getCookie(url)
                    //Add cookie and User-Agent to request
                    request.addRequestHeader("Cookie", cookie)
                    request.addRequestHeader("User-Agent", userAgent)
                    //file scanned by MediaScannar
                    request.allowScanningByMediaScanner()
                    request.setDescription("Download file...")
                    //Download is visible and its progress, after completion too.
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    //DownloadManager created
                    val downloadmanager =
                        activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    //Saving file in Download folder
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        filename
                    )
                    //download enqued
                    downloadmanager.enqueue(request)

                    Toast.makeText(context, "Downloading file", Toast.LENGTH_SHORT).show()
                }

            })

            binding.wvvs.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP && binding.wvvs.canGoBack()) {
                    binding.wvvs.goBack()
                    return@OnKeyListener true
                }
                false
            })

            binding.wvvs.webChromeClient = object : WebChromeClient() {
                // For 3.0+ Devices (Start)
                // onActivityResult attached before constructor
                protected fun openFileChooser(uploadMsg: ValueCallback<Uri>?, acceptType: String?) {
                    mUploadMessage = uploadMsg
                    var i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)
                    i.setType("/*")
                    i= Intent.createChooser(i, "File Browser")
                    resultLauncher.launch(i)

                    /* startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE
                    )*/

                }

                // For Lollipop 5.0+ Devices
                override fun onShowFileChooser(
                    mWebView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams
                ): Boolean {
                    if (uploadMessage != null) {
                        uploadMessage!!.onReceiveValue(null)
                        uploadMessage = null
                    }
                    uploadMessage = filePathCallback
                    var intent: Intent? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent = fileChooserParams.createIntent()
                    }
                    resultLauncher.launch(intent)

                    /* try {
                         startActivityForResult(intent, REQUEST_SELECT_FILE)
                     } catch (e: ActivityNotFoundException) {
                         uploadMessage = null
                         Toast.makeText(
                             this@MainActivity,
                             "Cannot Open File Chooser",
                             Toast.LENGTH_LONG
                         ).show()
                         return false
                     }*/
                    return true
                }
            }
        }
        //for ads
        adView = AdView(context)
        binding.bannerContainertwo.addView(adView)
        adView.adUnitId = "ca-app-pub-4814079884774543/3165545505"

        adView.adSize = adSize
        val adRequest = AdRequest
            .Builder()
            .build()
        // Start loading the ad in the background.
        adView.loadAd(adRequest)

    }
    //forads
    private val adSize: AdSize
        get() {
            val display =activity?.windowManager!!.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.bannerContainertwo.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
   }