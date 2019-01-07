package io.dynagents.http.oauth2

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.dynagents.jsonformat.JsDecoder

final case class AccessRequest(
  code: String,
  redirect_uri: String Refined Url,
  client_id: String,
  client_secret: String,
  scope: String = "",
  grant_type: String = "authorization_code"
)

final case class AccessResponse(
  access_token: String,
  token_type: String,
  expires_in: Long,
  refresh_token: String
)

object AccessResponse {
  implicit val json: JsDecoder[AccessResponse] = j => for {
    acc <- j.getAs[String]("access_token")
    tpe <- j.getAs[String]("token_type")
    exp <- j.getAs[Long]("expires_in")
    ref <- j.getAs[String]("refresh_token")
  } yield AccessResponse(acc, tpe, exp, ref)
}
