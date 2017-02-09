package io.smartcat.twicas.preprocessing

import io.smartcat.twicas.pipeline.Pipeline
import org.apache.spark.ml.feature.NGram
import org.apache.spark.sql.DataFrame

class FeatureNGram(nGram: NGram) extends Pipeline {
  override def process(df: DataFrame): DataFrame = nGram.transform(df)
}

object FeatureNGram extends Serializable {

  def make(inputCol: String, outputCol: String, ngram: Int): FeatureNGram = {
    new FeatureNGram(new NGram()
      .setInputCol(inputCol)
      .setOutputCol(outputCol)
      .setN(ngram))
  }

}
