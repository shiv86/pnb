package com.pnb.repo.jpa;

import javax.persistence.Embeddable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnb.domain.jpa.TaskMetaData;

@Embeddable
public interface TaskMetaRepo extends JpaRepository<TaskMetaData, Long> {

}
