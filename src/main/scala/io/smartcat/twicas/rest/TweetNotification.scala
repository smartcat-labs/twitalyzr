package io.smartcat.twicas.rest

import com.google.gson.Gson
import io.smartcat.twicas.util.Conf
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.{DataFrame, Row}

case class TweetNotification(id: Long, prediction: String, probability: Double)

object TweetNotification {


  /**
    * First filter dataframe to send only tweets of one class (eg, only tweets about cassandraDB = 1.0)
    * @param df
    * @param classLabel 1.0 is tweets about cassandra database.
    */
  def filterAndSend(df:DataFrame, classLabel:Double = 1.0 ):Unit = {
    send(df.filter(df(Conf.Train.predictionColumn) === classLabel))
  }

  /**
    * Sends all tweets
    * @param df
    */
  def send(df: DataFrame): Unit = {

    def toTweetNotification(row: Row): TweetNotification = {
      val id = row.getAs[String](Conf.idColumn).toLong
      val predictionNum: Int = row.getAs[Double](Conf.Train.predictionColumn).toInt
      val probability: Double = row.getAs[Vector](Conf.Train.probabilityColumn).toArray(predictionNum)
      TweetNotification(id, Conf.SendResults.classes(predictionNum), probability)
    }

    def forward(tweets: List[TweetNotification]): Unit = {
      val stockAsJson = new Gson().toJson(tweets)

      val post = new HttpPost(Conf.SendResults.host)

      post.setHeader("Content-type", "application/json")

      post.setEntity(new StringEntity(stockAsJson))

      val response = HttpClientBuilder.create().build().execute(post)

    }

    forward(df.take(Conf.SendResults.maxTweets).map(toTweetNotification).toList)


  }

}