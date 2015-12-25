package com.pnb;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pnb.algo.earnings.BuyRecommendationTask;
import com.pnb.algo.earnings.BuyReturnsTask;
import com.pnb.algo.earnings.CSVBuyRecommendationFileBuilderTask;
import com.pnb.algo.earnings.EarnReturnsTask;
import com.pnb.algo.earnings.EarningsRevisionGame;
import com.pnb.task.yahoo.TradeDateTask;
import com.pnb.task.yahoo.YahooParentDataLoadTask;
import com.pnb.task.yahoo.YahooPriceHistoryTask;
import com.pnb.task.yahoo.datacleaner.YahooAnncmtDateCheckerTask;
import com.pnb.task.yahoo.datacleaner.YahooRemoveHistoricalTickFromCurrentTask;
import com.pnb.task.yahoo.datacleaner.YahooRetryDataForErroredTask;

@Component
public class YahooEarningsFeedEngine {

    private final static LocalDate DEFAULT_START_DATE = LocalDate.now().minusDays(30);
    private final static LocalDate DEFAULT_END_DATE = LocalDate.now().plusDays(10);
    private LocalDate startDate = LocalDate.of(2001, 01, 01);// DEFAULT_START_DATE;//
    private LocalDate endDate = LocalDate.of(2001, 01, 01);// LocalDate.now(); //LocalDate.of(2015, 11,
                                                          // 13);//startDate.now();//LocalDate.of(2015, 11, 30); ////
                                                          // DEFAULT_END_DATE; //

    @Autowired
    private YahooParentDataLoadTask yahooDataLoadTask;
    @Autowired
    private YahooRemoveHistoricalTickFromCurrentTask yahooTickCleaner;
    @Autowired
    private EarningsRevisionGame earningRevisionGame;

    @Autowired
    private YahooRetryDataForErroredTask retryErroredTasks;

    @Autowired
    private YahooPriceHistoryTask priceLoadTask;

    // Attempted to determine earnings date by volume
    @Autowired
    private YahooAnncmtDateCheckerTask anncmtChecker;

    @Autowired
    private BuyRecommendationTask buyRecommendationTask;

    @Autowired
    private BuyReturnsTask returnsTask;

    @Autowired
    private CSVBuyRecommendationFileBuilderTask csvBuilder;

    @Autowired
    private EarnReturnsTask earnReturnsTask;
    
    @Autowired
    private TradeDateTask tradeDateTask;

    @PostConstruct
    public void start() {
        // retryErroredTasks.start(startDate, endDate);
         yahooDataLoadTask.start(startDate,endDate);
        // yahooTickCleaner.start(startDate, endDate);
        // earningRevisionGame.start(startDate, endDate);
        // priceLoadTask.start(startDate, endDate);
        // anncmtChecker.start(startDate, startDate);
        // buyRecommendationTask.start(startDate, endDate);
        // returnsTask.start(startDate, endDate);
        // csvBuilder.start(startDate, endDate);
        // earnReturnsTask.start(startDate, endDate);
        // tradeDateTask.start(startDate, endDate);
    }

}
