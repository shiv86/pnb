package com.pnb.repo.jpa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pnb.domain.jpa.BuyRecommend;

public interface BuyRecommendRepo extends JpaRepository<BuyRecommend, Long> {

    @Query(value = "SELECT * FROM buyrecommend ORDER BY trade_date ASC", nativeQuery = true)
    List<BuyRecommend> findAllOrderByTradeDateAsc();

    @Query(value = "SELECT * FROM buyrecommend WHERE trade_date >= date(?1) AND trade_date <= date(?2) ORDER BY trade_date ASC", nativeQuery = true)
    List<BuyRecommend> findAllGreaterThanOrEqualToStartAndEndDate(String startDate, String endDate);
    
    BuyRecommend findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

}
