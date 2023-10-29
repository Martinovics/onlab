package com.onlab.oauth.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlab.oauth.classes.ConnectionRepository
import com.onlab.oauth.interfaces.ICallback
import com.onlab.oauth.interfaces.IConnectionService


class DrawerMenuViewModel: ViewModel() {

    val connectionTitles = MutableLiveData<Map<Int, String>>()
    val toastMessage = MutableLiveData<String>()
    val closeDrawer = MutableLiveData<Unit>()

    companion object {
        const val tag = "DrawerMenuViewModel"

        fun createFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DrawerMenuViewModel() as T
                }
            }
        }
    }


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
                object : ICallback {
                    override fun onSuccess() {
                        updateNavigationMenuItemTitles()
                        // todo remove folders of this source
                        toastMessage.value = "Disconnected from ${connectionService.title}"
                    }
                    override fun onFailure() {
                        toastMessage.value = "Disconnect failed"
                    }
                })
        } else {
            connectionService.signIn(
                object : ICallback {
                    override fun onSuccess() {
                        updateNavigationMenuItemTitles()
                        // todo list root folder
                        toastMessage.value = "Connected to ${connectionService.title}"
                    }
                    override fun onFailure() {
                        toastMessage.value = "Disconnect failed"
                    }
                })
        }
    }

    fun closeDrawer() {
        closeDrawer.value = Unit
    }
}
