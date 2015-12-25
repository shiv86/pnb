package com.pnb.repo.jpa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.Earning.ANNCMT_TIME;

public interface EarningsRepo extends JpaRepository<Earning, Long> {

    Earning findBySymbolAndDate(String symbol, LocalDate date);

    List<Earning> findBySymbolOrderByDateDesc(String symbol);

    @Query("SELECT DISTINCT(e.symbol) FROM Earning e")
    List<String> getAllUniqueSymbol();

    @Query(value = "SELECT DISTINCT(symbol) FROM earning WHERE symbol not like '%.%' order by symbol asc", nativeQuery = true)
    List<String> getAllUSSymbol();

    @Query(value = "SELECT * FROM earning WHERE symbol = :sym AND consensus_eps IS NOT NULL AND reported_eps IS NOT NULL ORDER BY earnings_date ASC", nativeQuery = true)
    List<Earning> getSymbolEarningWherePopulated(@Param("sym") String sym);

    @Query(value = "SELECT * FROM earning WHERE symbol = ?1 AND earnings_date < date(?2) AND consensus_eps IS NOT NULL AND reported_eps IS NOT NULL AND symbol not like '%.%' ORDER BY earnings_date ASC", nativeQuery = true)
    List<Earning> getHistoricalPopulateEarnings(String sym, String earnDate);

    @Query(value = "SELECT * FROM earning WHERE earnings_date = date(?1) AND consensus_eps IS NOT NULL AND symbol not like '%.%'", nativeQuery = true)
    List<Earning> findByDateAndConsensusEPSNotNull(String date);
    
    @Query(value = "SELECT * FROM earning WHERE earnings_date = date(?1) AND consensus_eps IS NOT NULL AND reported_eps IS NOT NULL AND symbol not like '%.%'", nativeQuery = true)
    List<Earning> findByDateAndConsensusANDSurpriseEPSNotNull(String date);

    /*
     * Note earningRepo is used to determine the lastEarningsTaskRun
     */
    @Query(value = "SELECT MAX(earnings_date) from earning where earnings_eps_populated = 't'", nativeQuery = true)
    LocalDate getLastEarningsTaskRunDate();

    @Query(value = "select distinct(anncmt_time) from earning where symbol = ?1 and anncmt_time != 'NOT_SUPPLIED'", nativeQuery = true)
    List<ANNCMT_TIME> getAllDistinctAnncmt(String symbol);

    @Query(value = "select count(*) from earning where symbol = ?1 and anncmt_time = ?2", nativeQuery = true)
    int getAnncmtCountForSymbol(String symbol, String anncmtTime);

    @Query(value = "select * from earning where anncmt_time != 'NOT_SUPPLIED' AND anncmt_time != 'DURING_MKT_HRS' AND symbol not like '%.%' AND earnings_date > '2015-11-01'", nativeQuery = true)
    List<Earning> getAllEarningsWhereTimeIsSuppliedOrderByAsc();
    
    @Query(value = "select count(*) from earning where anncmt_time != 'NOT_SUPPLIED' and symbol not like '%.%", nativeQuery = true)
    List<Earning> getEarningsToPopulateTradeDate();

}
