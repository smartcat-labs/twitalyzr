package io.smartcat.twitalyzr.training.preprocess

import io.smartcat.twitalyzr.pipeline.PipelineProcessor
import io.smartcat.twitalyzr.preprocessing._
import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.sql.DataFrame

object Word2Vector {

  /**
    * Make word2vector preprocess pipeline
    * @param df DataFrame
    * @param text vector size for tweet text
    * @param user vector size for user description text
    * @param hashtags vector size for hashtags
    * @return PipelineProcessor
    */
  def makeOne(df: DataFrame, text: Int, user: Int, hashtags: Int): PipelineProcessor = {
    val textCleaner = new TextCleaner(List(Conf.textColumn, Conf.userDescriptionColumn))
    val tokenizer = FeatureTokenizer.make(List(Conf.textColumn, Conf.userDescriptionColumn))
    val tokenizerSufix = Conf.Preprocessing.Sufix.afterTokenizer
    val stopWord = FeatureStopWordRemove.make(List(Conf.textColumn + tokenizerSufix, Conf.userDescriptionColumn + tokenizerSufix))

    val cleanedDF = stopWord.process(tokenizer.process(textCleaner.process(df)))

    val textAfterFilter = Conf.textColumn + tokenizerSufix + Conf.Preprocessing.Sufix.afterStopWord
    val userAfterFilter = Conf.textColumn + tokenizerSufix + Conf.Preprocessing.Sufix.afterStopWord

    val textAfterW2V = Conf.textColumn + Conf.Preprocessing.Sufix.afterW2V
    val userAfterW2V = Conf.userDescriptionColumn + Conf.Preprocessing.Sufix.afterW2V

    val word2VecText = FeatureWord2Vec.make(cleanedDF, textAfterFilter,
      textAfterW2V, text, 3)
    val word2VecUser = FeatureWord2Vec.make(cleanedDF, userAfterFilter,
      userAfterW2V, user, 3)
    val hashtagsTF = FeatureHashTF.make(Map(Conf.hashtagsColumn -> hashtags))
    val assembler = FeatureAssembler.make(List(textAfterW2V, userAfterW2V,
      Conf.hashtagsColumn + Conf.Preprocessing.Sufix.afterTF), Conf.Preprocessing.outputColumn)

    new PipelineProcessor(List(textCleaner, tokenizer, stopWord, word2VecText, word2VecUser, hashtagsTF, assembler))
  }

}
