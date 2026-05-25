package com.karthik.lifeos.tracker.tracker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

/**
 * Determines whether a given package is a game.
 *
 * Strategy:
 * 1. Check ApplicationInfo.category == CATEGORY_GAME (API 26+, which is our minSdk).
 * 2. Fall back to a manually maintained allowlist for popular games that
 *    don't declare the game category in their manifest.
 *
 * Limitation: CATEGORY_GAME depends on the developer or the store (Google Play)
 * setting this metadata. Many games don't declare it, hence the allowlist.
 *
 * Results are cached to avoid repeated PackageManager lookups.
 */
class GameDetector(private val context: Context) {

    companion object {
        private const val TAG = "GameDetector"

        /**
         * Manual allowlist for popular games that may not declare CATEGORY_GAME.
         * Add/remove packages as needed.
         */
        val GAME_ALLOWLIST = setOf(
            // Casual / Runner
            "com.kiloo.subwaysurf",                 // Subway Surfers
            "com.imangi.templerun2",                // Temple Run 2
            "com.halfbrick.fruitninjafree",         // Fruit Ninja
            "com.king.candycrushsaga",
            "com.playdead.limbo.full",    // Limbo

            // Battle Royale / Shooter
            "com.tencent.ig",                       // PUBG Mobile
            "com.activision.callofduty.shooter",    // Call of Duty Mobile
            "com.dts.freefiremax",                   // Free Fire
            "com.epicgames.fortnite",               // Fortnite

            // Strategy / MOBA
            "com.supercell.clashofclans",           // Clash of Clans
            "com.supercell.clashroyale",            // Clash Royale
            "com.mobile.legends",                   // Mobile Legends
            "com.riotgames.league.wildrift",        // Wild Rift

            // Racing
            "com.gameloft.android.ANMP.GlsoftAsphalt9", // Asphalt 9
            "com.naturalmotion.customstreetracer2",      // CSR Racing 2

            // Puzzle / Board
            "com.etermax.preguntados.lite",         // Trivia Crack
            "com.chess",                            // Chess.com
            "org.lichess.mobileapp",                // Lichess

            // Minecraft & sandbox
            "com.mojang.minecraftpe",               // Minecraft
            "com.roblox.client",                    // Roblox

            // Other popular
            "com.innersloth.spacemafia",            // Among Us
            "com.nianticlabs.pokemongo",            // Pokémon GO
            "com.supercell.brawlstars",             // Brawl Stars
            "com.thegamekitchen.blasphemousmobile",
            "com.eapublishing.dhsw.paid"
        )
    }

    /** Cache: packageName → isGame. Avoids hitting PackageManager on every scan. */
    private val cache = HashMap<String, Boolean>()

    fun isGame(packageName: String): Boolean {
        return cache.getOrPut(packageName) { detectGame(packageName) }
    }

    private fun detectGame(packageName: String): Boolean {
        // Check the manual allowlist first (fast path)
        if (packageName in GAME_ALLOWLIST) return true

        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            appInfo.category == ApplicationInfo.CATEGORY_GAME
        } catch (e: Exception) {
            Log.w(TAG, "Failed to detect game for $packageName: ${e.message}")
            false
        }
    }

    /** Resolve a human-readable app label from a package name. */
    fun getAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            // Can happen if the app was uninstalled OR if it's our own app 
            // and there's a transient PackageManager issue.
            Log.w(TAG, "Failed to get app name for $packageName: ${e.message}")
            packageName
        }
    }
}
