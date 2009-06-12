package org.dataminx.dts.domain;

import org.springframework.context.ApplicationContext;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.beans.factory.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.dataminx.dts.domain.repo.JobDao;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;

/**
 * Hello world!
 *
 */
public class App {
	private final ApplicationContext mContext;

	public App(final String springClasspath) {
		mContext = new ClassPathXmlApplicationContext(springClasspath);
	}

	public static void main(final String[] args) {
		App app = new App("applicationContext.xml");
		app.createJobEntry();
	}

	public void createJobEntry() {
		JobDao jobRepo = (JobDao) mContext.getBean("jobRepository");

		Job job = new Job();
		job.setJobName("hello");
		job.setJobResourceKey("http://abcd");
		job.setJobStatus(JobStatus.CREATED);

		jobRepo.saveOrUpdate(job);
	}
}