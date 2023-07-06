package it.ter.sync.view

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.databinding.FragmentHomeBinding
import it.ter.sync.view.adapter.PostAdapter
import it.ter.sync.viewmodel.UserViewModel
import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.view.size
import com.google.android.gms.location.*
import it.ter.sync.database.user.UserData
import it.ter.sync.viewmodel.NotificationViewModel


class HomeFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerPost
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        postAdapter = PostAdapter(emptyList(), emptyList(), UserData(),notificationViewModel)
        recyclerView.adapter = postAdapter

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        val kms = arrayOf("5 Km", "20 Km", "50 Km", "200 Km")
        val spinner = binding.spinnerSplitter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // Seleziono 5 Km come default
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = kms[position] // Ottieni l'elemento selezionato dallo Spinner
                // Estraggo solo il numero dalla stringa
                val kmNumber = selectedItem.replace(Regex("[^0-9.]"), "").toDouble()
                userViewModel.updateMaxDistance(kmNumber)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Ottieni i parametri di layout correnti
        val layoutParams = recyclerView.layoutParams as RelativeLayout.LayoutParams
        // Imposta la regola di posizionamento della recyclerView
        layoutParams.addRule(RelativeLayout.BELOW, R.id.search_view)

        binding.btnSearch.setOnClickListener {
            if(binding.searchView.text.isNotEmpty()) {
                val searchString = binding.searchView.text.toString().replace(" ", "")
                binding.searchView.text.clear()
                binding.searchView.clearFocus()

                val visibleButton =
                    findFirstVisibleButton(binding.btnTag1, binding.btnTag2, binding.btnTag3)

                if (visibleButton != null) {
                    visibleButton.visibility = View.VISIBLE
                    visibleButton.text = searchString

                    layoutParams.addRule(RelativeLayout.BELOW, R.id.layout_tags)

                    userViewModel.addTag(searchString)
                }
            }
            (activity as MainActivity).hideKeyboard()
        }

        binding.btnTag1.setOnClickListener {
            // Rendere invisibili i bottoni
            binding.btnTag1.visibility = View.INVISIBLE
            userViewModel.removeTag(binding.btnTag1.text.toString())
            binding.btnTag1.text = ""
            if(areAllInvisible(binding.btnTag1,binding.btnTag2,binding.btnTag3))
                layoutParams.addRule(RelativeLayout.BELOW, R.id.search_view)
        }

        binding.btnTag2.setOnClickListener {
            // Rendere invisibili i bottoni
            binding.btnTag2.visibility = View.INVISIBLE
            userViewModel.removeTag(binding.btnTag2.text.toString())
            binding.btnTag2.text = ""
            if(areAllInvisible(binding.btnTag1,binding.btnTag2,binding.btnTag3))
                layoutParams.addRule(RelativeLayout.BELOW, R.id.search_view)
        }

        binding.btnTag3.setOnClickListener {
            // Rendere invisibili i bottoni
            binding.btnTag3.visibility = View.INVISIBLE
            userViewModel.removeTag(binding.btnTag3.text.toString())
            binding.btnTag3.text = ""
            if(areAllInvisible(binding.btnTag1,binding.btnTag2,binding.btnTag3))
                layoutParams.addRule(RelativeLayout.BELOW, R.id.search_view)
        }

        initObservers()

        return root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!userViewModel.isUserLoggedIn()) {
            // Utente non autenticato, reindirizza al fragment di login
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        userViewModel.getUserInfo()
        notificationViewModel.retrieveLikes()


        // Prendo gli utenti
        userViewModel.retrieveUsers()

        // L'utente è autenticato, verifica le autorizzazioni della posizione
        requestLocationUpdates()
    }

    private fun findFirstVisibleButton(vararg buttons: Button): Button? {
        for (button in buttons) {
            if (button.visibility == View.INVISIBLE) {
                return button
            }
        }
        return null
    }

    private fun areAllInvisible(vararg buttons: Button): Boolean {
        for (button in buttons) {
            if (button.visibility == View.VISIBLE) {
                return false
            }
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .apply {
                setMinUpdateDistanceMeters(10f)
                setGranularity(Granularity.GRANULARITY_FINE)
                setWaitForAccurateLocation(true)
            }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation

                val latitude = lastLocation?.latitude!!
                val longitude = lastLocation?.longitude!!

                userViewModel.updateUserPosition(latitude, longitude)

                Log.d("HomeFragment", "Coordinate: Latitude = $latitude, Longitude = $longitude")
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } else {
            // Richiedi l'autorizzazione all'utente
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }


    /**
     * it initializes the view model and the methods used to retrieve the live data for the interface
     */
    private fun initObservers() {
        Log.i(TAG, "Registering Observers: ViewModel? $userViewModel")
        userViewModel.users.observe(viewLifecycleOwner) {
            postAdapter.setPostList(it)
        }
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            postAdapter.setCurrentUser(it)
        }
        notificationViewModel.likeList.observe(viewLifecycleOwner) {
            postAdapter.setLikeList(it)
        }
    }

}