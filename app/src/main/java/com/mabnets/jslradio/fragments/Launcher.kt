package com.mabnets.jslradio.fragments

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.mabnets.jslradio.R
import com.mabnets.jslradio.databinding.FragmentLauncherBinding

class Launcher : Fragment(R.layout.fragment_launcher) {
    private var _binding: FragmentLauncherBinding? = null
    private val binding get() = _binding!!
    private val handler= Handler()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLauncherBinding.bind(view)
    }
    private val runnable= Runnable {
        Navigation.findNavController(requireView()).navigate(R.id.action_launcher_to_mainfragment)
    }
    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable,1000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}