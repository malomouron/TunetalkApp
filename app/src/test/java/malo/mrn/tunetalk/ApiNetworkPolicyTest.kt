package malo.mrn.tunetalk

import malo.mrn.tunetalk.data.remote.ApiNetworkPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiNetworkPolicyTest {
    @Test
    fun localDevelopmentEndpointsRequireLocalNetworkAccess() {
        listOf(
            "http://10.0.2.2:5269/",
            "http://localhost:5269/",
            "http://192.168.1.10:5269/",
            "http://172.20.0.5:5269/",
            "http://tune-api.local/"
        ).forEach { baseUrl ->
            assertTrue(baseUrl, ApiNetworkPolicy.requiresLocalNetworkPermission(baseUrl))
        }
    }

    @Test
    fun publicEndpointsDoNotRequireLocalNetworkAccess() {
        listOf(
            "https://api.example.com/",
            "https://203.0.113.10/"
        ).forEach { baseUrl ->
            assertFalse(baseUrl, ApiNetworkPolicy.requiresLocalNetworkPermission(baseUrl))
        }
    }
}
