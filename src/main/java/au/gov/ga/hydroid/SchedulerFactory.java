package au.gov.ga.hydroid;

import au.gov.ga.hydroid.job.EnhancerJob;
import au.gov.ga.hydroid.utils.HydroidException;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by u24529 on 15/04/2016.
 */
public class SchedulerFactory extends SchedulerFactoryBean {

   private ApplicationContext applicationContext;

   public SchedulerFactory(ApplicationContext context, DataSource dataSource, JobFactory jobFactory,
                           @Qualifier("enhancerJobTrigger") Trigger enhancerJobTrigger) {
      logger.debug("SchedulerFactory - started");
      this.setApplicationContext(context);
      this.setConfigLocation(new ClassPathResource("/quartz.properties"));
      this.setOverwriteExistingJobs(true);
      this.setDataSource(dataSource);
      this.setJobFactory(jobFactory);
      this.setTriggers(enhancerJobTrigger);
      logger.debug("SchedulerFactory - finished");
   }

   @Override
   public void setApplicationContext(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
      super.setApplicationContext(applicationContext);
   }

   public boolean isThereAnyJobRunning() {
      try {
         List<JobExecutionContext> jobs = getScheduler().getCurrentlyExecutingJobs();
         if (jobs == null || jobs.isEmpty()){
            return false;
         }
         for (JobExecutionContext job : jobs) {
            if (job.getJobDetail().getJobClass().equals(EnhancerJob.class)) {
               return true;
            }
         }
      } catch (Exception e) {
         throw new HydroidException(e);
      }
      return false;
   }

   public boolean triggerJob() {
      try {
         // Trigger job manually
         JobDetail jobDetail = (JobDetail) applicationContext.getBean("enhancerJobDetail");
         if (jobDetail != null) {
            Scheduler scheduler = this.getScheduler();
            scheduler.triggerJob(jobDetail.getKey());
         }
      } catch (Exception e) {
         throw new HydroidException(e);
      }
      return false;
   }

}
