package app.gamenative.gamefixes

import android.content.Context
import app.gamenative.data.GameSource
import com.winlator.container.Container
import com.winlator.core.envvars.EnvVars
import timber.log.Timber
import java.io.File

/**
 * Batman: Arkham Asylum GOTY Edition (Steam 35140)
 *
 * - Automatically deletes stale/corrupted UserEngine.ini and BmEngine.ini files from AppData/Documents so UE3 regenerates clean defaults.
 * - Sets SCUDO_OPTIONS="QuarantineSizeMb=0:DeallocationTypeMismatch=0:ZeroContents=0" to prevent 32-bit Bionic memory allocator crashes.
 */
val STEAM_Fix_35140: KeyedGameFix = object : KeyedGameFix {
    override val gameSource = GameSource.STEAM
    override val gameId = "35140"

    override fun apply(
        context: Context,
        gameId: String,
        installPath: String,
        installPathWindows: String,
        container: Container,
    ): Boolean {
        return try {
            var changed = false

            val envVars = EnvVars(container.envVars)
            if (!envVars.has("SCUDO_OPTIONS") || envVars.get("SCUDO_OPTIONS") != "QuarantineSizeMb=0:DeallocationTypeMismatch=0:ZeroContents=0") {
                envVars.put("SCUDO_OPTIONS", "QuarantineSizeMb=0:DeallocationTypeMismatch=0:ZeroContents=0")
                changed = true
            }

            // Clean up any stale/corrupted AppData INI files from previous runs
            val configDirs = mutableListOf<File>(
                File(container.getRootDir(), "home/xuser/.wine/drive_c/users/xuser/My Documents/Square Enix/Batman Arkham Asylum GOTY/BmGame/Config"),
                File(container.getRootDir(), "home/xuser/.wine/drive_c/users/xuser/Documents/Square Enix/Batman Arkham Asylum GOTY/BmGame/Config")
            )

            val sharedDir = File(context.filesDir, "imagefs_shared/home")
            if (sharedDir.exists()) {
                sharedDir.listFiles()?.forEach { userHome ->
                    if (userHome.isDirectory) {
                        val configDir1 = File(userHome, ".wine/drive_c/users/xuser/My Documents/Square Enix/Batman Arkham Asylum GOTY/BmGame/Config")
                        val configDir2 = File(userHome, ".wine/drive_c/users/xuser/Documents/Square Enix/Batman Arkham Asylum GOTY/BmGame/Config")
                        if (!configDirs.contains(configDir1)) configDirs.add(configDir1)
                        if (!configDirs.contains(configDir2)) configDirs.add(configDir2)
                    }
                }
            }

            for (configDir in configDirs) {
                if (configDir.exists()) {
                    listOf("UserEngine.ini", "BmEngine.ini").forEach { iniName ->
                        val iniFile = File(configDir, iniName)
                        if (iniFile.exists()) {
                            try {
                                iniFile.delete()
                                changed = true
                                Timber.tag("GameFixes").i("Deleted stale $iniName at ${iniFile.path}")
                            } catch (e: Exception) {
                                Timber.tag("GameFixes").e(e, "Failed to delete stale $iniName")
                            }
                        }
                    }
                }
            }

            if (changed) {
                container.envVars = envVars.toString()
                container.saveData()
                Timber.tag("GameFixes").i("Applied Batman: Arkham Asylum fix (Deleted stale INIs, SCUDO_OPTIONS set)")
            }
            true
        } catch (e: Exception) {
            Timber.tag("GameFixes").e(e, "Failed to apply Batman: Arkham Asylum fix")
            false
        }
    }
}
