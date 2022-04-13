package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import cn.zeshawn.kaitobot.util.isAlphabet
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource
import com.j256.ormlite.stmt.QueryBuilder
import com.j256.ormlite.table.DatabaseTable
import java.io.File


object WordData : DataFileBase(File("${KaitoMind.root}/data", "WordData.db")) {
    private lateinit var connSource: JdbcPooledConnectionSource
    private lateinit var dao: Dao<Word, Int>
    override fun load() {
        Class.forName("org.sqlite.JDBC")
        connSource = JdbcPooledConnectionSource("jdbc:sqlite:${KaitoMind.root}/data/WordData.db")
        connSource.setMaxConnectionAgeMillis(5 * 60 * 1000)
        dao = DaoManager.createDao(connSource, Word::class.java)

    }

    override fun save() {

    }

    override fun init() {

    }

    fun getRandomWord(rank: VocabularyRank): Word {
        val bookId = rank.rank
        val words = dao.queryForEq("bookId", bookId)
        return words.random()
    }

    fun getWordsByLength(length:Int):MutableSet<String>{
        return buildSet {
            dao.queryRaw("SELECT word FROM word_dict WHERE LENGTH(word)=$length").results.forEach {
                if (it[0].isAlphabet())
                    add(it[0])
            }
        }.toMutableSet()
    }

}


enum class VocabularyRank(val rank: Int, val desc: String = "") {
    CET4(1, "四级"),
    CET6(2, "六级"),
    POSTGRADUATE(3, "研究生考试"),
    MAJOR4(4, "专四"),
    MAJOR8(5, "专八"),
    IELTS(6, "雅思"),
    TOEFL(7, "托福"),
    JUNIOR(8, "中考"),
    GAOKAO(9, "高考"),
    PEP31(10, "人教版小学英语-三年级上册"),
    PEP71(11, "人教版初中英语-七年级上册"),
    PEPGaozhong(12, "人教版高中英语-必修"),
    Cuzhong(13, "初中英语词汇"),
    Gaozhong(14, "高中英语词汇"),
    BEC(15, "商务英语词汇"),
}


@DatabaseTable(tableName = "word_dict")
class Word {
    @DatabaseField(id = true)
    var id: Int = 0

    @DatabaseField
    var word: String = ""

    @DatabaseField
    var pos: String = ""

    @DatabaseField
    var tran: String = ""

    @DatabaseField
    var bookId: Int = 0

    constructor() {
        // ORMLite needs a no-arg constructor
    }

    constructor(id: Int, word: String, pos: String, tran: String, bookId: Int) {
        this.id = id
        this.word = word
        this.pos = pos
        this.tran = tran
        this.bookId = bookId
    }


}