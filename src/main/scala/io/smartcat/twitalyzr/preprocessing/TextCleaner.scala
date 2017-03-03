package io.smartcat.twitalyzr.preprocessing

import io.smartcat.twitalyzr.pipeline.Pipeline
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

class TextCleaner(columns: List[String]) extends Pipeline {

  override def process(df: DataFrame): DataFrame = {
    def cleanOne(df: DataFrame, columnName: String): DataFrame = {
      df.withColumn(columnName,
        trim(lower(
          regexp_replace(
            regexp_replace(
              regexp_replace(
                df(columnName),
                """-|\'|RT|:|\.|@[^\s]+|http[^\s]+|([^\x00-\x7F])""", ""),
              """[^a-zA-Z]""", " "),
            """\s+""", " "))))
    }

    columns.foldLeft(df)((dff, columnName) => cleanOne(dff, columnName))
  }
}