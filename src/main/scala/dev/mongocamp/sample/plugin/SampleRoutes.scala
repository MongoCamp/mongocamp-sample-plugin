package dev.mongocamp.sample.plugin

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.plugin.RoutesPlugin
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.stringBody

import scala.concurrent.Future

class SampleRoutes extends RoutesPlugin with LazyLogging {

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(adminTestEndpoint, securedTestEndpoint, unsecuredTestEndpoint)

  lazy val adminTestEndpoint = adminEndpoint
    .in("plugin")
    .in("admin")
    .out(stringBody)
    .tag("Sample Plugin")
    .method(Method.GET)
    .serverLogic(user => parameter => adminOutput(user))

  def adminOutput(userProfile: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), String]] = {
    Future.successful {
      Right {
        s"Hello ${userProfile.userId} you are admin"
      }
    }
  }

  lazy val securedTestEndpoint = securedEndpoint
    .in("plugin")
    .in("secured")
    .out(stringBody)
    .tag("Sample Plugin")
    .method(Method.GET)
    .serverLogic(user => parameter => securedOutput(user))

  def securedOutput(userProfile: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), String]] = {
    Future.successful {
      Right {
        s"Hello ${userProfile.userId} you are secured"
      }
    }
  }

  val unsecuredTestEndpoint = baseEndpoint
    .in("plugin")
    .in("unsecured")
    .out(stringBody)
    .method(Method.GET)
    .tag("Sample Plugin")
    .serverLogic(_ => unsecureRoute())

  def unsecureRoute(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), String]] = {
    Future.successful(Right("unsecure route"))
  }

}
