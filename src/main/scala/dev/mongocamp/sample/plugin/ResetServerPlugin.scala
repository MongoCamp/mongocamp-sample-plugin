package dev.mongocamp.sample.plugin

import akka.actor.Props
import dev.mongocamp.sample.plugin.event.listener.UserChangesListener
import dev.mongocamp.sample.plugin.job.ResetJob
import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.config.ConfigHelper
import dev.mongocamp.server.event.{Event, EventSystem}
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.JobConfig
import dev.mongocamp.server.model.auth.{AuthorizedCollectionRequest, Grant, Role, UserInformation}
import dev.mongocamp.server.plugin.{JobPlugin, ServerPlugin}

class ResetServerPlugin extends ServerPlugin {

  override def activate(): Unit = {
    checkOrSetDefaultValue("rest.admin.user", "admin")
    checkOrSetDefaultValue("rest.admin.password", "admin")
    checkOrSetDefaultValue("rest.admin.role.name", "adminRole")
    checkOrSetDefaultValue("rest.user.user", "user")
    checkOrSetDefaultValue("rest.user.password", "user")
    checkOrSetDefaultValue("rest.user.role.name", "userRole")

    val jobUser = UserInformation(this.getClass.getSimpleName, "", None, List())
    try {
      val added = JobPlugin.addJob(jobUser, JobConfig("ResetDataJob", classOf[ResetJob].getName, "", "0 0/30 * ? * * *", "SampleData"))
      if (added) {
        JobPlugin.reloadJobs()
      }
    }
    catch {
      case e: MongoCampException =>
        ""
    }
  }

  private def checkOrSetDefaultValue(configPath: String, defaultValue: String) = {
    val confString = try{
      ConfigHelper.globalConfigString(configPath)
    } catch {
      case _: Exception => ""
    }
    if (confString.trim.equalsIgnoreCase("")) {
      val systemSettingKey           = configPath.toUpperCase().replace(".", "_")
      System.setProperty(systemSettingKey, defaultValue)
    }
  }

}

object ResetServerPlugin {

  lazy val adminUser: String = ConfigHelper.globalConfigString("rest.admin.user")
  lazy val adminPwd: String = ConfigHelper.globalConfigString("rest.admin.password")
  lazy val adminRoleName: String = ConfigHelper.globalConfigString("rest.admin.role.name")
  lazy val adminRole: Role = Role(
    adminRoleName,
    isAdmin = true,
    List(
      Grant(AuthorizedCollectionRequest.all, read = true, write = true, administrate = true, Grant.grantTypeCollection),
      Grant(AuthorizedCollectionRequest.all, read = true, write = true, administrate = true, Grant.grantTypeBucket)
    )
  )

  lazy val defaultAdminUser: UserInformation = UserInformation(adminUser, AuthHolder.handler.encryptPassword(adminPwd), None, List(adminRole.name))

  lazy val userUser: String = ConfigHelper.globalConfigString("rest.user.user")
  lazy val userPwd: String = ConfigHelper.globalConfigString("rest.user.password")
  lazy val userRoleName: String = ConfigHelper.globalConfigString("rest.user.role.name")
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