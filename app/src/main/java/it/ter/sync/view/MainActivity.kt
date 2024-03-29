package it.ter.sync.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import it.ter.sync.R
import it.ter.sync.databinding.ActivityMainBinding
import it.ter.sync.viewmodel.UserViewModel
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import it.ter.sync.viewmodel.MessageViewModel
import it.ter.sync.viewmodel.NotificationViewModel

class MainActivity : AppCompatActivity() {
    private val TAG: String = javaClass.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var imageUser: ImageView
    private lateinit var nameUser: TextView

    private lateinit var userViewModel: UserViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var messageViewModel: MessageViewModel
    private val CHANNEL_ID = "channel_id_01"
    private val notification_id = 101

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        initObservers()

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.accountFragment, R.id.chatFragment, R.id.loginFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.signUpFragment) {
                binding.appBarMain.badgeLayout.visibility = View.INVISIBLE
            } else {
                binding.appBarMain.badgeLayout.visibility = View.VISIBLE
            }

            binding.appBarMain.notifyMenu.isClickable = destination.id != R.id.notificationFragment
            binding.appBarMain.plusIcon.isClickable = destination.id != R.id.groupFragment
        }

        binding.appBarMain.notifyMenu.setOnClickListener {
            navController.navigate(R.id.notificationFragment)
        }

        binding.appBarMain.plusIcon.setOnClickListener {
            navController.navigate(R.id.groupFragment)
        }

        val headerView = navView.getHeaderView(0)
        imageUser = headerView.findViewById(R.id.imageUser)
        nameUser = headerView.findViewById(R.id.textNameUser)

        // Prendo le notifiche non ancora visualizzate
        notificationViewModel.retrieveNotificationsNotDisplayed()

        userViewModel.getUserInfo()
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Se mi trovo nel loginFragment non posso accedere al menù
        if (navController.currentDestination?.id == R.id.loginFragment) {
            return false
        }
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Il menu laterale è aperto, chiudi il menu
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText || view is TextView) {
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
            currentFocusView.clearFocus()
        }
    }

    fun onChangeUser(){
        notificationViewModel.retrieveNotificationsNotDisplayed()

        userViewModel.getUserInfo()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val title = "title"
            val description_text = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, title, importance).apply {
                description = description_text
            }
            val notificationManager: NotificationManager =
                this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(DescriptionText:String,Sender: String){
        val builder= NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_emoji_people_24)
            .setContentTitle(Sender)
            .setContentText(DescriptionText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notification_id,builder.build())
        }

    }

    private fun initObservers() {
        Log.i(TAG, "Registering Observers: ViewModel? $notificationViewModel")
        notificationViewModel.notificationListNotDisplayed.observe(this) {
            if(it.isNotEmpty()) {
                binding.appBarMain.badgeNotification.visibility = View.VISIBLE
                binding.appBarMain.badgeNotification.text = it.size.toString()
            } else {
                binding.appBarMain.badgeNotification.visibility = View.INVISIBLE
            }
            for(item in it){
                createNotificationChannel()
                item.notifierName?.let { it1 -> sendNotification(item.text, it1) }
            }

        }
        userViewModel.currentUser.observe(this) {
            nameUser.text = it?.name

            Glide.with(this)
                .load(it?.image)
                .error(R.mipmap.ic_launcher)
                .into(imageUser)
        }
    }
}

