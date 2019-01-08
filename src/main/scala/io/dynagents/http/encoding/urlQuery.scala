package io.dynagents.http.encoding

import java.net.URI

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import scalaz._, scalaz.Scalaz._
import simulacrum.typeclass

final case class UrlQuery(params: IList[(String, String)]) {
  def forUrl(encoded: String Refined Url): String Refined Url = {
    val uri = new URI(encoded.value)
    val update = new URI(
      uri.getScheme,
      uri.getUserInfo,
      uri.getHost,
      uri.getPort,
      uri.getPath,
      params.map { case (k, v) => k + "=" + v }.intercalate("&"),
      uri.getFragment
    )
    Refined.unsafeApply(update.toASCIIString)
  }
}

@typeclass trait UrlQueryWriter[A] {
  def toUrlQuery(a: A): UrlQuery
}