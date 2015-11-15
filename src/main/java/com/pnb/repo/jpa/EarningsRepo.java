package com.pnb.repo.jpa;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnb.domain.jpa.Earnings;

public interface EarningsRepo extends JpaRepository<Earnings, Long> {
    
    Earnings findBySymbolAndDate(String symbol,LocalDate date);

}
