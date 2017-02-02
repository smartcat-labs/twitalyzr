package io.smartcat.twicas.preprocessing

import io.smartcat.twicas.pipeline.Pipeline
import io.smartcat.twicas.util.Conf
import org.apache.spark.ml.feature.StopWordsRemover
import org.apache.spark.sql.DataFrame

class FeatureStopWordRemove(colRem: List[StopWordsRemover]) extends Pipeline {
  override def process(df: DataFrame): DataFrame = colRem.foldLeft(df)((dff, stop) => stop.transform(dff))
}

object FeatureStopWordRemove extends Serializable {
  val filtered = Conf.Preprocessing.Sufix.afterStopWord

  def make(columns: List[String]): FeatureStopWordRemove = {
    new FeatureStopWordRemove(columns map { col =>
      new StopWordsRemover()
        .setInputCol(col)
        .setOutputCol(col + filtered)
    })
  }

}
