package io.dynagents.http.oauth2

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url

final case class AuthRequest(
  redirect_uri: String Refined Url,
  scope: String,
  client_id: String,
  prompt: String = "consent",
  response_type: String = "code",
  access_type: String = "offline"
)