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
    
    @Query(value = "select max(date) from price_history where symbol = ?1", nativeQuery = true)
    LocalDate getLastPriceHistoryTaskRunDate(String symbol);
    
    /*
     * Important to note and task_name = 'PRICE_HISTORY' and status = 'ERROOR' 
     * will exclude symbol from updating the price history information
     */
    @Query(value = "select distinct(symbol) from earning where symbol not in (select distinct(task_sub_type) from task_meta where task_name = 'PRICE_HISTORY' and status = 'ERROR') and symbol not like '%.%' order by symbol asc", nativeQuery = true)
    List<String> getAllSymbolsForPriceHistoryUpdate();
    
    
    /*
     * 1) All successfully completed PriceData History Load:
     * select * from task_meta where task_name = 'PRICE_HISTORY' and status = 'COMPLETED' order by created_date desc; 
     * 
     * 2) Number of errors:
     * select count(*) from task_meta where task_name = 'PRICE_HISTORY' and status = 'ERROR' and created_date = '2015-12-25'
     * 
     * Errors due to constraint voliation:
     * select count(*) from task_meta where task_name = 'PRICE_HISTORY' and status = 'ERROR' and created_date = '2015-12-25' and message like '%org.hibernate.exception.ConstraintViolationException%';
     * 
     */

}
