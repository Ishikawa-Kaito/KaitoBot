package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.CallbackCommand
import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.command.base.Disabled
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.FurryPicService
import cn.zeshawn.kaitobot.util.toChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import java.util.*
import kotlin.concurrent.schedule

@Disabled
object FurryPicCommand : ChatCommand, CallbackCommand {

    override val name: String
        get() = "furrypic"
    override val alias: List<String>
        get() = listOf("srk", "furry", "来图")
    override val permission: UserRole
        get() = UserRole.USER


    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        var nsfwFlag = false
        if (args.size == 1 && args[0] in listOf("?", "help", "？"))
            return getHelp().toChain()
        val imageStream = if (args.isEmpty()) {// 随机来张sfw图
            FurryPicService.randomPic(2)
        } else if (args.size == 1) {
            if (args[0] != "s") {
                FurryPicService.searchPic(2, args[0])
            } else {
                nsfwFlag = true
                FurryPicService.randomPic(1)
            }
        } else {
            val keyword = if (args[0] == "s") {
                nsfwFlag = true
                args.subList(1, args.size).joinToString("")
            } else {
                args.joinToString("")

            }
            FurryPicService.searchPic(1, keyword)
        }
        val image = if (imageStream != null) {
            event.subject.uploadImage(imageStream)
        } else {
            null
        }
        return if (image != null) {
            if (nsfwFlag) {
                buildMessageChain {
                    +FlashImage(image.imageId)
                }
            } else {
                buildMessageChain {
                    +image
                }
            }
        } else {
            "操作失败".toChain()
        }
    }

    override fun getHelp(): String {
        return """
            获取一张FA上的图片。
            /srk    [s,允许nsfw]    随机获得一张图，不保证任何可观赏性。
            /srk    [s,允许nsfw]    [关键词]    搜索获得一张图，仅支持英文。
            
            示例：
            /srk    随机获得一张sfw图。
            /srk    s    随机获得一张nsfw图，闪照形式。
            /srk    kamui    获得一张神威的sfw图。
            /srk    s    kamui    获得一张神威的nsfw图。
        """.trimIndent()
    }

    override fun callback(receipt: MessageReceipt<Contact>) {
        Timer().schedule(30000L) {
            runBlocking {
                receipt.recall()
            }
        }
    }
}