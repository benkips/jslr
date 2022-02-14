package com.mabnets.jslradio.fragments

import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.beraldo.playerlib.PlayerService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
//import com.mabnets.jslradio.BuildConfig
import com.mabnets.jslradio.R
import com.mabnets.jslradio.Utils.getBitmapFromVectorDrawable
import com.mabnets.jslradio.databinding.FragmentMainfragmentBinding
import org.jetbrains.anko.AnkoLogger
import java.util.*


class mainfragment : Fragment(R.layout.fragment_mainfragment) ,AnkoLogger {


    private var isPlaying = false

    private var _binding: FragmentMainfragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adView: AdView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding= FragmentMainfragmentBinding.bind(view)
        binding.toolbarLayout.title = "Jesus is lord radio"
        binding.fab.setOnClickListener { view ->
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "JESUS is LORD radio android app on playstore\n https://play.google.com/store/apps/details?id=com.mabnets.jslradio"
            )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
        binding.content.mrradio.setOnClickListener { view ->
            val c = "https://repentanceandholinessinfo.com/playradio.php"
            Navigation.findNavController(view).navigate(R.id.wvinfo, bundleOf("web" to c))
        }
        binding.content.altimesmssgs.setOnClickListener { view ->
            Navigation.findNavController(view).navigate(R.id.alltimemsg)
        }

        val urls="https://s3.radio.co/s97f38db97/listen"
        binding.content.sname.text="Jesus is Lord radio"
        //Toast.makeText(context, urls, Toast.LENGTH_SHORT).show()
        initMediaControls()
        //Start the service
        val intent = Intent(context, PlayerService::class.java).apply {
            putExtra(PlayerService.STREAM_URL, urls)
        }
        activity?.applicationContext!!.bindService(intent, connection, Context.BIND_AUTO_CREATE)



     //activate ads
        adView = AdView(context)
        binding.content.bannerContainertwo.addView(adView)
        adView.adUnitId = "ca-app-pub-4814079884774543/3165545505"

        adView.adSize = adSize
        val adRequest = AdRequest
            .Builder()
            .build()
        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
       /* exoPlayer?.release()
        exoPlayer = null*/
    }


    //ads
    private val adSize: AdSize
        get() {
            val display = activity?.windowManager!!.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.content.bannerContainertwo.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }



    private fun initMediaControls() {
        initPlayButton()
    }

    private fun initPlayButton() {
        binding.content.btnPlay.requestFocus()
        binding.content.btnPlay.setOnClickListener { setPlayPause(!isPlaying) }
    }

    /**
     * Starts or stops playback. Also takes care of the Play/Pause button toggling
     *
     * @param play True if playback should be started
     */
    private fun setPlayPause(play: Boolean) {
        isPlaying = play
        if (!isPlaying) {
            binding.content.btnPlay.setImageResource(android.R.drawable.ic_media_play)
        } else {
            binding.content.btnPlay.setImageResource(android.R.drawable.ic_media_pause)
        }
    }
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {}
        /*
         * Called after a successful bind with our PlayerService.
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.PlayerServiceBinder) {
               //service.getPlayerHolderInstance() // use the player and call methods on it to start and stop
                binding.content.btnPlay.setOnClickListener {
                    if (!isPlaying) {
                        Toast.makeText(context, "Playing..", Toast.LENGTH_LONG).show()
                        service.getPlayerHolderInstance().start()
                        setPlayPause(true)
                    } else {
                        Toast.makeText(context, "Pausing..", Toast.LENGTH_LONG).show()
                        service.getPlayerHolderInstance().stop()
                        setPlayPause(false)
                    }
                }


            }else{
                setPlayPause(false)
            }
        }
    }
}