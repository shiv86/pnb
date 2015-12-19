package com.pnb.repo.jpa;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnb.domain.jpa.EarnReturn;

public interface EarnReturnRepo extends JpaRepository<EarnReturn, Long> {

    EarnReturn findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

}
