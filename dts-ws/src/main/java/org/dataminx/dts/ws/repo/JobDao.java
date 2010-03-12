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
package org.dataminx.dts.ws.repo;

import org.dataminx.dts.ws.model.Job;

import org.dataminx.dts.common.model.JobStatus;

import java.util.List;

/**
 * The Job Data Access Object. This provides operations for accessing and making changes to the Job entity.
 *
 * @author Gerson Galang
 */
public interface JobDao extends Dao {

    /**
     * Find the job given its id.
     *
     * @param id the job identifier
     * @return the job
     */
    Job findById(Long id);

    /**
     * Find the job given its resource key.
     *
     * @param resourceKey the job's resource key
     * @return the job
     */
    Job findByResourceKey(String resourceKey);

    /**
     * Save or update the changes to the Job.
     *
     * @param job the job to be saved or updated
     */
    void saveOrUpdate(Job job);

    /**
     * Find all the jobs owned by the user given his/her DN (Distinguished Name).
     *
     * @param subjectName the user's DN (Distinguished Name)
     * @return the list< job> of jobs that matched the selection criteria
     */
    List<Job> findByUser(String subjectName);

    /**
     * Find by jobs having the following status and owned by the user with DN subjectName.
     *
     * @param subjectName the user's DN (Distinguished Name)
     * @param status the status of the job
     * @return the list< job> of jobs that matched the selection criteria
     */
    List<Job> findByUserAndStatus(String subjectName, JobStatus status);

    // find all pending jobs by user
    // find all jobs owned by user
    // find all running jobs by user

}
