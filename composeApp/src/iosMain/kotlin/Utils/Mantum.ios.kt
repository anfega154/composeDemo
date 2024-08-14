package Utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkFlagsReachable

actual class Mantum {
    @OptIn(ExperimentalForeignApi::class)
    actual fun isConnectedOrConnecting(): Boolean {
        val reachability = SCNetworkReachabilityCreateWithName(null, "www.google.com")
        var isConnected = false

        memScoped {
            val flags = alloc<UIntVar>()
            val gotFlags = SCNetworkReachabilityGetFlags(reachability, flags.ptr)
            val kSCNetworkFlagsConnectionRequired = 0u

            isConnected = gotFlags && (flags.value and kSCNetworkFlagsReachable != 0u) &&
                    (flags.value and kSCNetworkFlagsConnectionRequired == 0u)
        }

        return isConnected
    }
}