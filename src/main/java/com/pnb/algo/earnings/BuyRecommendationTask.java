package com.pnb.algo.earnings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.algo.earnings.EarningRank.QUAD;
import com.pnb.algo.earnings.SurpriseIndexService.SurpriseIndex;
import com.pnb.domain.jpa.BuyRecommend;
import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.repo.jpa.EarningsRepo;
import com.pnb.repo.jpa.RepoService;
import com.pnb.task.Task;
import com.pnb.util.YahooUtil;

@Service
public class BuyRecommendationTask extends Task {

    private static final String BUY_RECOMMENDATION = "BUY_RECOMMENDATION";

    @Autowired
    private SurpriseIndexService indexService;

    @Autowired
    private EarningsRepo earnRepo;

    @Autowired
    private RepoService repoService;
    
    @Autowired
    private YahooUtil yahooUtil;

    @Override
    protected TaskMetaData process() {
        int totalPredictions = 0;
        int successfulPredictions = 0;
        int buysSkippdeDueToNullTradeDate = 0;
        List<BuyRecommend> allBuysRecommended = new ArrayList<BuyRecommend>();
        try {
            while (!taskDate.isAfter((taskEndDate))) {
                List<Earning> earnings = earnRepo.findByDateAndConsensusEPSNotNull(taskDate.toString());
                for (Earning earning : earnings) {
                    SurpriseIndex surpriseIndex = indexService.getSurpriseIndex(earning.getSymbol(), earning.getDate());
                    if (Earning.postiveSurpriseIsPredicted(surpriseIndex.value) && QUAD.QPRPS.equals(surpriseIndex.quad)) {
                        totalPredictions++;
                        System.err.println("Should BUY:" + earning.getSymbol() + ", Date:" + earning.getDate() + ", Surprise Index:"
                                + surpriseIndex.value);
                        System.err.println(earning.toString());

                        LocalDate tradeDate = yahooUtil.getTradeDate(earning);
                        if (tradeDate != null) {
                            Double earningsSurprise = earning.getSurprisePercentage();
                            BuyRecommend buy = new BuyRecommend(earning.getSymbol(), surpriseIndex.value, earning.getDate(), tradeDate);
                            if (earningsSurprise != null) {
                                buy.setSurprisePercentage(earningsSurprise);
                                if (earning.getSurprisePercentage() > 0) {
                                    buy.setPredictCorrect(true);
                                    successfulPredictions++;
                                }
                            }
                            allBuysRecommended.add(buy);
                        } else {
                            buysSkippdeDueToNullTradeDate++;
                        }

                    } /*-else if (Earning.negativeSurpriseIsPredicted((surprise.value))) {
                        totalPredictions++;
                        System.err.println("Should SELL:" + earning.getSymbol() + ", Date:" + earning.getDate() + ", Surprise Index:" + surprise);
                        System.err.println(earning.toString());
                        if (earning.getSurprisePercentage() < 0) {
                            successfulPredictions++;
                            System.err.println("CORRECT");
                        } else {
                            System.err.println("IN-CORRECT");
                        }
                      } */
                }
                taskDate = taskDate.plusDays(1);
                // repoService.saveAllBuyRecommendation(allBuysRecommended);
            }

            System.err.println("------------------------------------------");
            System.err.println("Total Skipped: " + buysSkippdeDueToNullTradeDate + " Out of " + totalPredictions);
            System.err.println("Result: " + successfulPredictions + "/" + totalPredictions + " OR "
                    + (Double.valueOf(successfulPredictions) / Double.valueOf(totalPredictions)) * 100 + "%");
            System.err.println("------------------------------------------");
        } catch (Exception e) {
            String eMessage = e.getMessage();
            System.err.println(eMessage);
            if (StringUtil.isBlank(eMessage) || YahooUtil.shouldRecordError(taskDate, eMessage)) {
                return buildErrorMeta(BUY_RECOMMENDATION, TASK_TYPE.PERSIST, QUAD.QPRPS.toString(), taskDate, eMessage);
            }
        }

        // return new TaskMetaData(taskDate, BUY_RECOMMENDATION, TASK_TYPE.PERSIST, QUAD.QPRPS.toString(),
        // STATUS.COMPLETED,
        // String.valueOf(allBuysRecommended.size()));
        return null;
    }

   
}
