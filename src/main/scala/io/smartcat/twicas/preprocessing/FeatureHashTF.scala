package io.smartcat.twicas.preprocessing

import io.smartcat.twicas.pipeline.Pipeline
import org.apache.spark.ml.feature.HashingTF
import org.apache.spark.sql.DataFrame

class FeatureHashTF(hashingModels: List[HashingTF]) extends Pipeline {
  override def process(df: DataFrame): DataFrame = hashingModels.foldLeft(df)((dff, model) => model.transform(dff))
}

object FeatureHashTF extends Serializable {
  val afterHashing = "_tf"

  def make(columnNamesFeatureNum: Map[String, Int]): FeatureHashTF = {
    new FeatureHashTF(columnNamesFeatureNum.keySet.map(column => {
      new HashingTF()
        .setInputCol(column)
        .setOutputCol(column + afterHashing)
        .setNumFeatures(columnNamesFeatureNum(column))
    }).toList)
  }

}