package dev.mongocamp.sample.plugin

import dev.mongocamp.sample.plugin.job.ResetJob
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.JobConfig
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.plugin.{JobPlugin, ServerPlugin}

class ResetServerPlugin extends ServerPlugin {

  override def activate(): Unit = {
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

}
