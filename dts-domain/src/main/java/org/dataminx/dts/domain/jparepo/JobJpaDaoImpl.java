package org.dataminx.dts.domain.jparepo;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.repo.JobDao;

@Transactional
public class JobJpaDaoImpl implements JobDao {

	protected final Log logger = LogFactory.getLog(getClass());

	private EntityManager em;

	//@PersistenceUnit
	//private EntityManagerFactory emf;

    @PersistenceContext(unitName="punit")
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    private EntityManager getEntityManager() {
        return em;
    }

	public Job findById(Long id) {
		return em.find(Job.class, id);
	}

	public Job findByResourceKey(String resourceKey) {
		return null;
	}

	public void saveOrUpdate(Job job) {
		//em = emf.createEntityManager();
		//logger.info(emf);
        if (job.getJobId() == null) {
            // save
        	logger.info("save(job)");
        	logger.info(job);
            em.persist(job);
        } else {
            // update
        	logger.info("update(job)");
            em.merge(job);
        }
        //em.close();
	}

}
