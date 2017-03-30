package io.smartcat.twitalyzr.tweet

import org.apache.spark.sql.Row
import twitter4j.Status

case class Tweet(id: String, text: String, favoriteCount: Long,
                 hashtags: Array[String], urls: Array[String], retweetCount: Long,
                 userFollowers: Long, userFriendsCount: Long, userDescription: String, label: Long, lang: String) extends Serializable

object Tweet extends Serializable {

  def makeStream(status: Status): Tweet = {
    val hashtags = status.getHashtagEntities.map(entity => entity.getText.toLowerCase)
    val urls = status.getURLEntities.map(entity => entity.getText)
    Tweet(status.getId.toString, status.getText, status.getFavoriteCount, hashtags, urls, status.getRetweetCount,
      status.getUser.getFollowersCount, status.getUser.getFriendsCount, status.getUser.getDescription, -1, status.getLang)
  }

  def makeJsonRow(row: Row): Tweet = {
    val tweetText = row.getAs[String]("text")
    val id = row.getAs[String]("id_str")
    val favoriteCount = row.getAs[Long]("favorite_count")
    val entities = row.getAs[Row]("entities")
    val hashtags: Array[String] = {
      entities.getSeq[Row](entities.fieldIndex("hashtags")).map { el: Row => el.getAs[String]("text").toLowerCase }.toArray
    }
    val urls: Array[String] = {
      entities.getSeq[Row](entities.fieldIndex("urls")).map { el: Row => el.getAs[String]("url") }.toArray
    }
    val label = row.getAs[Long]("label")
    val retweetCount = row.getAs[Long]("retweet_count")
    val user = row.getAs[Row]("user")
    val userDescription = user.getAs[String]("description")
    val userFollowersCount = user.getAs[Long]("followers_count")
    val userFriendsCount = user.getAs[Long]("friends_count")
    val userID = user.getAs[Long]("id").toString

    Tweet(id, tweetText, favoriteCount, hashtags, urls, retweetCount, userFollowersCount,
      userFriendsCount, userDescription, label, row.getAs[String]("lang"))
  }
}
