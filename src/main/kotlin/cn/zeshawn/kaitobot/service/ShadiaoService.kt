package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.data.CPStories

/**
 * https://shadiao.app/ 网站api的合集
 */
object ShadiaoService {


    /***
     * 渣男语录
     */
    fun getSweetNothings(): String {
        return HttpUtil.get("https://api.lovelive.tools/api/SweetNothings/1?genderType=F")
    }


    fun getCPStory(top: String, bottom: String): String {
        return CPStories.stories.toList().random().asText().replace("<攻>", top).replace("<受>", bottom)
    }
}