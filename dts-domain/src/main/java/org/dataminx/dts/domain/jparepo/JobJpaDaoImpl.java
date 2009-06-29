package org.dataminx.dts.domain.jparepo;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Job Data Access Object's JPA implementation.
 *
 * @author Gerson Galang
 */
@Transactional
public class JobJpaDaoImpl implements JobDao {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JobJpaDaoImpl.class);

    /** The entity manager. */
    private EntityManager mEntityManager;

    /**
     * Sets the entity manager.
     *
     * @param em the new entity manager
     */
    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.mEntityManager = em;
    }

    /**
     * Gets the entity manager.
     *
     * @return the entity manager
     */
    private EntityManager getEntityManager() {
        return mEntityManager;
    }

    /**
     * {@inheritDoc}
     */
    public Job findById(Long id) {
        return mEntityManager.find(Job.class, id);
    }

    /**
     * {@inheritDoc}
     */
    public Job findByResourceKey(String resourceKey) {
        Query query = mEntityManager.createNamedQuery("Job.findJobByResourceKey");
        query.setParameter("resourceKey", resourceKey);
        return (Job) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    public List<Job> findByUser(String subjectName) {
        Query query = mEntityManager.createNamedQuery("Job.findJobByUser");
        query.setParameter("userDN", subjectName);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public List<Job> findByUserAndStatus(String subjectName, JobStatus status) {
        Query query = mEntityManager.createNamedQuery("Job.findJobByUserAndStatus");
        query.setParameter("userDN", subjectName);
        query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrUpdate(Job job) {
        if (job.getJobId() == null) {
            // save
            LOGGER.info("save(job)");
            mEntityManager.persist(job);
        }
        else {
            // update
            LOGGER.info("update(job)");
            mEntityManager.merge(job);
        }
    }

}
