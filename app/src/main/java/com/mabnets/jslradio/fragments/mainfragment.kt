package com.mabnets.jslradio.fragments

import android.app.Notification
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.mabnets.jslradio.BuildConfig
import com.mabnets.jslradio.R
import com.mabnets.jslradio.Utils.getBitmapFromVectorDrawable
import com.mabnets.jslradio.databinding.FragmentMainfragmentBinding
import java.util.*


class mainfragment : Fragment(R.layout.fragment_mainfragment) {
    private var playbackStateListener: PlaybackStateListener? = null
    private var exoPlayer: SimpleExoPlayer? = null

    private var handler: Handler? = null
    private var isPlaying = false

    private var _binding: FragmentMainfragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adView: AdView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding= FragmentMainfragmentBinding.bind(view)
        binding.toolbarLayout.title = "Jesus is lord radio"
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Sharing option will be available soon", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            /* val sendIntent = Intent()
             sendIntent.action = Intent.ACTION_SEND
             sendIntent.putExtra(
                 Intent.EXTRA_TEXT,
                 "Repentance and Holiness android app on playstore\n https://play.google.com/store/apps/details?id=www.digitalexperts.church_traker&hl=en"
             )
             sendIntent.type = "text/plain"
             startActivity(sendIntent)*/
        }
        binding.content.mrradio.setOnClickListener { view ->
            val c = "https://repentanceandholinessinfo.com/playradio.php"
            Navigation.findNavController(view).navigate(R.id.wvinfo, bundleOf("web" to c))
        }

        val urls="https://s3.radio.co/s97f38db97/listen"
        binding.content.sname.text="Jesus is Lord radio"
        //Toast.makeText(context, urls, Toast.LENGTH_SHORT).show()
        prepareExoPlayerFromURL(Uri.parse(urls))


        adView = AdView(context)
        binding.content.bannerContainertwo.addView(adView)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

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
        exoPlayer?.release()
        exoPlayer = null
    }
    private fun initSeekBar() {
        binding.content.mediacontrollerProgress.requestFocus()
        binding.content.mediacontrollerProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return
                }
                exoPlayer?.seekTo(progress * 1000.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.content.mediacontrollerProgress.max = 0
        binding.content.mediacontrollerProgress.max = exoPlayer?.duration!!.toInt() / 1000
    }
    private fun stringForTime(timeMs: Int): String? {
        val mFormatBuilder: StringBuilder
        val mFormatter: Formatter
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
    private fun setProgress() {
        binding.content.mediacontrollerProgress.progress = 0
        binding.content.mediacontrollerProgress.max = exoPlayer?.duration!!.toInt() / 1000
        binding.content.timeCurrent.text = stringForTime(exoPlayer?.currentPosition!!.toInt())
        binding.content.playerEndTime.text = stringForTime(exoPlayer?.duration!!.toInt())
        if (handler == null) handler = Handler()
        //Make sure you update Seekbar on UI thread
        handler!!.post(object : Runnable {
            override fun run() {
                if (exoPlayer != null && isPlaying) {
                    binding.content.mediacontrollerProgress.max = exoPlayer?.duration!!.toInt() / 1000
                    val mCurrentPosition = exoPlayer?.currentPosition!!.toInt() / 1000
                    binding.content.mediacontrollerProgress.progress = mCurrentPosition
                    binding.content.timeCurrent.text = stringForTime(exoPlayer?.currentPosition!!.toInt())
                    binding.content.playerEndTime.text = stringForTime(exoPlayer?.duration!!.toInt())
                    handler!!.postDelayed(this, 1000)
                }


            }
        })
    }
    private inner class PlaybackStateListener : Player.EventListener {

        override fun onPlayerStateChanged(
            playWhenReady: Boolean,
            playbackState: Int
        ) {
            val stateString: String
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    stateString = "ExoPlayer.STATE_IDLE      -"
                }
                ExoPlayer.STATE_BUFFERING -> {
                    stateString = "ExoPlayer.STATE_BUFFERING -"
                    binding.content.pgbar.visibility = View.VISIBLE
                }
                ExoPlayer.STATE_READY -> {
                    initListener()
                    stateString = "ExoPlayer.STATE_READY     -"
                    binding.content.pgbar.visibility = View.GONE
                    Log.i(
                        ContentValues.TAG, "ExoPlayer ready! pos: " + exoPlayer?.currentPosition
                                + " max: " + stringForTime(exoPlayer?.duration!!.toInt())
                    )
                    setProgress()
                }
                ExoPlayer.STATE_ENDED -> {
                    stateString = "ExoPlayer.STATE_ENDED     -"
                    //Stop playback and return to start position
                    setPlayPause(false)
                    exoPlayer?.seekTo(0)
                }
                else -> stateString = "UNKNOWN_STATE             -"
            }
            Log.d(
                ContentValues.TAG, "changed state to " + stateString
                        + " playWhenReady: " + playWhenReady
            )
        }
    }
    private fun initListener() {
        val playerNotificationManager: PlayerNotificationManager
        val notificationId = 1234
        val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentSubText(player: Player): CharSequence? {
                return "Live"
            }

            override fun getCurrentContentTitle(player: Player): String {
                return "JESUS is LORD RADIO"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                return null
            }

            override fun getCurrentContentText(player: Player): String {
                return "Live stream"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                /*return AppCompatResources.getDrawable(context!!, R.drawable.jslord)?.toBitmap()*/
                return requireContext().getBitmapFromVectorDrawable(R.drawable.logo)
            }
        }

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            requireContext(),
            "My_channel_id",
            R.string.app_name,
            notificationId,
            mediaDescriptionAdapter,
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {

                }

                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                }

            })

        playerNotificationManager.setUseNavigationActions(false)
        playerNotificationManager.setUseNavigationActionsInCompactView(false)
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setFastForwardIncrementMs(0)
        playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        playerNotificationManager.setPlayer(exoPlayer)
    }

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

    private fun prepareExoPlayerFromURL(uri: Uri) {
        val trackSelector = DefaultTrackSelector()
        val loadControl: LoadControl = DefaultLoadControl()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext(), trackSelector, loadControl)
        val userAgent = Util.getUserAgent(requireContext(), BuildConfig.APPLICATION_ID)
        val mediaSource = ExtractorMediaSource(
            uri,
            DefaultDataSourceFactory(context, userAgent),
            DefaultExtractorsFactory(),
            null,
            null
        )
        playbackStateListener = PlaybackStateListener()
        exoPlayer?.addListener(playbackStateListener!!)
        exoPlayer?.prepare(mediaSource)

        initMediaControls()

    }

    private fun initMediaControls() {
        initPlayButton()
        initSeekBar()
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
        exoPlayer?.playWhenReady = play
        if (!isPlaying) {
            binding.content.btnPlay.setImageResource(android.R.drawable.ic_media_play)
        } else {
            setProgress()
            binding.content.btnPlay.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

}