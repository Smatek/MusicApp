package pl.skolimowski.musicapp.network.watcher

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkStateWatcher {
    val networkState: StateFlow<NetworkState>
    fun register()
    fun unregister()
}

enum class NetworkState {
    AVAILABLE, UNAVAILABLE
}

@Singleton
class NetworkStateWatcherImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkStateWatcher {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow(getCurrentNetworkState())
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkState.value = NetworkState.AVAILABLE
        }

        override fun onLost(network: Network) {
            if (connectivityManager.activeNetwork == null) {
                 _networkState.value = NetworkState.UNAVAILABLE
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                              networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if(isConnected) {
                 _networkState.value = NetworkState.AVAILABLE
            } else if (connectivityManager.activeNetwork == null) {
                 _networkState.value = NetworkState.UNAVAILABLE
            }
        }
    }

    override fun register() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        _networkState.value = getCurrentNetworkState()
    }

    override fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            // Ignore: NetworkCallback was not registered or already unregistered
            // Log.w("NetworkStateWatcher", "NetworkCallback not registered or already unregistered.")
        }
    }

    private fun getCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return if (capabilities != null &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            NetworkState.AVAILABLE
        } else {
            NetworkState.UNAVAILABLE
        }
    }
} 