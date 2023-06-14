package it.ter.sync.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.databinding.FragmentHomeBinding
import it.ter.sync.view.adapter.PostAdapter
import it.ter.sync.viewmodel.HomeViewModel
import it.ter.sync.viewmodel.UserViewModel

class HomeFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null

    private lateinit var recyclerView: RecyclerView


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.getAllUsers()

        recyclerView = binding.recyclerPost
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        initObservers()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!userViewModel.isUserLoggedIn()) {
            // Utente non autenticato, reindirizza al fragment di login
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * it initializes the view model and the methods used to retrieve the live data for the interface
     */
    private fun initObservers() {
        Log.i(TAG, "Registering Observers: ViewModel? $homeViewModel")
        homeViewModel.users.observe(viewLifecycleOwner) {

            val adapter = PostAdapter(it)
            recyclerView.adapter = adapter
        }

    }

}