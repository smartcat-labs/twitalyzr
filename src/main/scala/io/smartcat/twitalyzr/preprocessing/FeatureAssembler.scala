package io.smartcat.twitalyzr.preprocessing

import io.smartcat.twitalyzr.pipeline.Pipeline
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.DataFrame

class FeatureAssembler(assembler: VectorAssembler) extends Pipeline {

  override def process(df: DataFrame): DataFrame = assembler.transform(df)
}

object FeatureAssembler extends Serializable {

  def make(columns: List[String], outputColumn: String): FeatureAssembler =
    new FeatureAssembler(new VectorAssembler().setInputCols(columns.toArray).setOutputCol(outputColumn))

}
