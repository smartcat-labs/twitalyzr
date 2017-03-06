package io.smartcat.twitalyzr.training.preprocess

import io.smartcat.twitalyzr.pipeline.PipelineProcessor
import io.smartcat.twitalyzr.preprocessing._
import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.sql.DataFrame

object NGrams {

  /**
    * Make NGrams and do TF on them
    * @param text vector size of tweet text
    * @param user vector size of user description text
    * @param hashtags vctor size of hashtags
    * @param nGramText n for ngram of text
    * @param nGramUser n for ngram of user description
    * @param nGramTextTF vector size for ngram of text
    * @param nGramUserTF vector size of ngram of user description
    * @return PipelineProcessor
    */
  def makeOne(text: Int, user: Int, hashtags: Int, nGramText: Int,
              nGramUser: Int, nGramTextTF: Int, nGramUserTF: Int): PipelineProcessor = {
    val textCleaner = new TextCleaner(List(Conf.textColumn, Conf.userDescriptionColumn))
    val tokenizer = FeatureTokenizer.make(List(Conf.textColumn, Conf.userDescriptionColumn))

    val textAfterTok = Conf.textColumn + Conf.Preprocessing.Sufix.afterTokenizer
    val userAfterTok = Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterTokenizer

    val stopWord = FeatureStopWordRemove.make(List(textAfterTok, userAfterTok))

    val tAfterStop = textAfterTok + Conf.Preprocessing.Sufix.afterStopWord
    val uAfterStop = userAfterTok + Conf.Preprocessing.Sufix.afterStopWord

    val textNGramName = Conf.textColumn + Conf.Preprocessing.Sufix.afterNGram
    val userNGramName = Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterNGram

    val textNGram = FeatureNGram.make(tAfterStop, textNGramName, nGramText)
    val userNGram = FeatureNGram.make(uAfterStop, userNGramName, nGramUser)

    val hashTF = FeatureHashTF.make(Map(
      tAfterStop -> text,
      uAfterStop -> user,
      Conf.hashtagsColumn -> hashtags,
      textNGramName -> nGramTextTF,
      userNGramName -> nGramUserTF
    ))

    val afterTF = Conf.Preprocessing.Sufix.afterTF

    val assembler = FeatureAssembler.make(List(tAfterStop + afterTF, uAfterStop + afterTF, Conf.hashtagsColumn + afterTF,
      textNGramName + afterTF, userNGramName + afterTF), Conf.Preprocessing.outputColumn)
    PipelineProcessor(List(textCleaner, tokenizer, stopWord, textNGram, userNGram, hashTF, assembler))

  }
  /**
    * Make NGrams and do TF on them
    * @param df DataFrame training set
    * @param text vector size of tweet text
    * @param user vector size of user description text
    * @param hashtags vctor size of hashtags
    * @param nGramText n for ngram of text
    * @param nGramUser n for ngram of user description
    * @param nGramTextCV vector size for ngram of text
    * @param nGramUserCV vector size of ngram of user description
    * @return PipelineProcessor
    */
  def withWordCounter(df: DataFrame, text: Int, user: Int, hashtags: Int, nGramText: Int,
                      nGramUser: Int, nGramTextCV: Int, nGramUserCV: Int): PipelineProcessor = {
    val textCleaner = new TextCleaner(List(Conf.textColumn, Conf.userDescriptionColumn))
    val tokenizer = FeatureTokenizer.make(List(Conf.textColumn, Conf.userDescriptionColumn))

    val textAfterTok = Conf.textColumn + Conf.Preprocessing.Sufix.afterTokenizer
    val userAfterTok = Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterTokenizer

    val stopWord = FeatureStopWordRemove.make(List(textAfterTok, userAfterTok))

    val tAfterStop = textAfterTok + Conf.Preprocessing.Sufix.afterStopWord
    val uAfterStop = userAfterTok + Conf.Preprocessing.Sufix.afterStopWord

    val textNGramName = Conf.textColumn + Conf.Preprocessing.Sufix.afterNGram
    val userNGramName = Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterNGram

    val textNGram = FeatureNGram.make(tAfterStop, textNGramName, nGramText)
    val userNGram = FeatureNGram.make(uAfterStop, userNGramName, nGramUser)

    val dfAfterTrans = userNGram.process(textNGram.process(stopWord.process(tokenizer.process(textCleaner.process(df)))))

    val wordCounter = FeatureCountVectorizer.make(dfAfterTrans, Map(
      tAfterStop -> text,
      uAfterStop -> user,
      Conf.hashtagsColumn -> hashtags,
      textNGramName -> nGramTextCV,
      userNGramName -> nGramUserCV
    ))

    val afterCV = Conf.Preprocessing.Sufix.afterCountVectorizer

    val assembler = FeatureAssembler.make(List(tAfterStop + afterCV, uAfterStop + afterCV, Conf.hashtagsColumn + afterCV,
      textNGramName + afterCV, userNGramName + afterCV), Conf.Preprocessing.outputColumn)
    PipelineProcessor(List(textCleaner, tokenizer, stopWord, textNGram, userNGram, wordCounter, assembler))
  }

}
