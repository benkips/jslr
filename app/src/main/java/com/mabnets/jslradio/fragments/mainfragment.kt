package com.mabnets.jslradio.fragments

//import com.mabnets.jslradio.BuildConfig
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.beraldo.playerlib.PlayerService
import com.mabnets.jslradio.R
import com.mabnets.jslradio.databinding.FragmentMainfragmentBinding
import org.jetbrains.anko.AnkoLogger


class mainfragment : Fragment(R.layout.fragment_mainfragment) ,AnkoLogger {


    private var isPlaying = false

    private var _binding: FragmentMainfragmentBinding? = null
    private val binding get() = _binding!!


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
            val c = "http://node-15.zeno.fm/gmdx1sb97f8uv?rj-ttl=5&rj-tok=AAABfccRdpIA8mopC5CghSrEoA"
            Navigation.findNavController(view).navigate(R.id.wvinfo, bundleOf("web" to c))
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



    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
       /* exoPlayer?.release()
        exoPlayer = null*/
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