package malo.mrn.tunetalk

import android.Manifest
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import malo.mrn.tunetalk.data.remote.LoginRequestDto
import malo.mrn.tunetalk.data.remote.TunetalkApi
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class ApiConnectivityInstrumentedTest {
    private val api = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TunetalkApi::class.java)

    @Before
    fun grantLocalNetworkPermission() {
        if (Build.VERSION.SDK_INT >= 37) {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val packageNames = setOf(
                instrumentation.context.packageName,
                instrumentation.targetContext.packageName
            )

            packageNames.forEach { packageName ->
                instrumentation.uiAutomation.grantRuntimePermission(
                    packageName,
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                )
            }
        }
    }

    @Test
    fun catalogIsReachableFromTheEmulator() = runBlocking {
        val genres = api.getGenres()

        assertEquals(7, genres.size)
    }

    @Test
    fun loginEndpointRespondsThroughTheAndroidNetworkStack() = runBlocking {
        try {
            api.login(
                LoginRequestDto(
                    email = "connectivity-probe@invalid.example",
                    password = "not-a-valid-password"
                )
            )
            fail("An unknown test account should not authenticate.")
        } catch (exception: HttpException) {
            assertEquals(401, exception.code())
        }
    }
}
