package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.WordleData
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.message.data.MessageChain
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


object WordleService {

    private val solverMap = mutableMapOf<Long,WordleSolver>()

    init {
        WordleData.load()
        if (WordleData.file.exists() && WordleData.file.isDirectory){
            WordleData.file.listFiles()!!.forEach {
                if (it.extension.lowercase() == "png") {
                    ImageIO.read(it).let { image ->
                        val charMeta = it.name.chunked(1)
                        WordleData.charData[image] = Pair(charMeta[0],charMeta[1].toInt())
                    }
                }
            }
        }
    }

    private fun getRows(respondImage: BufferedImage): MutableList<List<Pair<String,Int>>>{
        val rows = mutableListOf<List<Pair<String,Int>>>()
        val lines = buildList {
            for (i in 20..310 step 50) {
                val tempImg = respondImage.getSubimage(20,i,240,40)
                add(tempImg)
            }
        }
        lines.forEach {
            val words = buildList {
                for (i in 0..240 step 50) {
                    val word = it.getSubimage(i, 0, 40, 40)
                    add(ocr(word))
                }
            }
            rows.add(words)
        }
        return rows
    }

    private fun getRound(rows: MutableList<List<Pair<String,Int>>>):Int{
        return rows.filter { row->row.all { it.first != "" } }.size // 获取非空行行数即为当前回合数
    }

    suspend fun solve(message: MessageChain,id:Long):String{
        if (!message.contains(Image)) return ""
        val img = message.first { it is Image } as Image
        if (!isWorldeImage(img)) return ""
        val respondImage = ImageIO.read(HttpUtil.downloadBytes(img.queryUrl()).inputStream())
        val rows = getRows(respondImage)
        val round = getRound(rows)
        return WordleSolver().nextRound(rows,round)
    }

    private fun compareByte(imgA:BufferedImage, imgB:BufferedImage):Boolean{
        var diff = 0
        for (i in 0 until imgA.width) {
            for (j in 0 until imgA.height) {
                if (imgA.getRGB(i,j) != imgB.getRGB(i,j))
                    diff++
                if (diff > 20) return false
            }
        }

        return true
    }

    private fun isWorldeImage(img: Image):Boolean{
        if (img.isEmoji) return false
        if (img.width != 280 || img.height != 330){
            return false
        }
        return true
    }

    private fun ocr(img:BufferedImage): Pair<String,Int> {
        WordleData.charData.keys.forEach {
            if (compareByte(it,img)){
                return WordleData.charData[it]!!
            }
        }
        return Pair("",0)
    }


    fun BufferedImage.toByteArray():ByteArray{
        val out = ByteArrayOutputStream()
        ImageIO.write(this,"PNG", out)
        return out.toByteArray()
    }

    fun getLength(respondImage: BufferedImage):Int{
        // imageWidth = letterWidth * length + (length-1) * padding + boarder
        // padding = 10 letterWidth = 40 boarder = 40
        return (respondImage.width - 30) / 50
    }

}


class WordleSolver{

    private val wrongLetters = mutableSetOf<String>() // grey letters
    private val untriedLetters = mutableSetOf<String>() // untried letters
    private val candidateWords = mutableSetOf<String>() // possible words

    private val greenLetters = mutableSetOf<Pair<String,Int>>() // green letters and position
    private val yellowLetters = mutableSetOf<Pair<String,Int>>() // yellow letters and position

    var round = 0 // which round now
    var lastAttempt = ""  // 有记录的尝试

    init {
        reset()
    }

    fun reset(){
        wrongLetters.clear()
        untriedLetters.clear()
        candidateWords.clear()
        candidateWords.addAll(WordleData.words)
        round = 0
        lastAttempt = ""
    }


    private fun getLetterCounter(valid:Boolean,words:MutableSet<String>):Map<String,Int>{
        val probabilities = mutableMapOf<String,Int>()
        words.forEach {
            it.forEach { char ->
                val letter = char.toString()
                if (valid || letter in untriedLetters){
                    if (probabilities.contains(letter)){
                        probabilities[letter] = probabilities[letter]!!.plus(1)
                    }else{
                        probabilities[letter] = 1
                    }
                }
            }
        }
        return probabilities
    }


    /**
     * 获得所有字母的词频
     */
    private fun getLetterFreq(words: MutableSet<String>):Map<String,Int>{
        return getLetterCounter(true,words)
    }

    /**
     * 获得有可能的字母的词频
     */
    private fun getLetterProbe(words: MutableSet<String>):Map<String,Int>{
        return getLetterCounter(false,words)
    }

    private fun isWordForbidden(word:String):Boolean{
        return word.any { it.toString() in wrongLetters }
    }

    private fun matchGreen(word: String):Boolean{
        return greenLetters.all {
            it.first == word[it.second-1].toString()
        }
    }

    private fun matchYellow(word: String):Boolean{
        return yellowLetters.all {
            it.first != word[it.second-1].toString() && it.first in word
        }
    }

    private fun getNextCandidateWords(){
        val removal = mutableSetOf<String>()

        candidateWords.addAll(WordleData.words)
        WordleData.words.forEach {
            if (isWordForbidden(it) || !matchGreen(it) || !matchYellow(it)){
                removal.add(it)
            }
        }
        candidateWords.removeAll(removal)
    }

    private fun guess():String{
        val probes = getLetterProbe(candidateWords)
        val freq = getLetterFreq(candidateWords)
        if (untriedLetters.size >1 && round < 6){
            val wordScore = mutableListOf<Triple<String,Int,Int>>()
            val wordList = WordleData.words
            wordList.forEach { word ->
                val letters = word.chunked(1).toSet()
                val untriedScore = letters.sumOf {
                     if (probes.contains(it)) probes[it]!! else 0
                }
                val freqScore = letters.sumOf {
                    freq[it]!!
                }
                wordScore.add(Triple(word,untriedScore,freqScore))

            }
            return wordScore.sortedWith(compareBy({-it.second},{-it.third},{it.first}))[0].first
        }
        else{
            return candidateWords.sortedWith(
                compareBy(
                    {-it.chunked(1).toSet().size},
                    {-it.sumOf {
                            letter -> freq[letter.toString()]!!
                        }
                    }
                )
            )[0]
        }
    }

    private fun pickUpAWord():String{
        getNextCandidateWords()
        println("left ${candidateWords.size} words")

        if (candidateWords.isEmpty()) {
            println("黔驴技穷")
            return ""
        }
        else if (candidateWords.size == 1){
            return candidateWords.first()
        }

        return guess()
    }

    // 获取游戏状态
    private fun isWinning(rows:MutableList<List<Pair<String,Int>>>):Boolean{
        return rows.any { row -> row.all { it.second == 1 } } // 任意一行全绿判定为胜利
    }

    private fun getActualLastAttempt(rows: MutableList<List<Pair<String, Int>>>):String{
        return buildString {
            rows.last { row->row.all { it.first != "" }}.forEach { cell -> append(cell.first) }
        }
    }

    private fun collectInfos(rows: MutableList<List<Pair<String, Int>>>){
        for (row in rows.filter { row -> row.all { it.first != "" } }) {
            for (i in 0 until 5){
                // 0-yellow 1-green 2-wrong
                when(row[i].second){
                    0->{
                        yellowLetters.add(Pair(row[i].first,i+1))
                    }
                    1->{
                        val letterV = Pair(row[i].first,i+1)
                        greenLetters.add(letterV)
                        yellowLetters.removeIf{it == letterV}
                    }
                    2->{
                        wrongLetters.add(row[i].first)
                    }
                }
            }
        }
    }

    fun nextRound(rows:MutableList<List<Pair<String,Int>>>,round:Int):String{
        KaitoMind.KaitoLogger.info("Round: $round Guess $lastAttempt")
        this.round = round
        if (this.round == 0){ // 初始回合
            reset()
        }
        if (isWinning(rows)){ // 胜利的话就重置状态
            this.reset()
        }else{ // 收集green yellow
            collectInfos(rows)
            lastAttempt = pickUpAWord()
        }
        KaitoMind.KaitoLogger.info("Round: $round Guess: $lastAttempt")
        return lastAttempt
    }

}
