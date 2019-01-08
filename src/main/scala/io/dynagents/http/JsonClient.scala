package io.dynagents.http

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.dynagents.http.encoding.UrlEncodedWriter
import io.dynagents.jsonformat.JsDecoder
import scalaz.IList

trait JsonClient[F[_]] {

  def get[A: JsDecoder](
    uri: String Refined Url,
    headers: IList[(String, String)] = IList.empty
  ): F[A]

  def post[P: UrlEncodedWriter, A: JsDecoder](
    uri: String Refined Url,
    payload: P,
    headers: IList[(String, String)] = IList.empty
  ): F[A]
}
