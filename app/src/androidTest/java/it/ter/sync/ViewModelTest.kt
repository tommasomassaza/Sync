package it.ter.sync

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.notify.NotificationType
import it.ter.sync.viewmodel.MessageViewModel
import it.ter.sync.viewmodel.NotificationViewModel
import it.ter.sync.viewmodel.UserViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ViewModelTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val userViewModel: UserViewModel = UserViewModel(context.applicationContext as Application)
    private val messageViewModel: MessageViewModel = MessageViewModel(context.applicationContext as Application)
    private val notificationViewModel: NotificationViewModel = NotificationViewModel(context.applicationContext as Application)
    private val latch = CountDownLatch(1)

    @Before
    fun register() {
        userViewModel.register("test","11/8/1998", "example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)
        userViewModel.login("example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun testSendMessageAndNotification(){
        messageViewModel.retrieveMessages("fsjsjW0j7Ka1naftKvt19bZdiPY2", "Ettore", "test")
        latch.await(2, TimeUnit.SECONDS)

        messageViewModel.sendMessage("Ciao, come va?","fsjsjW0j7Ka1naftKvt19bZdiPY2", "","")
        latch.await(2, TimeUnit.SECONDS)

        val messageId = messageViewModel.messageList.value?.get(0)?.uid
        assertEquals("Ciao, come va?", messageViewModel.messageList.value?.get(0)?.text)

        messageViewModel.deleteMessage(messageId ?: "")
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.logout()
        userViewModel.login("ettore@gmail.com", "ettore")
        latch.await(2, TimeUnit.SECONDS)

        notificationViewModel.retrieveNotificationsNotDisplayed()
        latch.await(2, TimeUnit.SECONDS)

        val notification = notificationViewModel.notificationListNotDisplayed.value?.get(0) ?: NotificationData()

        assertEquals("Ciao, come va?", notification.text)
        assertEquals(NotificationType.MESSAGE, notification.type)
        assertEquals("test", notification.notifierName)

        notificationViewModel.remove(notification)
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.logout()
        userViewModel.login("example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)

        messageViewModel.deleteMessage(messageId ?: "")
        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun testLike(){
        notificationViewModel.addLikeNotification("fsjsjW0j7Ka1naftKvt19bZdiPY2","test","")
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.logout()
        userViewModel.login("ettore@gmail.com", "ettore")
        latch.await(2, TimeUnit.SECONDS)

        notificationViewModel.retrieveNotificationsNotDisplayed()
        latch.await(2, TimeUnit.SECONDS)

        val notification = notificationViewModel.notificationListNotDisplayed.value?.get(0) ?: NotificationData()

        assertEquals("Ti ha Syncato", notification.text)
        assertEquals(NotificationType.LIKE, notification.type)
        assertEquals("test", notification.notifierName)

        userViewModel.logout()
        userViewModel.login("example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)

        notificationViewModel.deleteLikeNotification("fsjsjW0j7Ka1naftKvt19bZdiPY2")
        latch.await(2, TimeUnit.SECONDS)
    }

    @After
    fun deleteUser() {
        userViewModel.login("example@gmail.com", "password")
        latch.await(2, TimeUnit.SECONDS)

        userViewModel.deleteAccount()
        latch.await(2, TimeUnit.SECONDS)
    }
}