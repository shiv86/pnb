package com.pnb.task.yahoo.datacleaner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.Earning.ANNCMT_TIME;
import com.pnb.domain.jpa.PriceHistory;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.repo.jpa.EarningsRepo;
import com.pnb.repo.jpa.PriceHistoryRepo;
import com.pnb.repo.jpa.TaskMetaRepo;
import com.pnb.util.YahooUtil;

@Service
public class YahooAnncmtDateCheckerTask extends YahooDataCleaner {

    // private static final String YAHOO_RETRY_DATALOAD = "YahooRetryDataLoad";

    @Autowired
    TaskMetaRepo taskRepo;

    @Autowired
    EarningsRepo earningsRepo;

    @Autowired
    PriceHistoryRepo priceHistoryRepo;

    @Override
    protected TaskMetaData process() {
        List<Earning> earningsWithTimeSupplied = earningsRepo.getAllEarningsWhereTimeIsSuppliedOrderByAsc();
        int totalBeforeOpenPrediction = 0;
        int totalScoreBOP = 0;
        int totalAfterClosePrediction = 0;
        int totalScoreACP = 0;
        int totalPriceNA = 0;

        for (Earning earn : earningsWithTimeSupplied) {

            LocalDate anncmtDate = earn.getDate();
            LocalDate tPlusOne = anncmtDate.plusDays(1);
            if (YahooUtil.isWeekend(tPlusOne)) {
                tPlusOne = tPlusOne.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            }

            List<PriceHistory> priceList =
                    priceHistoryRepo.getPotentialPriceHistoryForEarnings(earn.getSymbol(), anncmtDate.toString(), tPlusOne.toString());

            LocalDate predictedEarningDate = null;

            if (priceList.size() == 2) {
                long tDayVol = priceList.get(0).getVolume();
                long tPlusOneVol = priceList.get(1).getVolume();
                boolean sigDiff = Math.abs(tDayVol-tPlusOneVol) > 1000000;

                if (sigDiff && tDayVol > tPlusOneVol) {
                    predictedEarningDate = priceList.get(0).getDate();
                } else if (sigDiff && tDayVol < tPlusOneVol) {
                    predictedEarningDate = priceList.get(1).getDate();
                }

                if (!CollectionUtils.isEmpty(priceList) && predictedEarningDate != null) {
                    if (predictedEarningDate.isAfter(earn.getDate())) {
                        totalAfterClosePrediction++;
                        if (ANNCMT_TIME.AFTER_CLOSE.equals(earn.getAnncmtTime())) {
                            totalScoreACP++;
                        } else {
                            System.err.println(earn.toString());
                            System.err.println("Predicte AFTER_CLOSE Actual:" + earn.getAnncmtTime());
                        }
                    } else if (predictedEarningDate.equals(earn.getDate())) {
                        totalBeforeOpenPrediction++;
                        if (ANNCMT_TIME.BEFORE_OPEN.equals(earn.getAnncmtTime())) {
                            totalScoreBOP++;
                        } else {
                            System.err.println(earn.toString());
                            System.err.println("Predicte BEFORE_OPEN Actual:" + earn.getAnncmtTime());
                        }
                    }
                } else {
                    totalPriceNA++;
                }
            }

        }

        System.out.println("totalBeforeOpenPrediction:" + totalBeforeOpenPrediction + ", totalScoreBOP: " + totalScoreBOP);
        System.out.println("totalAfterClosePrediction:" + totalAfterClosePrediction + ", totalScoreACP: " + totalScoreACP);
        System.out.println("totalEarnings:" + earningsWithTimeSupplied.size() + ", totalPriceEmpty: " + totalPriceNA);
        return null;
    }
}
