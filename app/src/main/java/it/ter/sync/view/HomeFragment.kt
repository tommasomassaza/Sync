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
import android.location.LocationListener
import android.location.LocationManager
import android.content.Context
import android.location.Location
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import it.ter.sync.database.user.UserData
import kotlin.math.*


class HomeFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null

    private lateinit var recyclerView: RecyclerView


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    val filteredUsers = mutableListOf<UserData>()

    private lateinit var name: String
    private lateinit var age: String
    private lateinit var location: String





    private lateinit var fusedLocationClient: FusedLocationProviderClient


    //Verifica le autorizzazioni dall'utente per la posizione
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
            interval = 10000 // Intervallo di aggiornamento in millisecondi
            fastestInterval = 5000 // Intervallo minimo di aggiornamento in millisecondi
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Precisione richiesta
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                // Usa lastLocation per le tue operazioni
                val latitude = lastLocation.latitude
                val longitude = lastLocation.longitude
                // Esegui le operazioni desiderate con le coordinate geografiche ottenute


                // Stampa le coordinate nel log
                Log.d("HomeFragment", "Coordinate: Latitude = $latitude, Longitude = $longitude")
                // Stampa le coordinate nel log
                Log.d("HomeFragment", "Nome = $name, Age = $age")


                // Utilizza le coordinate formattate per le operazioni successive
                userViewModel.updateUser(name, age, location, latitude, longitude)


                userViewModel.users.observe(viewLifecycleOwner) {

                    val MAX_DISTANCE = 5.0 // in chilometri

                    it.let{ users ->
                        for(user in users) {
                            val distance = calculateDistance(
                                latitude,
                                longitude,
                                user.latitude,
                                user.longitude
                            )
                            // Stampa le coordinate nel log
                            Log.d("HomeFragment", "Distanza = $distance")

                            if (distance <= MAX_DISTANCE) {
                                //ichiarare filteredUsers.
                                //filteredUsers.clear()
                                if(user !in filteredUsers)
                                    filteredUsers.add(user)
                                // Stampa le coordinate nel log
                                Log.d("HomeFragment", "filteredusers = $filteredUsers")

                            }
                        }
                    }

                }





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



    fun calculateDistance(
        lat1: Double, lon1: Double, // Coordinate del primo punto
        lat2: Double, lon2: Double  // Coordinate del secondo punto
    ): Double {
        val earthRadius = 6371 // Raggio medio della Terra in chilometri

        // Converti le coordinate in radianti
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // Calcola la differenza tra le latitudini e le longitudini
        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        // Applica la formula di Haversine
        val a = sin(dLat/2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon/2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        return distance
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

        // chiamo il metodo del viewModel per prendere tutti gli utenti
        userViewModel.getAllUsers()

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
        }else {
            // L'utente è autenticato, verifica le autorizzazioni della posizione
            checkLocationPermission()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 123
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
            
            val adapter = PostAdapter(it)
            recyclerView.adapter = adapter
        }
        userViewModel.currentUser.observe(viewLifecycleOwner) {

            name = it.name
            age = it.age
            location = it.location
        }
    }

}