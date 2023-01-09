package dev.mongocamp.sample.plugin

import akka.actor.Props
import dev.mongocamp.sample.plugin.event.listener.{RoleChangesListener, UserChangesListener}
import dev.mongocamp.sample.plugin.job.ResetJob
import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.event.{Event, EventSystem}
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.{AuthorizedCollectionRequest, Grant, Role, UserInformation}
import dev.mongocamp.server.model.{JobConfig, MongoCampConfiguration}
import dev.mongocamp.server.plugin.{JobPlugin, ServerPlugin}
import dev.mongocamp.server.service.ConfigurationService

class ResetServerPlugin extends ServerPlugin {

  override def activate(): Unit = {
    ConfigurationService.registerConfig("rest.admin.user", MongoCampConfiguration.confTypeString)
    ConfigurationService.registerConfig("rest.admin.password", MongoCampConfiguration.confTypeString)
    ConfigurationService.registerConfig("rest.admin.role.name", MongoCampConfiguration.confTypeString)
    ConfigurationService.registerConfig("rest.user.user", MongoCampConfiguration.confTypeString)
    ConfigurationService.registerConfig("rest.user.password", MongoCampConfiguration.confTypeString)
    ConfigurationService.registerConfig("rest.user.role.name", MongoCampConfiguration.confTypeString)
    ConfigurationService.registerConfig("database.ignored", s"List[${MongoCampConfiguration.confTypeString}]")

    val jobUser = UserInformation(this.getClass.getSimpleName, "", None, List())
    try {
      val added = JobPlugin.addJob(JobConfig("ResetDataJob", classOf[ResetJob].getName, "", "0 0/30 * ? * * *", "SampleData"), Some(jobUser))
      if (added) {
        JobPlugin.reloadJobs()
      }
    }
    catch {
      case e: MongoCampException =>
        ""
    }
    val userChangesListener = EventSystem.eventBusActorSystem.actorOf(Props(classOf[UserChangesListener]), "userChangesListener")
    EventSystem.eventStream.subscribe(userChangesListener, classOf[Event])

    val roleChangesListener = EventSystem.eventBusActorSystem.actorOf(Props(classOf[RoleChangesListener]), "roleChangesListener")
    EventSystem.eventStream.subscribe(roleChangesListener, classOf[Event])

  }

}

object ResetServerPlugin {

  lazy val adminUser: String = ConfigurationService.getConfigValue[String]("rest.admin.user")
  lazy val adminPwd: String = ConfigurationService.getConfigValue[String]("rest.admin.password")
  lazy val adminRoleName: String = ConfigurationService.getConfigValue[String]("rest.admin.role.name")
  lazy val adminRole: Role = Role(
    adminRoleName,
    isAdmin = true,
    List(
      Grant(AuthorizedCollectionRequest.all, read = true, write = true, administrate = true, Grant.grantTypeCollection),
      Grant(AuthorizedCollectionRequest.all, read = true, write = true, administrate = true, Grant.grantTypeBucket)
    )
  )

  lazy val defaultAdminUser: UserInformation = UserInformation(adminUser, AuthHolder.handler.encryptPassword(adminPwd), None, List(adminRole.name))

  lazy val userUser: String = ConfigurationService.getConfigValue[String]("rest.user.user")
  lazy val userPwd: String = ConfigurationService.getConfigValue[String]("rest.user.password")
  lazy val userRoleName: String = ConfigurationService.getConfigValue[String]("rest.user.role.name")
  lazy val userRole: Role = Role(
    userRoleName,
    isAdmin = false,
    List(
      Grant("users", read = true, write = false, administrate = false, Grant.grantTypeCollection),
      Grant("pokemon", read = true, write = true, administrate = false, Grant.grantTypeCollection)
    )
  )

  lazy val defaultTestUser: UserInformation = UserInformation(userUser, AuthHolder.handler.encryptPassword(userPwd), None, List(userRole.name))

}