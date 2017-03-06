package io.smartcat.twitalyzr.preprocessing

import io.smartcat.twitalyzr.pipeline.Pipeline
import io.smartcat.twitalyzr.util.Conf
import org.apache.spark.ml.feature.RegexTokenizer
import org.apache.spark.sql.DataFrame

class FeatureTokenizer(regexpTokenizers: List[RegexTokenizer]) extends Pipeline {

  override def process(df: DataFrame): DataFrame = regexpTokenizers.foldLeft(df)((dff, reg) => reg.transform(dff))

}

object FeatureTokenizer extends Serializable {
  val nameModification : String = Conf.Preprocessing.Sufix.afterTokenizer

  def make(columnsNames: List[String]): FeatureTokenizer = {
    new FeatureTokenizer(columnsNames.map(
      column => new RegexTokenizer()
        .setInputCol(column)
        .setOutputCol(column + nameModification)
        .setPattern("""\s+""")
    ))
  }

}
