package io.smartcat.twicas.preprocessing

import io.smartcat.twicas.pipeline.Pipeline
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

class TextCleaner(columns: List[String]) extends Pipeline {
  // ONE NASTY REGEXP
  override def process(df: DataFrame): DataFrame = {
    def cleanOne(df: DataFrame, columnName: String): DataFrame = {
      df.withColumn(columnName,
        trim(lower(
          regexp_replace(
            regexp_replace(df(columnName), """\/|\\|~|&|\^|\?|\!|\$|\(|\)|\\b\\w{1}\\b\\s?|\"|\'|RT|:|\.|@[^\s]+|http[^\s]+|#|[0-9]+|([^\x00-\x7F])""", ""),
            """(\+|_|-|\,|\s+|;)""", " "))))
    }

    columns.foldLeft(df)((dff, columnName) => cleanOne(dff, columnName))
  }
}