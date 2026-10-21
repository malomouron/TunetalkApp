package malo.mrn.tunetalk.data.remote

import java.net.URI
import java.util.Locale

internal object ApiNetworkPolicy {
    fun requiresLocalNetworkPermission(baseUrl: String): Boolean {
        val host = runCatching { URI(baseUrl).host }
            .getOrNull()
            ?.removeSurrounding("[", "]")
            ?.lowercase(Locale.ROOT)
            ?: return false

        if (host == "localhost"
            || host.endsWith(".localhost")
            || host.endsWith(".local")
            || host.endsWith(".lan")
            || host.endsWith(".internal")
            || host.endsWith(".home.arpa")
        ) {
            return true
        }

        if (':' in host) {
            return host == "::1"
                || host.startsWith("fc")
                || host.startsWith("fd")
                || host.startsWith("fe80:")
        }

        val octets = host.split('.')
            .mapNotNull(String::toIntOrNull)
            .takeIf { parts -> parts.size == 4 && parts.all { it in 0..255 } }
            ?: return '.' !in host

        return octets[0] == 10
            || octets[0] == 127
            || (octets[0] == 172 && octets[1] in 16..31)
            || (octets[0] == 192 && octets[1] == 168)
            || (octets[0] == 169 && octets[1] == 254)
    }
}
