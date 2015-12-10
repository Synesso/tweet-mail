package com.github.synesso.twitmote

import com.github.synesso.twitmote.Config.conf
import com.typesafe.config.ConfigFactory
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

trait Twitter {

  lazy val config = new ConfigurationBuilder()
    .setOAuthConsumerKey(conf.getString("twitter.api.key"))
    .setOAuthConsumerSecret(conf.getString("twitter.api.secret"))
    .setOAuthAccessToken(conf.getString("twitter.access.token"))
    .setOAuthAccessTokenSecret(conf.getString("twitter.access.secret"))
    .build()

  lazy val twitter = new TwitterFactory(config).getInstance

  lazy val accounts = ConfigFactory.parseResources("accounts.config")

}
