package malo.mrn.tunetalk

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import malo.mrn.tunetalk.data.local.SessionStore
import org.junit.Rule
import org.junit.Test

class RegistrationFlowInstrumentedTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun successfulAccountCreationOpensHomeInsteadOfCrashing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext

        if (Build.VERSION.SDK_INT >= 37) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            )
        }

        runBlocking {
            SessionStore(context).clear()
        }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            composeRule.onNodeWithText(context.getString(R.string.auth_switch_to_register)).performClick()
            composeRule.onNodeWithText("Ton pseudo").performTextInput("Registration UI")
            composeRule.onNodeWithText("Adresse e-mail")
                .performTextInput("registration-ui-${System.currentTimeMillis()}@example.com")
            composeRule.onNodeWithText("Mot de passe").performTextInput("TuneTalk2026")
            composeRule.onNodeWithText(context.getString(R.string.auth_register_button)).performClick()

            composeRule.waitUntil(timeoutMillis = 30_000) {
                composeRule.onAllNodesWithText(context.getString(R.string.home_title))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithText(context.getString(R.string.home_title)).assertIsDisplayed()
        } finally {
            scenario.close()
            runBlocking {
                SessionStore(context).clear()
            }
        }
    }
}
