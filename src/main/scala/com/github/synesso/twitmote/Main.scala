package com.github.synesso.twitmote

import java.io.FileWriter
import javax.mail.internet.InternetAddress

import com.github.synesso.twitmote.Config._
import com.typesafe.config.{ConfigRenderOptions, ConfigValueFactory}
import twitter4j.{Paging, Status}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.language.reflectiveCalls
import scala.util.Try

object Main extends App with Twitter with Mail {

  val sender = conf.getInternetAddress("email.sender")
  val recipient = conf.getInternetAddress("email.recipient")

  val tweets: Try[Seq[String]] = Try {
    val users = accounts.getObject("accounts").keySet().asScala.map { k => k -> accounts.getLong(s"accounts.$k.last") }.toMap
    val tba = users.map { case (handle, last) => handle -> statuses(handle, last) }
    saveConfig(tba)
    tba.flatMap{ case (_, statuses) => statuses.map(_.getText.replaceAll("#[a-zA-Z]*", "").replaceAll("\\? ","").trim).toSeq }.toSeq
  }

  tweets.recover{ case t => reportError(s"Job failed fetching from twitter: ${t.getMessage}", t)}

  tweets.foreach{ xs =>
    val subject = s"${xs.size} new tweet${if (xs.size == 1) "" else "s"}"
    if (xs.nonEmpty) {
      val html =
        s"""<ul>
         |${xs.map(x => s"<li>$x</li>").mkString}
         |</ul>
       """.stripMargin
      mail(subject, html, sender, recipient)
    }
    println(subject)
  }

  def statuses(handle: String, since: Long): Stream[Status] = {
    @tailrec
    def loop(page: Int, acc: Stream[Status]): Stream[Status] = {
      val paging = new Paging(page, since)
      val xs = twitter.getUserTimeline(handle, paging).iterator().asScala.toSeq
      val stream = Stream.concat(acc, xs.filterNot(_.getText.startsWith("@")).filterNot(_.getText.startsWith("RT ")).toStream)
      if (xs.size < 20) stream
      else loop(page + 1, stream)
    }
    loop(1, Stream.empty)
  }

  def saveConfig(tba: Map[String, Stream[Status]]) {
    val updatedAccounts = tba.foldLeft(accounts) {
      case (acc, (handle, lastTweet #:: _)) =>
        acc.withValue(s"accounts.$handle.last", ConfigValueFactory.fromAnyRef(lastTweet.getId))
      case (acc, _) => acc
    }
    val w = new FileWriter("src/main/resources/accounts.config")
    w.write(updatedAccounts.root.render(ConfigRenderOptions.concise().setFormatted(true)))
    w.close()
  }

}
