package com.pnb.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnb.domain.jpa.EarningsMetaData;

public interface EarningsMetaDataRepo extends JpaRepository<EarningsMetaData, Long> {

}
