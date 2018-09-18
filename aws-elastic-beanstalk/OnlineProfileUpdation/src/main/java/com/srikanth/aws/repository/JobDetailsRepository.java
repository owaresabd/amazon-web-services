package com.srikanth.aws.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import com.srikanth.aws.entity.JobDetails;

/**
 * Job Details Repository to perform the database operations
 * on job_details table
 *
 * @author  Srikanth Sambirli
 * @version 2.0
 * @since   2018-06-05
 */
@Repository
public interface JobDetailsRepository extends JpaRepository<JobDetails, Integer>,
			QueryByExampleExecutor<JobDetails> {

	List<JobDetails> findByExperience(Integer experience);
}
