package dev.mongocamp.sample.plugin.job

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.DatabaseProvider
import dev.mongocamp.server.config.{ConfigHelper, ConfigHolder}
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.service.SystemFileService
import org.quartz.{Job, JobExecutionContext}

class ResetJob extends Job with LazyLogging {

  override def execute(context: JobExecutionContext): Unit = {
    val mongoCampPrefix  = ConfigHolder.authCollectionPrefix.value
    val ignoredDatabases = ConfigHelper.globalConfigStringList("database.ignored")
    MongoDatabase.databaseProvider.databaseNames.foreach(database => {
      if (ConfigHolder.dbConnectionDatabase.value.equalsIgnoreCase(database)) {
        MongoDatabase.databaseProvider
          .collectionNames(database)
          .foreach {
            case s"${mongoCampPrefix}users" =>
              ""
            case s"${mongoCampPrefix}roles" =>
              ""
            case s: String if s.startsWith(mongoCampPrefix) =>
              ""
            case s: String =>
              MongoDatabase.databaseProvider.collection(s"$database${DatabaseProvider.CollectionSeparator}$s").drop().result()
          }
        case class MapCollectionDao(collectionName: String) extends MongoDAO[Map[String, Any]](MongoDatabase.databaseProvider, collectionName)

        val usersInsertResult = MapCollectionDao("users").insertMany(SystemFileService.readJsonList("sample/users.json")).result()
        val pokemonInsertResult = MapCollectionDao("pokemon").insertMany(SystemFileService.readJsonList("sample/pokedex.json")).result()

        ""
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
