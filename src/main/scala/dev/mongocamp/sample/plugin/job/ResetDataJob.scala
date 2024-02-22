package dev.mongocamp.sample.plugin.job

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.DatabaseProvider
import dev.mongocamp.sample.plugin.ResetServerPlugin
import dev.mongocamp.sample.plugin.ResetServerPlugin.ConfigKeyDatabaseIgnored
import dev.mongocamp.sample.plugin.service.ResetDataService
import dev.mongocamp.server.config.DefaultConfigurations.{ConfigKeyAuthPrefix, ConfigKeyConnectionDatabase}
import dev.mongocamp.server.database.{MongoDatabase, RolesDao, UserDao}
import dev.mongocamp.server.service.{ConfigurationService, SystemFileService}
import org.quartz.{DisallowConcurrentExecution, Job, JobExecutionContext}

@DisallowConcurrentExecution
class ResetDataJob extends Job with LazyLogging {

  override def execute(context: JobExecutionContext): Unit = {

    val ignoredDatabases = ConfigurationService.getConfigValue[List[String]](ConfigKeyDatabaseIgnored) ++ List("admin", "config", "local")
    MongoDatabase.databaseProvider.databaseNames.foreach(database => {
      if (ConfigurationService.getConfigValue[String](ConfigKeyConnectionDatabase).equalsIgnoreCase(database)) {
        ResetDataService.clearCollections(database)
        ResetDataService.insertData()
      }
      else {
        if (!ignoredDatabases.contains(database)) {
          MongoDatabase.databaseProvider.dropDatabase(database).result()
        }
      }
    })
    ""
  }

}
