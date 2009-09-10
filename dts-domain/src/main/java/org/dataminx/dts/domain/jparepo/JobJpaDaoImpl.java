/**
 * Copyright (c) 2009, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
        LOGGER.debug("JobJapDaoImpl findById()");
        return mEntityManager.find(Job.class, id);
    }

    /**
     * {@inheritDoc}
     */
    public Job findByResourceKey(String resourceKey) {
        LOGGER.debug("JobJapDaoImpl findByResourceKey()");
        Query query = mEntityManager.createNamedQuery("Job.findJobByResourceKey");
        query.setParameter("resourceKey", resourceKey);
        return (Job) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    public List<Job> findByUser(String subjectName) {
        LOGGER.debug("JobJapDaoImpl findByUser()");
        Query query = mEntityManager.createNamedQuery("Job.findJobByUser");
        query.setParameter("userDN", subjectName);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public List<Job> findByUserAndStatus(String subjectName, JobStatus status) {
        LOGGER.debug("JobJapDaoImpl findByUserAndStatus()");
        Query query = mEntityManager.createNamedQuery("Job.findJobByUserAndStatus");
        query.setParameter("userDN", subjectName);
        query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrUpdate(Job job) {
        LOGGER.debug("JobJapDaoImpl saveOrUpdate()");
        if (job.getJobId() == null) {
            // save
            LOGGER.debug("save(job)");
            mEntityManager.persist(job);
        }
        else {
            // update
            LOGGER.debug("update(job)");
            mEntityManager.merge(job);
        }
    }

}
