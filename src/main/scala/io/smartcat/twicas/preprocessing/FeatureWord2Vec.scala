package io.smartcat.twicas.preprocessing

import io.smartcat.twicas.pipeline.Pipeline
import org.apache.spark.ml.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.sql.DataFrame

class FeatureWord2Vec(word2VecModel: Word2VecModel) extends Pipeline {
  override def process(df: DataFrame): DataFrame = word2VecModel.transform(df)
}

object FeatureWord2Vec extends Serializable {

  def make(df: DataFrame, inputCol: String, outputCol: String, vectorSize: Int, minCount: Int): FeatureWord2Vec = {
    new FeatureWord2Vec(new Word2Vec()
      .setInputCol(inputCol)
      .setOutputCol(outputCol)
      .setVectorSize(vectorSize)
      .setMinCount(minCount).fit(df))
  }

}
