package au.gov.ga.hydroid;

import au.gov.ga.hydroid.job.EnhancerJob;
import au.gov.ga.hydroid.utils.AutowiringSpringBeanJobFactory;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Created by u24529 on 1/03/2016.
 */
@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
public class SchedulerConfiguration {

   @Bean
   public JobFactory jobFactory(ApplicationContext applicationContext)
   {
      AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
      jobFactory.setApplicationContext(applicationContext);
      return jobFactory;
   }

   @Bean
   public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory,
                                                    @Qualifier("enhancerJobTrigger") Trigger enhancerJobTrigger) throws IOException {
      SchedulerFactoryBean factory = new SchedulerFactoryBean();
      // this allows to update triggers in DB when updating settings in config file:
      factory.setOverwriteExistingJobs(true);
      factory.setDataSource(dataSource);
      factory.setJobFactory(jobFactory);

      factory.setQuartzProperties(quartzProperties());
      factory.setTriggers(enhancerJobTrigger);

      return factory;
   }

   @Bean
   public Properties quartzProperties() throws IOException {
      PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
      propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
      propertiesFactoryBean.afterPropertiesSet();
      return propertiesFactoryBean.getObject();
   }

   @Bean
   public JobDetailFactoryBean enhancerJobDetail() {
      return createJobDetail(EnhancerJob.class);
   }

   @Bean(name = "enhancerJobTrigger")
   public SimpleTriggerFactoryBean sampleJobTrigger(@Qualifier("enhancerJobDetail") JobDetail jobDetail,
                                                    @Value("${enhancer.job.frequency}") long frequency) {
      return createTrigger(jobDetail, frequency);
   }

   private static JobDetailFactoryBean createJobDetail(Class jobClass) {
      JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
      factoryBean.setJobClass(jobClass);
      factoryBean.setDurability(true);
      return factoryBean;
   }

   private static SimpleTriggerFactoryBean createTrigger(JobDetail jobDetail, long pollFrequencyHours) {
      SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
      factoryBean.setJobDetail(jobDetail);
      factoryBean.setStartDelay(0L);
      factoryBean.setRepeatInterval(Duration.ofMinutes(pollFrequencyHours).toMillis());
      factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
      factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
      return factoryBean;
   }

}
