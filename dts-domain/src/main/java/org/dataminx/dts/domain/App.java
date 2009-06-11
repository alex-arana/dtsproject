package org.dataminx.dts.domain;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import org.dataminx.dts.domain.repo.JobDao;
import org.dataminx.dts.domain.model.Job;

/**
 * Hello world!
 *
 */
public class App {
	private BeanFactory factory;

	public App(String beanConfigFilename) {
		factory = new XmlBeanFactory(new ClassPathResource(beanConfigFilename));
	}

	public static void main(String[] args) {
		App app = new App("applicationContext.xml");
		app.createJobEntry();
	}

	public void createJobEntry() {
		JobDao jobRepo = (JobDao) factory.getBean("jobRepository");

		Job job = new Job();
		job.setJobName("hello");
		job.setJobResourceKey("http://abcd");

		jobRepo.saveOrUpdate(job);
	}
}
