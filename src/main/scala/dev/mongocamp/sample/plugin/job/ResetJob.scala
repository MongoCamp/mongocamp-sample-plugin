package dev.mongocamp.sample.plugin.job

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.DatabaseProvider
import dev.mongocamp.sample.plugin.ResetServerPlugin
import dev.mongocamp.server.config.{ConfigHelper, ConfigHolder}
import dev.mongocamp.server.database.{MongoDatabase, RolesDao, UserDao}
import dev.mongocamp.server.service.SystemFileService
import org.quartz.{Job, JobExecutionContext}

class ResetJob extends Job with LazyLogging {

  override def execute(context: JobExecutionContext): Unit = {
    val mongoCampPrefix  = ConfigHolder.authCollectionPrefix.value
    val ignoredDatabases = ConfigHelper.globalConfigStringList("database.ignored")
    val userCollection = s"${mongoCampPrefix}users"
    val rolesCollection = s"${mongoCampPrefix}roles"
    MongoDatabase.databaseProvider.databaseNames.foreach(database => {
      if (ConfigHolder.dbConnectionDatabase.value.equalsIgnoreCase(database)) {
        MongoDatabase.databaseProvider
          .collectionNames(database)
          .foreach {
            case s: String if s.equalsIgnoreCase(userCollection)  =>
              MongoDatabase.databaseProvider.collection(s"$database${DatabaseProvider.CollectionSeparator}$userCollection").deleteMany(Map()).result()
              val userInsert = UserDao().insertMany(List(ResetServerPlugin.defaultAdminUser, ResetServerPlugin.defaultTestUser)).result()
              userInsert
            case s: String if s.equalsIgnoreCase(rolesCollection)  =>
              MongoDatabase.databaseProvider.collection(s"$database${DatabaseProvider.CollectionSeparator}$rolesCollection").deleteMany(Map()).result()
              val roleInsert = RolesDao().insertMany(List(ResetServerPlugin.userRole, ResetServerPlugin.adminRole)).result()
              roleInsert
            case s: String if s.startsWith(mongoCampPrefix) =>
              ""
            case s: String =>
              MongoDatabase.databaseProvider.collection(s"$database${DatabaseProvider.CollectionSeparator}$s").drop().result()
          }
        case class MapCollectionDao(collectionName: String) extends MongoDAO[Map[String, Any]](MongoDatabase.databaseProvider, collectionName)

        val usersInsertResult   = MapCollectionDao("users").insertMany(SystemFileService.readJsonList("sample/users.json")).result()
        val pokemonInsertResult = MapCollectionDao("pokemon").insertMany(SystemFileService.readJsonList("sample/pokedex.json")).result()

        usersInsertResult.wasAcknowledged() && pokemonInsertResult.wasAcknowledged()
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
