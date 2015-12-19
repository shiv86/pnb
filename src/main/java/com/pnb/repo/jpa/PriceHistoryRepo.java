package com.pnb.repo.jpa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pnb.domain.jpa.PriceHistory;

public interface PriceHistoryRepo extends JpaRepository<PriceHistory, Long> {

    @Query(value = "select * from price_history where symbol = ?1 and date in ( date(?2), date(?3)) ORDER BY date ASC", nativeQuery = true)
    List<PriceHistory> getPotentialPriceHistoryForEarnings(String sym, String earnDate, String tPlusOneEarnDate);

    PriceHistory findBySymbolAndDate(String Symbol, LocalDate date);

}
