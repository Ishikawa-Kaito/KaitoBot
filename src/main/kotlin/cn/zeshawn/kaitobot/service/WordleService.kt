package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.data.WordleData
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


object WordleService {

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

    private fun getRows(respondImage: BufferedImage): MutableList<List<BufferedImage>>{
        val rows = mutableListOf<List<BufferedImage>>()
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
                    add(word)
                }
            }
            rows.add(words)
        }
        return rows
    }

    fun solve(respondImage: BufferedImage):String{
        val rows = getRows(respondImage)
        rows.forEach {
            it.forEach { mat ->
                println(ocr(mat))
            }
        }

    }

    private fun compareByte(imgA:BufferedImage, imgB:BufferedImage):Boolean{
        var diff = 0
        for (i in 0 until imgA.width) {
            for (j in 0 until imgA.height) {
                if (imgA.getRGB(i,j)!=imgB.getRGB(i,j))
                    diff++
                if (diff > 10) return false
            }
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


}


class WordleSolver{

    private val wrongLetters = mutableSetOf<String>() // grey letters
    private val untriedLetters = mutableSetOf<String>() // untried letters
    private val candidateWords = mutableSetOf<String>() // possible words
    private val usedWords = mutableSetOf<String>() // tried words

    private val greenLetters = mutableSetOf<Pair<String,Int>>() // green letters and position
    private val yellowLetters = mutableSetOf<Pair<String,Int>>() // yellow letters and position

    var round = 0 // which round now

    init {
        reset()
    }

    fun reset(){
        wrongLetters.clear()
        untriedLetters.clear()
        candidateWords.clear()
        usedWords.clear()
        round = 0
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
    fun getLetterFreq(words: MutableSet<String>):Map<String,Int>{
        return getLetterCounter(true,words)
    }

    /**
     * 获得有可能的字母的词频
     */
    fun getLetterProbe(words: MutableSet<String>):Map<String,Int>{
        return getLetterCounter(false,words)
    }

    fun isWordForbidden(word:String):Boolean{
        return word.any { it.toString() in wrongLetters }
    }

    fun matchGreen(word: String):Boolean{
        return greenLetters.all {
            it.first == word[it.second].toString()
        }
    }

    fun matchYellow(word: String):Boolean{
        return yellowLetters.all {
            it.first != word[it.second].toString() && it.first in word
        }
    }

    fun getNextCandidateWords(){
        val removal = mutableSetOf<String>()
        candidateWords.forEach {
            if (isWordForbidden(it) || !matchGreen(it) || !matchYellow(it)){
                removal.add(it)
            }
        }
        candidateWords.removeAll(removal)
    }

    fun guess():String{
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

    fun pickUpAWord():String{
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

}
