package dev.mongocamp.sample.plugin.event.listener

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb.{GenericObservable, _}
import dev.mongocamp.sample.plugin.ResetServerPlugin
import dev.mongocamp.server.auth.{AuthHolder, MongoAuthHolder}
import dev.mongocamp.server.database.UserDao
import dev.mongocamp.server.event.user.{DeleteUserEvent, UpdateApiKeyEvent, UpdatePasswordEvent}

class UserChangesListener extends Actor with LazyLogging {
  override def receive: Receive = {
    case deleteEvent: DeleteUserEvent =>
      if (AuthHolder.isMongoDbAuthHolder) {
        if (deleteEvent.deletedUser.equalsIgnoreCase(ResetServerPlugin.adminUser)) {
          val userInsert = UserDao().insertOne(ResetServerPlugin.defaultAdminUser).result()
          userInsert
        }
        if (deleteEvent.deletedUser.equalsIgnoreCase(ResetServerPlugin.userUser)) {
          val userInsert = UserDao().insertOne(ResetServerPlugin.defaultTestUser).result()
          userInsert
        }
      }
    case updateApiKeyEvent: UpdateApiKeyEvent =>
      if (AuthHolder.isMongoDbAuthHolder) {
        if (updateApiKeyEvent.changedUserId.equalsIgnoreCase(ResetServerPlugin.adminUser)) {
          val userDelete = UserDao().deleteOne(Map("userId" -> updateApiKeyEvent.changedUserId)).result()
          val userInsert = UserDao().insertOne(ResetServerPlugin.defaultAdminUser).result()
          userInsert
        }
        if (updateApiKeyEvent.changedUserId.equalsIgnoreCase(ResetServerPlugin.userUser)) {
          val userDelete = UserDao().deleteOne(Map("userId" -> updateApiKeyEvent.changedUserId)).result()
          val userInsert = UserDao().insertOne(ResetServerPlugin.defaultTestUser).result()
          userInsert
        }
      }
    case updatePasswordEvent: UpdatePasswordEvent =>
      if (AuthHolder.isMongoDbAuthHolder) {
        val mongoAuthHolder = AuthHolder.handler.asInstanceOf[MongoAuthHolder]
        if (updatePasswordEvent.changedUserId.equalsIgnoreCase(ResetServerPlugin.adminUser)) {
          mongoAuthHolder.updatePasswordForUser(ResetServerPlugin.adminUser, ResetServerPlugin.adminPwd)
        }
        if (updatePasswordEvent.changedUserId.equalsIgnoreCase(ResetServerPlugin.userUser)) {
          mongoAuthHolder.updatePasswordForUser(ResetServerPlugin.userUser, ResetServerPlugin.userPwd)
        }
      }
  }
}
