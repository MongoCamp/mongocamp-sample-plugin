package dev.mongocamp.sample.plugin.job

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.config.{ ConfigHelper, ConfigHolder }
import dev.mongocamp.server.database.MongoDatabase
import org.quartz.{ Job, JobExecutionContext }

class ResetJob extends Job with LazyLogging {

  override def execute(context: JobExecutionContext): Unit = {
    val ignoredDatabases = ConfigHelper.globalConfigStringList("ignored.database")
    MongoDatabase.databaseProvider.databaseNames.foreach(database => {
      if (ConfigHolder.dbConnectionDatabase.value.equalsIgnoreCase(database)) {
        logger.error(s"${database} should cleaned up.")
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
