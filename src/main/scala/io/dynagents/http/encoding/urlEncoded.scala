package io.dynagents.http.encoding

import java.net.URLEncoder

import eu.timepit.refined.api.Refined
import scalaz.Scalaz._
import scalaz._
import simulacrum.typeclass

final case class UrlEncoded()

@typeclass trait UrlEncodedWriter[A] {
  def toUrlEncoded(a: A): String Refined UrlEncoded
}

object UrlEncodedWriter {

  import UrlEncodedWriter.ops._

  implicit val encoded: UrlEncodedWriter[String Refined UrlEncoded] = instance(x => x)

  implicit val string: UrlEncodedWriter[String] =
    instance(s => Refined.unsafeApply(URLEncoder.encode(s, "UTF-8")))

  implicit val long: UrlEncodedWriter[Long] =
    instance(l => Refined.unsafeApply(l.toString))

  implicit def ilist[K: UrlEncodedWriter, V: UrlEncodedWriter]: UrlEncodedWriter[IList[(K, V)]] =
    instance { m =>
      val raw = m.map {
        case (k, v) => k.toUrlEncoded + "=" + v.toUrlEncoded
      }.intercalate("&")
      Refined.unsafeApply(raw)
    }

  private def instance[T](f: T => String Refined UrlEncoded): UrlEncodedWriter[T] = f(_)
}
