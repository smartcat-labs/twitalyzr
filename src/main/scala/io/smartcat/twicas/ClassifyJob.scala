package io.smartcat.twicas


import io.smartcat.twicas.tweet.Tweet
import io.smartcat.twicas.util.Conf
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object ClassifyJob extends App {
  val spark = SparkSession.builder()
    .appName("twitter_classifier")
    .getOrCreate()

  spark.conf.set("spark.streaming.stopGracefullyOnShutdown","true")

  System.setProperty("twitter4j.oauth.consumerKey", Conf.Stream.consumerKey)
  System.setProperty("twitter4j.oauth.consumerSecret", Conf.Stream.consumerSecret)
  System.setProperty("twitter4j.oauth.accessToken", Conf.Stream.token)
  System.setProperty("twitter4j.oauth.accessTokenSecret", Conf.Stream.tokenSecret)

  val ssc = new StreamingContext(spark.sparkContext, Seconds(Conf.Stream.interval))
  val tweetStream = TwitterUtils.createStream(ssc, None, Seq(Conf.Stream.searchFilter))

  val loadedModel = PipelineModel.load(Conf.modelPath)

  import spark.sqlContext.implicits._

  tweetStream.foreachRDD((rdd, time) => {
    if (!rdd.isEmpty()) {
      val tweets = rdd.map(Tweet.makeStream)
      val tweetsDF = tweets.toDF
      val processedDF = loadedModel.transform(tweetsDF)
      //do something with results
      processedDF.select("text").show(3)
    }
  })

  sys.ShutdownHookThread {
    ssc.stop(true, true)
    tweetStream.stop()
  }

  ssc.start()
  ssc.awaitTermination()
}
