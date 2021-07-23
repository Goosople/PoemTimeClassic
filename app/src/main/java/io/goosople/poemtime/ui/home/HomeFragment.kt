package io.goosople.poemtime.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import io.goosople.poemtime.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val onlineService = sharedPreferences.getBoolean("online_service", true)
        if (onlineService) {
            binding.poemLocal.visibility = View.GONE
            binding.poemLocalDetail.visibility = View.GONE
            binding.con.visibility = View.GONE
        }
        else{
            binding.poem.visibility = View.GONE
            val poemNum = sharedPreferences.getInt("poemNum", 1)
            with(sharedPreferences.edit()) {
                putInt("poemNum", poemNum)
                commit()
            }
            poemInit(poemNum)
        }

        return binding.root
    }


    private fun poemInit(num: Int) {
        val poemNumText: CharSequence = num.toString()
        binding.poemNum.setText(poemNumText)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}