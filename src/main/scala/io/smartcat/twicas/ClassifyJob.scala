package io.smartcat.twicas

import io.smartcat.twicas.pipeline.PipelineProcessor
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
  val pipelineFile = ""

  val interval = 10

  System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
  System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
  System.setProperty("twitter4j.oauth.accessToken", token)
  System.setProperty("twitter4j.oauth.accessTokenSecret", tokenSecret)

  val ssc = new StreamingContext(spark.sparkContext, Seconds(interval))
  val tweetStream = TwitterUtils.createStream(ssc, None, Seq(searchFilter))

  val pipeline = PipelineProcessor.loadFromFile(pipelineFile)

  import spark.sqlContext.implicits._

  tweetStream.foreachRDD((rdd, time) => {
    if (!rdd.isEmpty()) {
      val tweets = rdd.map(Tweet.makeStream)
      val tweetsDF = tweets.toDF
      val prediction = pipeline.processAll(tweetsDF)
      /*
      Filter and save valuable tweets
       */
    }
  })

  ssc.start()
  ssc.awaitTermination()
}
