package com.onlab.oauth.viewModels

import android.util.Log
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlab.oauth.classes.ConnectionRepository
import com.onlab.oauth.interfaces.IConnectionService
import com.onlab.oauth.interfaces.IMainActivityUIManager


class DrawerMenuViewModel(
    private val uiManager: IMainActivityUIManager
    ): ViewModel() {

    companion object {
        const val tag = "DrawerMenuViewModel"

        fun createFactory(uiManager: IMainActivityUIManager): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DrawerMenuViewModel(
                        uiManager
                    ) as T
                }
            }
        }
    }


    fun init() {
        initNavigationView()
    }


    private fun initNavigationView() {

        // set connection titles
        for (kv in ConnectionRepository.registeredEntries) {
            val connectionService = kv.value
            val drawerMenuItem = uiManager.binding.drawerMenu.menu.findItem(connectionService.menuItemId)
            if (connectionService.isLoggedIn()) {
                drawerMenuItem.title = "Disconnect ${connectionService.title}"
            } else {
                drawerMenuItem.title = "Connect ${connectionService.title}"
            }
        }

        // set connection listeners
        uiManager.binding.drawerMenu.setNavigationItemSelectedListener { menuItem ->
            var isListenerSet = false
            for ((_, connectionService) in ConnectionRepository.registeredEntries) {
                if (connectionService.menuItemId == menuItem.itemId) {
                    connectionDrawerMenuListener(connectionService)
                    isListenerSet = true
                }
            }
            isListenerSet
        }
    }


    private fun connectionDrawerMenuListener(connectionService: IConnectionService) {
        val drawerMenuItem = uiManager.binding.drawerMenu.menu.findItem(connectionService.menuItemId)
        if (connectionService.isLoggedIn()) {
            connectionService.signOut(
                callback_success = {
                    drawerMenuItem.title = "Connect to ${connectionService.title}"
                    Log.i(tag, "Disconnected from ${connectionService.title}")
                    uiManager.makeToast("Disconnected from ${connectionService.title}")
                },
                callback_fail = {
                    Log.e(tag, "Disconnected from ${connectionService.title} failed")
                    uiManager.makeToast("Disconnect failed")
                }
            )
        } else {
            connectionService.signIn(
                callback_success = {
                    drawerMenuItem.title = "Disconnect from ${connectionService.title}"
                    // todo list root folders
                    Log.d(tag, "Connected to ${connectionService.title}")
                    uiManager.makeToast("Connected to ${connectionService.title}")
                },
                callback_fail = {
                    Log.e(tag, "Connection to ${connectionService.title} failed")
                    uiManager.makeToast("Connection failed")
                }
            )
        }
    }


    fun closeDrawer(): Boolean {
        if (uiManager.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            uiManager.binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }
}
