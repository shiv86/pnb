package com.pnb;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pnb.algo.EarningsRevisionGame;
import com.pnb.task.yahoo.YahooParentDataLoadTask;
import com.pnb.task.yahoo.YahooPriceHistoryTask;
import com.pnb.task.yahoo.datacleaner.YahooAnncmtDateCheckerTask;
import com.pnb.task.yahoo.datacleaner.YahooRemoveHistoricalTickFromCurrentTask;
import com.pnb.task.yahoo.datacleaner.YahooRetryDataForErroredTask;

@Component
public class YahooEarningsFeedEngine {

    private final static LocalDate DEFAULT_START_DATE = LocalDate.now().minusDays(30);
    private final static LocalDate DEFAULT_END_DATE = LocalDate.now().plusDays(10);
    private LocalDate startDate = LocalDate.of(2015, 11, 23);// DEFAULT_START_DATE;//
    private LocalDate endDate = LocalDate.of(2015, 11, 25);// LocalDate.now(); //LocalDate.of(2015, 11,
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

    @Autowired
    private YahooAnncmtDateCheckerTask anncmtChecker;

    @PostConstruct
    public void start() {
        // retryErroredTasks.start(startDate, endDate);
        // yahooDataLoadTask.start(startDate,endDate);
        // yahooTickCleaner.start(startDate, endDate);
        // earningRevisionGame.start(startDate, endDate);
        // priceLoadTask.start(startDate, endDate);

        anncmtChecker.start(startDate, startDate);
    }

}
