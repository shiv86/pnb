package com.pnb.repo.jpa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pnb.domain.jpa.BuyReturn;

public interface BuyReturnRepo extends JpaRepository<BuyReturn, Long> {

    BuyReturn findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);
    
    @Query(value = "SELECT * FROM buyreturn where symbol = ?1 and trade_date < date(?2)", nativeQuery = true)
    List<BuyReturn> findBuyReturnBeforeDate(String symbol, String tradeDate);

}
