package dev.mongocamp.sample.plugin.event.listener

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb.{GenericObservable, _}
import dev.mongocamp.sample.plugin.ResetServerPlugin.{adminRole, userRole}
import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.database.RolesDao
import dev.mongocamp.server.event.role.{DeleteRoleEvent, UpdateRoleEvent}

class RoleChangesListener extends Actor with LazyLogging {
  override def receive: Receive = {
    case deleteEvent: DeleteRoleEvent =>
      if (AuthHolder.isMongoDbAuthHolder) {
        if (deleteEvent.deletedRole.equalsIgnoreCase(userRole.name)) {
          val userInsert = RolesDao().insertOne(userRole).result()
          userInsert
        }
        if (deleteEvent.deletedRole.equalsIgnoreCase(adminRole.name)) {
          val userInsert = RolesDao().insertOne(adminRole).result()
          userInsert
        }
      }
    case updateEvent: UpdateRoleEvent =>
      if (AuthHolder.isMongoDbAuthHolder) {
        if (updateEvent.role.equalsIgnoreCase(userRole.name)) {
          val userDelete = RolesDao().deleteOne(Map("name" -> updateEvent.role)).result()
          val userInsert = RolesDao().insertOne(userRole).result()
          userInsert
        }
        if (updateEvent.role.equalsIgnoreCase(adminRole.name)) {
          val userDelete = RolesDao().deleteOne(Map("name" -> updateEvent.role)).result()
          val userInsert = RolesDao().insertOne(adminRole).result()
          userInsert
        }
      }
  }
}
