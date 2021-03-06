package io.dynagents.time

import java.time.Instant

import contextual.{Prefix, Verifier}

import scala.concurrent.duration._

final case class Epoch(millis: Long) extends AnyVal {
  def +(d: FiniteDuration): Epoch = Epoch(millis + d.toMillis)

  def -(e: Epoch): FiniteDuration = (millis - e.millis).millis
}

object Epoch {

  object EpochInterpolator extends Verifier[Epoch] {
    def check(s: String): Either[(Int, String), Epoch] =
      try Right(Epoch(Instant.parse(s).toEpochMilli))
      catch {
        case _: Throwable => Left((0, "not in ISO-8601 format"))
      }
  }

  implicit class EpochMillisStringContext(sc: StringContext) {
    val epoch = Prefix(EpochInterpolator, sc)
  }
}