package dev.mongocamp.sample.plugin.service

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.DatabaseProvider
import dev.mongocamp.sample.plugin.ResetServerPlugin
import dev.mongocamp.server.config.DefaultConfigurations.ConfigKeyAuthPrefix
import dev.mongocamp.server.database.{ MongoDatabase, RolesDao, UserDao }
import dev.mongocamp.server.service.{ ConfigurationService, SystemFileService }

object ResetDataService {
  private val mongoCampPrefix = ConfigurationService.getConfigValue[String](ConfigKeyAuthPrefix)
  private val userCollection  = s"${mongoCampPrefix}users"
  private val rolesCollection = s"${mongoCampPrefix}roles"
  private val jobsCollection = s"${mongoCampPrefix}jobs"
  private val tokenCollection = s"${mongoCampPrefix}token_cache"
  private case class MapCollectionDao(collectionName: String) extends MongoDAO[Map[String, Any]](MongoDatabase.databaseProvider, collectionName)

  def insertData(): Boolean = {
    val maxWait = 300
    val usersInsertResult   = MapCollectionDao("users").insertMany(SystemFileService.readJsonList("sample/users.json")).result(maxWait)
    val pokemonInsertResult = MapCollectionDao("pokemon").insertMany(SystemFileService.readJsonList("sample/pokedex.json")).result(maxWait)

    val users = List(ResetServerPlugin.defaultTestUser, ResetServerPlugin.defaultAdminUser)
    val user = UserDao().deleteMany(Map()).map(_ => {
      val userResult = UserDao().insertMany(users).result(maxWait)
      userResult.wasAcknowledged()
    }).result(maxWait)

    val roles = RolesDao().deleteMany(Map()).map(_ => {
      val adminRoleResult =     RolesDao().insertMany(List(ResetServerPlugin.adminRole, ResetServerPlugin.userRole)).result()
      adminRoleResult.wasAcknowledged()
    }).result(maxWait)

    usersInsertResult.wasAcknowledged() && pokemonInsertResult.wasAcknowledged() && roles && user
  }

  def clearCollections(database: String): Unit = {
    MongoDatabase.databaseProvider
      .collectionNames(database)
      .foreach {
        case s: String if s.equalsIgnoreCase(userCollection) =>
          val deleteResponse = UserDao().deleteMany(Map()).result()
          ""
        case s: String if s.equalsIgnoreCase(rolesCollection) =>
          val deleteResponse = MongoDatabase.databaseProvider.collection(s"$database${DatabaseProvider.CollectionSeparator}$s").deleteMany(Map()).result()
          ""
        case s: String if s.equalsIgnoreCase(tokenCollection) =>
          val deleteResponse = MongoDatabase.databaseProvider.collection(s"$database${DatabaseProvider.CollectionSeparator}$s").deleteMany(Map()).result()
          ""
        case s: String if s.startsWith(mongoCampPrefix) =>
          ""
        case s: String =>
          val deleteResponse = MongoDatabase.databaseProvider.collection(s"$database${DatabaseProvider.CollectionSeparator}$s").drop().result()
          ""
      }
  }

}
