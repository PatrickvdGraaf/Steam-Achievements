package com.crepetete.steamachievements

import android.app.Application

/**
 * We use a separate App for tests to prevent initializing dependency injection.
 *
 * See [com.crepetete.steamachievements.util.SteamAchievementsTestRunner].
 */
class TestApp : Application()