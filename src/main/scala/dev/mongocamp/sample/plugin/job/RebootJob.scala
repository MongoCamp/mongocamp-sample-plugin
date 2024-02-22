package dev.mongocamp.sample.plugin.job

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.DatabaseProvider
import dev.mongocamp.sample.plugin.ResetServerPlugin
import dev.mongocamp.sample.plugin.ResetServerPlugin.ConfigKeyDatabaseIgnored
import dev.mongocamp.server.config.DefaultConfigurations.{ConfigKeyAuthPrefix, ConfigKeyConnectionDatabase}
import dev.mongocamp.server.database.{MongoDatabase, RolesDao, UserDao}
import dev.mongocamp.server.service.{ConfigurationService, SystemFileService}
import org.quartz.{DisallowConcurrentExecution, Job, JobExecutionContext}

@DisallowConcurrentExecution
class RebootJob extends Job with LazyLogging {

  override def execute(context: JobExecutionContext): Unit = {
    val ignoredDatabases = ConfigurationService.getConfigValue[List[String]](ConfigKeyDatabaseIgnored)
    MongoDatabase.databaseProvider.databaseNames.foreach(database => {
      if (!ignoredDatabases.contains(database)) {
        try{
          MongoDatabase.databaseProvider.dropDatabase(database).result()
        } catch {
          case e: Exception =>
            e
        }
      }
    })
    System.exit(0)
  }

}
