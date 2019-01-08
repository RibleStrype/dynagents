package io.dynagents.http.oauth2

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.dynagents.http.encoding.{UrlQuery, UrlQueryWriter}
import scalaz.IList

final case class AuthRequest(
  redirect_uri: String Refined Url,
  scope: String,
  client_id: String,
  prompt: String = "consent",
  response_type: String = "code",
  access_type: String = "offline"
)

object AuthRequest {
  implicit val query: UrlQueryWriter[AuthRequest] =
    a => UrlQuery(IList(
      "redirect_uri" -> a.redirect_uri.value,
      "scope" -> a.scope,
      "client_id" -> a.client_id,
      "prompt" -> a.prompt,
      "response_type" -> a.response_type,
      "access_type" -> a.access_type
    ))
}