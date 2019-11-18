package com.crepetete.steamachievements.db

import com.crepetete.steamachievements.util.TestUtil
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.runner.AndroidJUnit4
import com.crepetete.steamachievements.BuildConfig.TEST_USER_ID
import com.crepetete.steamachievements.util.LiveDataTestUtil.getValue
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepoTest : DbTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertAndRead() {
        val repo = TestUtil.createPlayer(TEST_USER_ID)
        db.playerDao().insert(repo)
        val loaded = getValue(db.playerDao().getPlayerById(TEST_USER_ID))
        MatcherAssert.assertThat(loaded, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(loaded.steamId, CoreMatchers.`is`(TEST_USER_ID))
    }
}