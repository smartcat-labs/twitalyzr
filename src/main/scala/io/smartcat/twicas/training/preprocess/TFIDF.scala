package io.smartcat.twicas.training.preprocess

import io.smartcat.twicas.pipeline.PipelineProcessor
import io.smartcat.twicas.preprocessing._
import io.smartcat.twicas.util.Conf
import org.apache.spark.sql.DataFrame

object TFIDF {

  /**
    * Makes preprocess pipeline for TFIDF
    * @param df DataFrame of train set
    * @param text vector size for tweet text
    * @param user vector size for user description text
    * @param hashtags vector size for hashtags
    * @return PipelineProcessor containing all stages of TFIDF
    */
  def makeOne(df: DataFrame, text: Int, user: Int, hashtags: Int): PipelineProcessor = {
    val textCleaner = new TextCleaner(List(Conf.textColumn, Conf.userDescriptionColumn))
    val tokenizer = FeatureTokenizer.make(List(Conf.textColumn, Conf.userDescriptionColumn))

    val textAfterTokenizer = Conf.textColumn + Conf.Preprocessing.Sufix.afterTokenizer
    val userAfterTokenizer = Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterTokenizer

    val stopWord = FeatureStopWordRemove.make(List(textAfterTokenizer, userAfterTokenizer))

    val textAfterSW = textAfterTokenizer + Conf.Preprocessing.Sufix.afterStopWord
    val userAfterSW = userAfterTokenizer + Conf.Preprocessing.Sufix.afterStopWord

    val hashTF = FeatureHashTF.make(Map(
      textAfterSW -> text,
      userAfterSW -> user,
      Conf.hashtagsColumn -> hashtags
    ))

    val textAfterTF = textAfterSW + Conf.Preprocessing.Sufix.afterTF
    val userAfterTF = textAfterSW + Conf.Preprocessing.Sufix.afterTF
    val hashtagAfterTF = Conf.hashtagsColumn + Conf.Preprocessing.Sufix.afterTF

    val dfAfterTF = hashTF.process(stopWord.process(tokenizer.process(textCleaner.process(df))))

    val textAfterIDF = textAfterTF + Conf.Preprocessing.Sufix.afterIDF
    val userAfterIDF = userAfterTF + Conf.Preprocessing.Sufix.afterIDF

    val idf = FeatureIDF.make(dfAfterTF, List(textAfterTF, userAfterTF))

    val assembler = FeatureAssembler.make(List(textAfterIDF, userAfterIDF, hashtagAfterTF), Conf.Preprocessing.outputColumn)

    PipelineProcessor(List(textCleaner, tokenizer, stopWord, hashTF, idf, assembler))
  }

}
