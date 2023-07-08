package it.ter.sync

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import it.ter.sync.database.repository.UserRepository
import it.ter.sync.database.user.UserData
import it.ter.sync.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class UserViewModelTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val userViewModel: UserViewModel = UserViewModel(context.applicationContext as Application)
    private val userRepository: UserRepository = UserRepository(context.applicationContext as Application)
    private val latch = CountDownLatch(1)

    @Before
    fun register() {
        userViewModel.register("test","11/8/1998", "example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun testRegister() {
        assertEquals("Success", userViewModel.registrationResult.value)
    }

    @Test
    fun testUserLoggedIn(){
        userViewModel.login("example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)
        assertEquals(true, userViewModel.isUserLoggedIn())
        userViewModel.logout()
        latch.await(2, TimeUnit.SECONDS)
        assertEquals(false, userViewModel.isUserLoggedIn())
    }

    @Test
    fun getUserInfo(){
        userViewModel.login("example@gmail.com","password")
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.getUserInfo()
        latch.await(2, TimeUnit.SECONDS)

        val user = userViewModel.currentUser.value

        assertEquals("test", user?.name)
        assertEquals("11/8/1998", user?.age)
        assertEquals("example@gmail.com", user?.email)

        var userLocal = UserData()
        CoroutineScope(Dispatchers.Main).launch {
            userLocal = userRepository.getUserByUid(user?.uid ?: "")!!
        }
        latch.await(2, TimeUnit.SECONDS)
        assertEquals("test", userLocal.name)
        assertEquals("11/8/1998", userLocal.age)
        assertEquals("example@gmail.com", userLocal.email)
    }
    @Test
    fun updateUserPosition(){
        userViewModel.login("example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.updateUserPosition(41.9028, 12.4964)
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.getUserInfo()
        latch.await(2, TimeUnit.SECONDS)

        val user = userViewModel.currentUser.value

        assertEquals("Roma", user?.location)
    }

    @After
    fun deleteUser() {
        userViewModel.login("example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.deleteAccount()
        latch.await(2, TimeUnit.SECONDS)
    }
}

