package dev.mongocamp.sample.plugin

import akka.actor.Props
import dev.mongocamp.sample.plugin.ResetServerPlugin._
import dev.mongocamp.sample.plugin.event.listener.{RoleChangesListener, UserChangesListener}
import dev.mongocamp.sample.plugin.job.{RebootJob, ResetDataJob}
import dev.mongocamp.sample.plugin.service.ResetDataService
import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.database.{ConfigDao, MongoDatabase}
import dev.mongocamp.server.event.{Event, EventSystem}
import dev.mongocamp.server.model.auth.{AuthorizedCollectionRequest, Grant, Role, UserInformation}
import dev.mongocamp.server.model.{JobConfig, MongoCampConfiguration}
import dev.mongocamp.server.plugin.{JobPlugin, ServerPlugin}
import dev.mongocamp.server.service.ConfigurationService
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.config.DefaultConfigurations.{ConfigKeyPluginsMavenRepositories, ConfigKeyPluginsUrls}

class ResetServerPlugin extends ServerPlugin {

  private lazy val scheduler = {
    val fact = new StdSchedulerFactory()
    fact.initialize("quarz/reboot-quartz.config")
    fact.getScheduler()
  }

  override def activate(): Unit = {

    ConfigurationService.registerConfig(ConfigKeyAdminUser, MongoCampConfiguration.confTypeString, Some("admin"))
    ConfigurationService.registerConfig(ConfigKeyAdminPassword, MongoCampConfiguration.confTypeString, Some("admin"))
    ConfigurationService.registerConfig(ConfigKeyAdminRole, MongoCampConfiguration.confTypeString, Some("adminRole"))
    ConfigurationService.registerConfig(ConfigKeyUserUser, MongoCampConfiguration.confTypeString, Some("user"))
    ConfigurationService.registerConfig(ConfigKeyUserPassword, MongoCampConfiguration.confTypeString, Some("user"))
    ConfigurationService.registerConfig(ConfigKeyUserRole, MongoCampConfiguration.confTypeString, Some("userRole"))
    ConfigurationService.registerConfig(ConfigKeyDatabaseIgnored, s"List[${MongoCampConfiguration.confTypeString}]", Some(List()))

    try {
      val jobUser = UserInformation(this.getClass.getSimpleName, "", None, List())
      JobPlugin.addJob(JobConfig("ResetDataJob", classOf[ResetDataJob].getName, "", "0 0/30 * ? * * *", "SampleData"), Some(jobUser))
    }
    catch {
      case e: Exception =>
        ""
    }
    val userChangesListener = EventSystem.eventBusActorSystem.actorOf(Props(classOf[UserChangesListener]), "userChangesListener")
    EventSystem.eventStream.subscribe(userChangesListener, classOf[Event])

    val roleChangesListener = EventSystem.eventBusActorSystem.actorOf(Props(classOf[RoleChangesListener]), "roleChangesListener")
    EventSystem.eventStream.subscribe(roleChangesListener, classOf[Event])

    ResetDataService.clearCollections(MongoDatabase.databaseProvider.DefaultDatabaseName)
    ResetDataService.insertData()

    registerRebootJobAtNewScheduler()

    val deleteResponse = ConfigDao().deleteMany(Map("$or" -> List(Map("key" -> ConfigKeyPluginsUrls), Map("key" -> ConfigKeyPluginsMavenRepositories)))).result(300)
    deleteResponse
  }

  private def registerRebootJobAtNewScheduler(): Unit = {

    val jobName  = "RebootJob"
    val jobGroup = "system"
    val job      = newJob(classOf[RebootJob]).withIdentity(jobName, jobGroup).build
    val trigger = newTrigger()
      .withIdentity(s"${jobName}Trigger", jobGroup)
      .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 ? * * *"))
      .withPriority(1)
      .forJob(job)
      .build()

    scheduler.scheduleJob(job, trigger)
    ""
  }
}

object ResetServerPlugin {

  val ConfigKeyAdminUser       = "REST_ADMIN_USER"
  val ConfigKeyAdminPassword   = "REST_ADMIN_PASSWORD"
  val ConfigKeyAdminRole       = "REST_ADMIN_ROLE_NAME"
  val ConfigKeyUserUser        = "REST_USER_USER"
  val ConfigKeyUserPassword    = "REST_USER_PASSWORD"
  val ConfigKeyUserRole        = "REST_USER_ROLE_NAME"
  val ConfigKeyDatabaseIgnored = "DATABASE_IGNORED"

  lazy val adminUser: String     = ConfigurationService.getConfigValue[String](ConfigKeyAdminUser)
  lazy val adminPwd: String      = ConfigurationService.getConfigValue[String](ConfigKeyAdminPassword)
  lazy val adminRoleName: String = ConfigurationService.getConfigValue[String](ConfigKeyAdminRole)
  lazy val adminRole: Role = Role(
    adminRoleName,
    isAdmin = true,
    List(
      Grant(AuthorizedCollectionRequest.all, read = true, write = true, administrate = true, Grant.grantTypeCollection),
      Grant(AuthorizedCollectionRequest.all, read = true, write = true, administrate = true, Grant.grantTypeBucket)
    )
  )

  lazy val defaultAdminUser: UserInformation = UserInformation(adminUser, AuthHolder.handler.encryptPassword(adminPwd), None, List(adminRole.name))

  lazy val userUser: String     = ConfigurationService.getConfigValue[String](ConfigKeyUserUser)
  lazy val userPwd: String      = ConfigurationService.getConfigValue[String](ConfigKeyUserPassword)
  lazy val userRoleName: String = ConfigurationService.getConfigValue[String](ConfigKeyUserRole)
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
