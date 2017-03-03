package io.smartcat.twitalyzr.preprocessing

import io.smartcat.twitalyzr.pipeline.Pipeline
import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel}
import org.apache.spark.sql.DataFrame

class FeatureCountVectorizer(countVectorizerModel: List[CountVectorizerModel]) extends Pipeline {
  override def process(df: DataFrame): DataFrame = countVectorizerModel.foldLeft(df)((dff, model) => model.transform(dff))
}

object FeatureCountVectorizer extends Serializable {

  val outputSufix: String = Conf.Preprocessing.Sufix.afterCountVectorizer
  val minDF: Int = Conf.Preprocessing.CountVectorizer.minDocumentFrequency
  val minTF: Int = Conf.Preprocessing.CountVectorizer.minTermFrequency

  def make(df: DataFrame, vectorCounts: Map[String, Int]): FeatureCountVectorizer = {

    val model = vectorCounts.map { case (k, v) =>
      new CountVectorizer()
        .setInputCol(k)
        .setOutputCol(k + outputSufix)
        .setBinary(false)
        .setMinDF(minDF)
        .setMinTF(minTF)
        .setVocabSize(v)
        .fit(df)
    }

    new FeatureCountVectorizer(model.toList)

  }
}

