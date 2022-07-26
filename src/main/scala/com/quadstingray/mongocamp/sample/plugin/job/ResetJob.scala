package com.quadstingray.mongocamp.sample.plugin.job

import com.typesafe.scalalogging.LazyLogging
import org.quartz.{ Job, JobExecutionContext }

class ResetJob extends Job with LazyLogging {

  override def execute(context: JobExecutionContext): Unit = {
    logger.error(s"${this.getClass.getSimpleName} executed")
  }

}
