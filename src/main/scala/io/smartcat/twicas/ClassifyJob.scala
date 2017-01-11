package io.smartcat.twicas

import io.smartcat.twicas.tweet.Tweet
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object ClassifyJob extends App {
  val spark = SparkSession.builder()
    .appName("twitter_classifier")
    .getOrCreate()

  val token = ""
  val tokenSecret = ""
  val consumerKey = ""
  val consumerSecret = ""

  val searchFilter = "#cassandra"

  val interval = 10

  System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
  System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
  System.setProperty("twitter4j.oauth.accessToken", token)
  System.setProperty("twitter4j.oauth.accessTokenSecret", tokenSecret)

  val ssc = new StreamingContext(spark.sparkContext, Seconds(interval))
  val tweetStream = TwitterUtils.createStream(ssc, None, Seq(searchFilter))

  import spark.sqlContext.implicits._

  tweetStream.foreachRDD((rdd, time) => {
    val tweets = rdd.map(Tweet.makeStream)
    val tweetsDF = tweets.toDF
    tweetsDF.show(3)
  })

  ssc.start()
  ssc.awaitTermination()
}
