package cn.zeshawn.kaitobot.service

import org.junit.jupiter.api.Test


internal class TwitterServiceTest {

    @Test
    fun getTweetTest() {
        TwitterService.getTweet("https://twitter.com/UnitedWeFanPod/status/1499831397724442626")
    }
}