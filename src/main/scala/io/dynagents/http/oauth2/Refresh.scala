package io.dynagents.http.oauth2

import io.dynagents.jsonformat.JsDecoder

final case class RefreshRequest(
  client_secret: String,
  refresh_token: String,
  client_id: String,
  grant_type: String = "refresh_token"
)

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
