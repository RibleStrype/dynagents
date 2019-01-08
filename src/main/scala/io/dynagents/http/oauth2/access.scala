package io.dynagents.http.oauth2

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.dynagents.http.encoding.UrlEncodedWriter
import io.dynagents.jsonformat.JsDecoder
import scalaz.IList

final case class AccessRequest(
  code: String,
  redirect_uri: String Refined Url,
  client_id: String,
  client_secret: String,
  scope: String = "",
  grant_type: String = "authorization_code"
)

object AccessRequest {

  import UrlEncodedWriter.ops._

  implicit val encoded: UrlEncodedWriter[AccessRequest] =
    a => IList(
      "code" -> a.code,
      "redirect_uri" -> a.redirect_uri.value,
      "client_id" -> a.client_id,
      "client_secret" -> a.client_secret,
      "scope" -> a.scope,
      "grant_type" -> a.grant_type
    ).toUrlEncoded
}

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
