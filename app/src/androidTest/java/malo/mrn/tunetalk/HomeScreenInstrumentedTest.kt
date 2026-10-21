package malo.mrn.tunetalk

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import malo.mrn.tunetalk.domain.model.Genre
import malo.mrn.tunetalk.domain.model.RadioChannel
import malo.mrn.tunetalk.presentation.AppUiState
import malo.mrn.tunetalk.presentation.home.HomeScreen
import malo.mrn.tunetalk.ui.theme.TunetalkTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysHomeAndCatalogStationsAfterRegistration() {
        val genre = Genre(id = "rock", name = "Rock")
        val channel = RadioChannel(
            id = "station-1",
            name = "Rock FM",
            genre = "Rock",
            description = "Classic rock",
            streamUrl = "https://radio.example.com/stream",
            artworkUrl = null
        )

        composeRule.setContent {
            TunetalkTheme {
                HomeScreen(
                    state = AppUiState(
                        genres = listOf(genre),
                        selectedGenre = genre,
                        channels = listOf(channel)
                    ),
                    onSelectGenre = {},
                    onJoinChannel = {},
                    onRefreshCatalog = {},
                    onLogout = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithText(context.getString(R.string.home_title)).assertIsDisplayed()
        composeRule.onNodeWithText(channel.name).assertIsDisplayed()
    }
}
