package com.github.synesso.twitmote

import javax.mail.internet.InternetAddress

import com.typesafe.config.{Config => Cfg, ConfigFactory}

import scala.util.Try

object Config {

  val conf = ConfigFactory.parseResources("app.config")

  implicit class ConfigExtend(conf: Cfg) {
    def getOptionalInt(key: String): Option[Int] = Try(conf.getInt(key)).toOption
    def getInternetAddress(key: String): InternetAddress = new InternetAddress(conf.getString(key))
  }

}
