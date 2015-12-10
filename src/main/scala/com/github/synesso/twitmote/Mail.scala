package com.github.synesso.twitmote

import java.io.{PrintWriter, StringWriter}
import javax.mail.internet.InternetAddress

import com.github.synesso.twitmote.Config._
import courier.{Envelope, Mailer, Multipart}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.language.reflectiveCalls


trait Mail {

  val admin = conf.getInternetAddress("email.sender")

  def mail(subject: String, body: String, sender: InternetAddress, recipient: InternetAddress,
           moreRecipients: InternetAddress*): Unit = {

    val allRecipients = recipient +: moreRecipients

    val futureMail = mailer(Envelope.from(sender)
      .to(allRecipients: _*)
      .subject(subject)
      .content(Multipart().html(body)))

    futureMail.onSuccess {
      case _ => println(s"Sent message '$subject' to ${allRecipients.map(_.toString).mkString(",")}")
    }

    futureMail.onFailure {
      case t =>
        println(s"Failed to send message '$subject' to ${allRecipients.map(_.toString).mkString(",")}")
        t.printStackTrace()
    }

    Await.result(futureMail, Duration.Inf)
  }

  def reportError(subject: String, t: Throwable): Unit = {
    mail(subject, s"<pre>${stackTrace(t)}</pre>", admin, admin)
  }

  val smtpHost = conf.getString("email.smtp.host")
  val smtpPort = conf.getInt("email.smtp.port")
  val smtpUser = conf.getString("email.smtp.user")
  val smtpPassword = conf.getString("email.smtp.password")

  private def mailer = Mailer(smtpHost, smtpPort)
    .auth(a = true)
    .as(smtpUser, smtpPassword)
    .startTtls(s = true)()

  private def stackTrace(t: Throwable) = {
    t.printStackTrace()
    val w = new StringWriter()
    t.printStackTrace(new PrintWriter(w))
    w.getBuffer.toString
  }

}
