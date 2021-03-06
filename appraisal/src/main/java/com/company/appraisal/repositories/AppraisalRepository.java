package com.company.appraisal.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.company.appraisal.entities.Appraisal;
import com.company.appraisal.entities.AppraisalCategory;
import com.company.appraisal.entities.AppraisalPk;
import com.company.appraisal.entities.Project;

@Repository
public interface AppraisalRepository extends CrudRepository<Appraisal, AppraisalPk>{
	
	Appraisal findByAppraisalPk(AppraisalPk appraisalPk);
	
	Appraisal findByAppraisalPkAndProject(AppraisalPk appraisalPk, Project project);
	
	Iterable<Appraisal> findBySignedOff(boolean signedOff);
	
	Iterable<Appraisal> findBySignedOffAndAppraisalPkAppraisalCategory(boolean signedOff,  AppraisalCategory category);
	
	Iterable<Appraisal> findBySignedOffAndAppraisalPkAppraisalCategoryAndProject(boolean signedOff,  AppraisalCategory category, Project project);
}
