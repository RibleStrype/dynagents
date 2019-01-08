package io.dynagents.http

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.dynagents.http.oauth2._
import io.dynagents.time.{Epoch, LocalClock}
import scalaz.Scalaz._
import scalaz._
import io.dynagents.http.encoding.UrlQueryWriter.ops._
import scala.concurrent.duration._

final case class ServerConfig(
  auth: String Refined Url,
  access: String Refined Url,
  refresh: String Refined Url,
  scope: String,
  clientId: String,
  clientSecret: String
)

final case class RefreshToken(token: String)

final case class BearerToken(token: String, expires: Epoch)

class OAuthClient[F[_] : Monad](
  config: ServerConfig
)(
  user: UserInteraction[F],
  client: JsonClient[F],
  clock: LocalClock[F]
) {

  def authenticate: F[CodeToken] =
    for {
      callback <- user.start
      params = AuthRequest(callback, config.scope, config.clientId)
      _ <- user.open(params.toUrlQuery.forUrl(config.auth))
      code <- user.stop
    } yield code

  def access(code: CodeToken): F[(RefreshToken, BearerToken)] =
    for {
      request <- AccessRequest(code.token, code.redirect_uri, config.clientId, config.clientSecret).pure[F]
      msg <- client.post[AccessRequest, AccessResponse](config.access, request)
      time <- clock.now
      expires = time + msg.expires_in.seconds
      refresh = RefreshToken(msg.refresh_token)
      bearer = BearerToken(msg.access_token, expires)
    } yield (refresh, bearer)

  def bearer(refresh: RefreshToken): F[BearerToken] =
    for {
      request <- RefreshRequest(config.clientSecret, refresh.token, config.clientId).pure[F]
      msg <- client.post[RefreshRequest, RefreshResponse](config.refresh, request)
      time <- clock.now
      expires = time + msg.expires_in.seconds
      bearer = BearerToken(msg.access_token, expires)
    } yield bearer
}
