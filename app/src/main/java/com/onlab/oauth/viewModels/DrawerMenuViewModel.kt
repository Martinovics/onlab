package com.onlab.oauth.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlab.oauth.classes.ConnectionRepository
import com.onlab.oauth.interfaces.IConnectionService


class DrawerMenuViewModel: ViewModel() {

    companion object {
        const val tag = "DrawerMenuViewModel"  // todo use this

        fun createFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DrawerMenuViewModel() as T
                }
            }
        }
    }

    val connectionTitles = MutableLiveData<Map<Int, String>>()  // todo rename these
    val showToastMessage = MutableLiveData<String>()
    val closeDrawerEvent = MutableLiveData<Unit>()  // Esemény a fiók bezárására


    fun init() {
        updateNavigationMenuItemTitles()
    }


    private fun updateNavigationMenuItemTitles() {
        val titles = mutableMapOf<Int, String>()
        for (kv in ConnectionRepository.registeredEntries) {
            val connectionService = kv.value
            if (connectionService.isLoggedIn()) {
                titles[connectionService.menuItemId] = "Disconnect ${connectionService.title}"
            } else {
                titles[connectionService.menuItemId] = "Connect ${connectionService.title}"
            }
        }
        connectionTitles.value = titles
    }

    fun onMenuItemSelected(menuItemId: Int) {
        ConnectionRepository.registeredEntries.find { it.value.menuItemId == menuItemId }?.value?.let { connectionService ->
            connectionDrawerMenuListener(connectionService)
        }
    }

    private fun connectionDrawerMenuListener(connectionService: IConnectionService) {
        if (connectionService.isLoggedIn()) {
            connectionService.signOut(
                callback_success = {
                    updateNavigationMenuItemTitles()
                    showToastMessage.value = "Disconnected from ${connectionService.title}"
                },
                callback_fail = {
                    showToastMessage.value = "Disconnect failed"
                }
            )
        } else {
            connectionService.signIn(
                callback_success = {
                    updateNavigationMenuItemTitles()
                    // todo list root folder
                    showToastMessage.value = "Connected to ${connectionService.title}"
                },
                callback_fail = {
                    showToastMessage.value = "Connection failed"
                }
            )
        }
    }

    fun closeDrawer() {
        closeDrawerEvent.value = Unit
    }
}
