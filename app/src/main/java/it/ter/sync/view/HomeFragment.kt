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
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.location.*


class HomeFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null

    private lateinit var recyclerView: RecyclerView

    private lateinit var postAdapter: PostAdapter

    private var searchString: String = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val PERMISSIONS_REQUEST_LOCATION = 1


    private fun checkLocationPermission() {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            locationPermission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            requestLocationUpdates()
        } else {
            // Richiedi l'autorizzazione all'utente
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(locationPermission),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }


    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000 // Intervallo di aggiornamento in millisecondi
            fastestInterval = 5000 // Intervallo minimo di aggiornamento in millisecondi
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Precisione richiesta
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                // Usa lastLocation per le tue operazioni
                val latitude = lastLocation?.latitude!!
                val longitude = lastLocation?.longitude!!


                // Stampa le coordinate nel log
                Log.d("HomeFragment", "Coordinate: Latitude = $latitude, Longitude = $longitude")

                // Update home
                userViewModel.updateHome(latitude, longitude, searchString)
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
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }



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

        postAdapter = PostAdapter(emptyList())
        recyclerView.adapter = postAdapter

        userViewModel.getUserInfo()


        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        initObservers()

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!userViewModel.isUserLoggedIn()) {
            // Utente non autenticato, reindirizza al fragment di login
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        val editTextSearch: EditText = view.findViewById(R.id.search_view)
        val buttonSearch: Button = view.findViewById(R.id.btn_search)
        val buttonClean: Button = view.findViewById(R.id.btn_clean)
        val searchView: EditText = view.findViewById(R.id.search_view)

        // Ottenere il riferimento ai bottoni
        val btnTag1 = view?.findViewById<Button>(R.id.btn_tag_1)
        val btnTag2 = view?.findViewById<Button>(R.id.btn_tag_2)
        val btnTag3 = view?.findViewById<Button>(R.id.btn_tag_3)


        buttonSearch.setOnClickListener {
            searchString = editTextSearch.text.toString().trim()


            // Verificare se i bottoni sono visibili
            val isBtnTag1Visible = btnTag1?.visibility == View.VISIBLE
            val isBtnTag2Visible = btnTag2?.visibility == View.VISIBLE
            val isBtnTag3Visible = btnTag3?.visibility == View.VISIBLE

            if(!isBtnTag1Visible) {
                // Impostare la visibilità dei bottoni
                btnTag1?.visibility = View.VISIBLE
                // Impostare il testo del bottone
                btnTag1?.text = searchString

            }
            if(!isBtnTag2Visible && isBtnTag1Visible) {
                btnTag2?.visibility = View.VISIBLE
                btnTag2?.text = searchString


            }
            if(!isBtnTag3Visible && isBtnTag1Visible && isBtnTag2Visible) {
                btnTag3?.visibility = View.VISIBLE
                btnTag3?.text = searchString

            }

        }


        btnTag1?.setOnClickListener {
            searchString = ""
            searchView.setText("")
            // Rendere invisibili i bottoni
            btnTag1?.visibility = View.INVISIBLE
        }

        btnTag2?.setOnClickListener {
            searchString = ""
            searchView.setText("")
            // Rendere invisibili i bottoni
            btnTag2?.visibility = View.INVISIBLE
        }

        btnTag3?.setOnClickListener {
            searchString = ""
            searchView.setText("")
            // Rendere invisibili i bottoni
            btnTag3?.visibility = View.INVISIBLE
        }

        buttonClean.setOnClickListener {
            searchString = ""
            searchView.setText("")

        }


        // L'utente è autenticato, verifica le autorizzazioni della posizione
        checkLocationPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * it initializes the view model and the methods used to retrieve the live data for the interface
     */
    private fun initObservers() {
        Log.i(TAG, "Registering Observers: ViewModel? $userViewModel")
        userViewModel.users.observe(viewLifecycleOwner) {
            postAdapter.setPostList(it)
        }
    }

}