package com.mabnets.jslradio.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.beraldo.playerlib.PlayerService
import com.mabnets.jslradio.R
import com.mabnets.jslradio.databinding.FragmentAlltimemsgBinding
import org.jetbrains.anko.AnkoLogger


class alltimemsg : Fragment(R.layout.fragment_alltimemsg) , AnkoLogger {
    private var isPlaying = false

    private var _binding:FragmentAlltimemsgBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding= FragmentAlltimemsgBinding.bind(view)
        val urls="http://node-15.zeno.fm/gmdx1sb97f8uv?rj-ttl=5&rj-tok=AAABfccRdpIA8mopC5CghSrEoA"
        binding.sname.text="24/7 EndTime Messages The LORD"
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
    }
    private fun initMediaControls() {
        initPlayButton()
    }

    private fun initPlayButton() {
        binding.btnPlay.requestFocus()
        binding.btnPlay.setOnClickListener { setPlayPause(!isPlaying) }
    }
    private fun setPlayPause(play: Boolean) {
        isPlaying = play
        if (!isPlaying) {
            binding.btnPlay.setImageResource(android.R.drawable.ic_media_play)
        } else {
            binding.btnPlay.setImageResource(android.R.drawable.ic_media_pause)
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
                binding.btnPlay.setOnClickListener {
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