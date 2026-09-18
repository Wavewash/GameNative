package app.gamenative.gamefixes

import app.gamenative.data.GameSource

/**
 * Batman: Arkham Asylum GOTY Edition (Steam 35140)
 */
val STEAM_Fix_35140: KeyedGameFix = KeyedWineEnvVarFix(
    gameSource = GameSource.STEAM,
    gameId = "35140",
    envVarsToSet = mapOf(
        "SCUDO_OPTIONS" to "QuarantineSizeMb=0:DeallocationTypeMismatch=0:ZeroContents=0",
    ),
)
