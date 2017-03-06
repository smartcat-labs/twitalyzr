package io.smartcat.twitalyzr.training.preprocess

import io.smartcat.twitalyzr.pipeline.PipelineProcessor
import io.smartcat.twitalyzr.preprocessing._
import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.sql.DataFrame

object CountVectorizer {

  /**
    * Make pipeline model for count occurrences in text columns
    * @param df DataFrame train set
    * @param text vector size of text
    * @param user vector size of user
    * @param hashtags vector size of hashtags
    * @return PipelineProcessor
    */
  def makeOne(df: DataFrame, text: Int, user: Int, hashtags: Int): PipelineProcessor = {
    val textCleaner = new TextCleaner(List(Conf.textColumn, Conf.userDescriptionColumn))
    val tokenizer = FeatureTokenizer.make(List(Conf.textColumn, Conf.userDescriptionColumn))

    val textAfterTokenizer = Conf.textColumn + Conf.Preprocessing.Sufix.afterTokenizer
    val userAfterTokenizer = Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterTokenizer

    val stopWord = FeatureStopWordRemove.make(List(textAfterTokenizer, userAfterTokenizer))

    val textAfterSW = textAfterTokenizer + Conf.Preprocessing.Sufix.afterStopWord
    val userAfterSW = userAfterTokenizer + Conf.Preprocessing.Sufix.afterStopWord

    val dfAfterTrans = stopWord.process(tokenizer.process(textCleaner.process(df)))

    val countVectorizer = FeatureCountVectorizer.make(dfAfterTrans, Map(
      textAfterSW -> text,
      userAfterSW -> user,
      Conf.hashtagsColumn -> hashtags
    ))


    val cvSuf = Conf.Preprocessing.Sufix.afterCountVectorizer

    val assembler = FeatureAssembler.make(List(textAfterSW + cvSuf, userAfterSW + cvSuf, Conf.hashtagsColumn + cvSuf),
      Conf.Preprocessing.outputColumn)
    PipelineProcessor(List(textCleaner, tokenizer, stopWord, countVectorizer, assembler))
  }

}
