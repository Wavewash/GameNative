package app.gamenative.gamefixes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.gamenative.service.SteamService
import com.winlator.container.Container
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.io.path.createTempDirectory

@RunWith(RobolectricTestRunner::class)
class BatmanGameFixTest {
    private lateinit var context: Context
    private lateinit var container: Container

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        container = mockk(relaxed = true)
        mockkObject(SteamService.Companion)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun applyFor_appliesBatmanFixes_whenInstalled() {
        val installDir = createTempDirectory(prefix = "batman-fix").toFile()
        val rootDir = createTempDirectory(prefix = "root-fix").toFile()
        every { SteamService.getAppDirPath(35140) } returns installDir.absolutePath
        every { container.envVars } returns ""
        every { container.envVars = any() } just runs
        every { container.getRootDir() } returns rootDir
        every { container.saveData() } just runs

        GameFixesRegistry.applyFor(context, "STEAM_35140", container)

        verify(exactly = 1) { container.setEnvVars("SCUDO_OPTIONS=QuarantineSizeMb=0:DeallocationTypeMismatch=0:ZeroContents=0") }
        verify(exactly = 1) { container.saveData() }
    }
}
