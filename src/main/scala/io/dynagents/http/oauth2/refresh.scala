package io.dynagents.http.oauth2

import io.dynagents.http.encoding.UrlEncodedWriter
import io.dynagents.jsonformat.JsDecoder
import scalaz.IList

final case class RefreshRequest(
  client_secret: String,
  refresh_token: String,
  client_id: String,
  grant_type: String = "refresh_token"
)

object RefreshRequest {

  import UrlEncodedWriter.ops._

  implicit val encoded: UrlEncodedWriter[RefreshRequest] =
    r => IList(
      "client_secret" -> r.client_secret,
      "refresh_token" -> r.refresh_token,
      "client_id" -> r.client_id,
      "grant_type" -> r.grant_type
    ).toUrlEncoded
}

final case class RefreshResponse(
  access_token: String,
  token_type: String,
  expires_in: Long
)

object RefreshResponse {
  implicit val json: JsDecoder[RefreshResponse] = j =>
    for {
      acc <- j.getAs[String]("access_token")
      tpe <- j.getAs[String]("token_type")
      exp <- j.getAs[Long]("expires_in")
    } yield RefreshResponse(acc, tpe, exp)
}