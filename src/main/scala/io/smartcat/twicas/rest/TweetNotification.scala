package io.smartcat.twicas.rest

import io.smartcat.twicas.util.Conf
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.sql.{DataFrame, Row}

case class TweetNotification(Id: String) {
  override def toString: String = {
    "{'Id':'" + Id + "'}"
  }
}

object TweetNotification {


  /**
    * First filter dataframe to send only tweets of one class (eg, only tweets about cassandraDB = 1.0)
    *
    * @param df
    * @param classLabel 1.0 is tweets about cassandra database.
    */
  def filterAndSend(df: DataFrame, classLabel: Double = 1.0): Unit = {
    send(df.filter(df(Conf.Train.predictionColumn) === classLabel))
  }

  /**
    * Sends all tweets
    *
    * @param df
    */
  def send(df: DataFrame): Unit = {

    def toTweetNotification(row: Row): TweetNotification = {
      val id = row.getAs[String](Conf.idColumn)
      TweetNotification(id)
    }

    def forward(tweets: List[TweetNotification]): Unit = {

      val start = """["""
      val end = """]"""
      val json = tweets.mkString(start, ",", end)

      val post = new HttpPost(Conf.SendResults.host)

      post.setHeader("Content-type", "application/json")

      post.setEntity(new StringEntity(json))

      val response = HttpClientBuilder.create().build().execute(post)


    }

    forward(df.take(Conf.SendResults.maxTweets).map(toTweetNotification).toList)


  }

}