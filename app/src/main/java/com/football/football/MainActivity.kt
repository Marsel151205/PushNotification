package com.football.football

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import androidx.activity.viewModels
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.football.football.databinding.ActivityMainBinding
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import im.delight.android.webview.AdvancedWebView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AdvancedWebView.Listener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<ViewModel>()
    private var utm = ""
    var url = ""
    var webViewUrl = ""
    var analyticsId = ""
    var fcmToken = ""
    private lateinit var preferencesHelper: Preferences
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 123

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customizedWebView()

        preferencesHelper = Preferences(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            val msgInstance = FirebaseMessaging.getInstance()
            msgInstance.token.addOnCompleteListener {
                Log.e("kirill", it.result)
                fcmToken = it.result
            }
            getFirebaseAnalytics()
            url = getAdvertisingId(this@MainActivity)!!
            settingInstallReferrer()
            delay(5000)
            fetchWeb(url)
        }
        Log.e("kirill", webViewUrl)
    }

    private fun fetchWeb(url: String) {
        Log.e("kirill", "preft: ${preferencesHelper.id}")
        lifecycleScope.launch {
            getCountryCodeFromSim(this@MainActivity)?.let { sim ->
                Log.e("kirill", getFirebaseAnalytics())
                viewModel.getData(url, sim, utm, preferencesHelper.id.toString(), fcmToken)
                    .observe(this@MainActivity, Observer {
                        Log.e("kirill", it)
                        binding.pbLoading.visibility = View.GONE
                        Log.e("kirill", preferencesHelper.url.toString())
                        preferencesHelper.url?.let { binding.webview.loadUrl(it) }
                    })
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Уведомления разрешены", Toast.LENGTH_SHORT).show()
                saveNotificationPermission(true)
            } else {
                Toast.makeText(
                    this,
                    "Уведомления отключены. Включите их в настройках приложения.",
                    Toast.LENGTH_SHORT
                ).show()
                saveNotificationPermission(false)
            }
        }
    }

    private fun saveNotificationPermission(granted: Boolean) {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        preferences.edit().putBoolean("notification_permission", granted).apply()
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    private fun getAdvertisingId(context: Context): String? {
        if (isGooglePlayServicesAvailable(context)) {
            val advertisingInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            Log.e("kirill", advertisingInfo.id.toString())
            return advertisingInfo.id.toString()
        }
        return null
    }

    private fun settingInstallReferrer() {
        val installReferrerClient = InstallReferrerClient.newBuilder(this).build()

        installReferrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        val referrerDetails: ReferrerDetails = installReferrerClient.installReferrer

                        val referrerUrl = referrerDetails.installReferrer
                        val uri = Uri.parse("?$referrerUrl")
                        val source = uri.getQueryParameter("utm_source")
                        val medium = uri.getQueryParameter("utm_medium")
                        if (source != null && medium != null) {
                            Log.d("kirill", "utm_source: $source, utm_medium: $medium")
                            utm = "$source&utm_medium=$medium"
                        } else {
                            Log.d("kirill", "No utm_source or utm_medium found")
                        }
                    }
                    else -> {
                        Log.e("kirill", "Unable to connect to Install Referrer service")
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Обработка отключения сервиса, если необходимо
            }
        })
    }

    private fun getFirebaseAnalytics(): String {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        var k = ""
        firebaseAnalytics.appInstanceId.addOnSuccessListener { appInstanceId ->
            analyticsId = appInstanceId.toString()
            preferencesHelper.id = appInstanceId
            k = appInstanceId.toString()
            Log.e("kirill", "getFirebaseAnalytics: $analyticsId")
        }.addOnFailureListener { exception ->
            Log.e("kirill", "excep: $exception")
        }
        return k
    }


    private fun getCountryCodeFromSim(context: Context): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.simCountryIso
    }

    private fun customizedWebView() {
        binding.webview.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            private var mOriginalSystemUiVisibility = 0
            private var mOriginalOrientation = 0
            private var mCustomViewCallback: CustomViewCallback? = null

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (!isCameraPermissionGranted()) {
                    getCameraAndStoragePermissions()
                    binding.webview.pauseTimers()
                    request?.grant(request.resources)
                } else {
                    request?.grant(request.resources)
                }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (mCustomView != null) {
                    onHideCustomView()
                    return
                }
                mCustomView = view
                mOriginalSystemUiVisibility = window.decorView.systemUiVisibility
                mOriginalOrientation = requestedOrientation

                mCustomViewCallback = callback

                val decor = window.decorView as FrameLayout
                decor.addView(
                    mCustomView, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_IMMERSIVE
            }

            override fun onHideCustomView() {
                val decor = window.decorView as FrameLayout
                decor.removeView(mCustomView)
                mCustomView = null

                window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
                requestedOrientation = mOriginalOrientation

                mCustomViewCallback?.onCustomViewHidden()
                mCustomViewCallback = null
            }
        }
    }

    fun isCameraPermissionGranted(): Boolean {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCameraAndStoragePermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1
        )
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {}

    override fun onPageFinished(url: String?) {}

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {}

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {}
}