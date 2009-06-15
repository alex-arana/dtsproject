package org.dataminx.dts.domain.jparepo;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JobJpaDaoImpl implements JobDao {

    protected final Log logger = LogFactory.getLog(getClass());

    private EntityManager em;

    private Query query;

    @PersistenceContext
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
        query = em.createNamedQuery("Job.findJobByResourceKey");
        query.setParameter("jobResourceKey", resourceKey);
        return (Job) query.getSingleResult();
    }

    public List<Job> findByUser(String subjectName) {
        query = em.createNamedQuery("Job.findJobByUser");
        query.setParameter("userDN", subjectName);
        return query.getResultList();
    }

    public List<Job> findByUserAndStatus(String subjectName, JobStatus status) {
        query = em.createNamedQuery("Job.findJobByUserAndStatus");
        query.setParameter("userDN", subjectName);
        query.setParameter("status", status);
        return query.getResultList();
    }

    public void saveOrUpdate(Job job) {
        if (job.getJobId() == null) {
            // save
            logger.info("save(job)");
            em.persist(job);
        }
        else {
            // update
            logger.info("update(job)");
            em.merge(job);
        }
    }

}
