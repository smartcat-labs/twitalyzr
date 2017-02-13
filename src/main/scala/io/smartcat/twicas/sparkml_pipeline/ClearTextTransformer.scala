package io.smartcat.twicas.sparkml_pipeline

import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{DataType, DataTypes, StringType}

class ClearTextTransformer(override val uid: String) extends UnaryTransformer[String, String, ClearTextTransformer] with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("upper"))

  override protected def outputDataType: DataType = DataTypes.StringType

  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType == StringType)
  }

  override protected def createTransformFunc: (String) => String = {
    _.toLowerCase.trim
      .replaceAll("""-|\'|RT|:|\.|@[^\s]+|http[^\s]+|([^\x00-\x7F])""", "")
      .replaceAll("""[^a-zA-Z]""", " ")
      .replaceAll("""\s+""", " ")
  }

}

object ClearTextTransformer extends DefaultParamsReadable[ClearTextTransformer]
