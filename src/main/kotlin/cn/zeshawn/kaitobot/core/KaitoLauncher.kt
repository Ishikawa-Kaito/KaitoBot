package cn.zeshawn.kaitobot.core

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.NeteaseMusicCommand
import cn.zeshawn.kaitobot.data.DataFiles
import cn.zeshawn.kaitobot.manager.CommandManager
import org.reflections.Reflections

//启动前的准备活动
object KaitoLauncher {
    fun launch(){
        KaitoMind.KaitoLogger.info {
            """

 __  ___      ___       __  .___________.  ______   
|  |/  /     /   \     |  | |           | /  __  \  
|  '  /     /  ^  \    |  | `---|  |----`|  |  |  | 
|    <     /  /_\  \   |  |     |  |     |  |  |  | 
|  .  \   /  _____  \  |  |     |  |     |  `--'  | 
|__|\__\ /__/     \__\ |__|     |__|      \______/  
                                                    
    
            """.trimIndent()
        }
        // 加载数据文件
        DataFiles.setup()

        // 载入全部命令
        CommandManager.autoSetup("cn.zeshawn.kaitobot.command")



    }
}