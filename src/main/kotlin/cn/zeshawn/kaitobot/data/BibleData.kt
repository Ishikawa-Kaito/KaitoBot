package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource
import com.j256.ormlite.table.DatabaseTable
import java.io.File

object BibleData : DataFileBase(File("${KaitoMind.root}/data", "bible.db")) {
    private lateinit var connSource: JdbcPooledConnectionSource
    private lateinit var dao: Dao<Bible, Int>
    override fun load() {
        Class.forName("org.sqlite.JDBC")
        connSource = JdbcPooledConnectionSource("jdbc:sqlite:${KaitoMind.root}/data/bible.db")
        connSource.setMaxConnectionAgeMillis(5 * 60 * 1000)
        dao = DaoManager.createDao(connSource, Bible::class.java)

    }

    override fun save() {

    }

    override fun init() {

    }


    fun getRandomBibleByGroup(groupId: Long): Bible {
        val bibles = dao.queryForEq("groupId", groupId)
        return bibles.random()
    }


    fun addNewBible(bible: Bible): Boolean {
        return dao.create(bible) == 1
    }

    fun isBibleExisted(hash: String): Boolean {
        val bibles = dao.queryForEq("imageHash", hash)
        return bibles.isNotEmpty()
    }


}

@DatabaseTable(tableName = "bible")
class Bible {
    @DatabaseField(id = true)
    var id: Int = 0

    @DatabaseField
    var groupId: Long = 0L

    @DatabaseField
    var uploaderId: Long = 0L

    @DatabaseField
    var imageHash: String = ""

    @DatabaseField
    var imagePath: String = ""

    constructor() {
        // ORMLite needs a no-arg constructor
    }

    constructor(id: Int, groupId: Long, uploaderId: Long, imageHash: String, imagePath: String) {
        this.id = id
        this.groupId = groupId
        this.uploaderId = uploaderId
        this.imageHash = imageHash
        this.imagePath = imagePath
    }


}