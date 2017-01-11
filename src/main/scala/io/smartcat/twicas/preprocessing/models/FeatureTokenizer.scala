package io.smartcat.twicas.preprocessing.models

import io.smartcat.twicas.preprocessing.pipeline.Pipeline
import org.apache.spark.ml.feature.RegexTokenizer
import org.apache.spark.sql.DataFrame

class FeatureTokenizer(regexpTokenizers: List[RegexTokenizer]) extends Pipeline {

  override def process(df: DataFrame): DataFrame = regexpTokenizers.foldLeft(df)((dff, reg) => reg.transform(dff))

}

object FeatureTokenizer extends Serializable {
  val nameModification = "_t"

  def make(columnsNames: List[String]): FeatureTokenizer = {
    new FeatureTokenizer(columnsNames.map(
      column => new RegexTokenizer()
        .setInputCol(column)
        .setOutputCol(column + nameModification)
        .setPattern("""\s+""")
    ))
  }

}
