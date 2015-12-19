package com.pnb.algo.earnings;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVWriter;

import com.pnb.domain.jpa.BuyRecommend;
import com.pnb.domain.jpa.BuyReturn;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.repo.jpa.BuyRecommendRepo;
import com.pnb.repo.jpa.BuyReturnRepo;
import com.pnb.task.Task;
import com.pnb.util.YahooUtil;

@Service
public class CSVBuyRecommendationFileBuilderTask extends Task {

    @Autowired
    private BuyRecommendRepo buyRepo;

    @Autowired
    private BuyReturnRepo buyReturnRepo;
    
    @Autowired 
    private YahooUtil yahooUtil;
    
    private MathContext mc = new MathContext(2, RoundingMode.HALF_UP);
    private DescriptiveStatistics totalStats;
    private DescriptiveStatistics lossStats;
    private DescriptiveStatistics winStats;
    private int totalWinner;
    private Double totalWinReturn;
    private int totalLosers;
    private Double totalLossReturn;
    private int totalTrades;
    
    


    @Override
    protected TaskMetaData process() {
        totalWinner = 0;
        totalLosers = 0;
        totalWinReturn = 0.0;
        totalLossReturn = 0.0;
        totalStats = new DescriptiveStatistics();
        winStats = new DescriptiveStatistics();
        lossStats = new DescriptiveStatistics();
        
        String[] header = new String[] { "symbol", "trade_date", "buy", "nextday" };

        String csv = "data.csv";
        CSVWriter writer = null;
        int rowIndex = 0;
        try {
            writer = new CSVWriter(new FileWriter(csv));
            if (rowIndex == 0) {
                writer.writeNext(header);
                rowIndex++;
            }

            //BuyRecommend testBuyRec =  buyRepo.findBySymbolAndTradeDate("PAY", LocalDate.of(2014, 12, 15));
            List<BuyRecommend> allBuyRecommended = buyRepo.findAllGreaterThanOrEqualToStartAndEndDate(taskDate.toString(), taskEndDate.toString()); // Arrays.asList(testBuyRec);

            for (BuyRecommend buyRecommend : allBuyRecommended) {
                Boolean buyOrSell = null;
                buyOrSell = shouldBuy(buyRecommend);
                if (buyOrSell != null) {
                    totalTrades++;
                    String tradeDate = buyRecommend.getTradeDate().toString();
                    String nextDate = YahooUtil.nextWeekDay(buyRecommend.getTradeDate()).toString();
                    
                    String[] buyRowValue = getBuyRow(buyRecommend, buyOrSell,tradeDate,false);
                    String[] sellRowValue = getBuyRow(buyRecommend, buyOrSell == true ? false : true,nextDate,true);
                    
                    writer.writeNext(buyRowValue);
                    writer.writeNext(sellRowValue);
                }

            }
            writer.close();
            System.err.println("Total Return: " + totalStats.getSum());
            System.err.println("Total Winner: " + totalWinner + " Out of " + totalTrades);
            System.err.println("Total Losers: " + totalLosers + " Out of " + totalTrades);
            System.err.println("Total Winner Percentage: " + winStats.getSum());
            System.err.println("Total Loss Percentage: " + lossStats.getSum());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private Boolean shouldBuy(BuyRecommend buyRecommend) {
        List<BuyReturn> allBuyReturnsBeforeEarningDates =
                buyReturnRepo.findBuyReturnBeforeDate(buyRecommend.getSymbol(), buyRecommend.getTradeDate().toString());
        int positive = 0;
        int negative = 0;
        BigDecimal totalReturn = BigDecimal.valueOf(0L);

        if (allBuyReturnsBeforeEarningDates.size() >= 3) {
            DescriptiveStatistics stats = new DescriptiveStatistics();

            for (BuyReturn buyReturn : allBuyReturnsBeforeEarningDates) {
                totalReturn.add(BigDecimal.valueOf(buyReturn.getCloseClose()));
                stats.addValue(buyReturn.getCloseOpen());
                if (Math.signum(buyReturn.getCloseOpen()) == 1) {
                    positive++;
                } else {
                    negative++;
                }
            }
            
            BigDecimal allBuyRtn = BigDecimal.valueOf(allBuyReturnsBeforeEarningDates.size());
            BigDecimal percentageItWasPostive = BigDecimal.valueOf(positive).divide(allBuyRtn,mc);
            BigDecimal percentageItWasNegative = BigDecimal.valueOf(negative).divide(allBuyRtn,mc);
            BigDecimal THRESHOLD = BigDecimal.valueOf(0.75);

            if (percentageItWasPostive.compareTo(THRESHOLD) == 1 && Math.signum(stats.getMean()) == 1 && Math.signum(stats.getPercentile(50)) == 1) {
                System.err.println("Buying " + buyRecommend.getSymbol() + "| Percentage Positive:" + percentageItWasPostive.toString()
                        + " | Mean:" + stats.getMean() + " | Median:" + stats.getPercentile(50));
                addToTotalStats(buyRecommend,true);
                return true;
            }

            if (percentageItWasNegative.compareTo(THRESHOLD) == 1 && Math.signum(stats.getMean()) == -1
                    && Math.signum(stats.getPercentile(50)) == -1) {
                System.err.println("Selling " + buyRecommend.getSymbol() + "| Percentage Negative:" + percentageItWasNegative.toString()
                        + " | Mean:" + stats.getMean() + " | Median:" + stats.getPercentile(50));
                addToTotalStats(buyRecommend,false);
                return false;
            }
        }
        return null;
    }

    private void addToTotalStats(BuyRecommend buyRecommend,boolean buy) {
        BuyReturn actReturn = buyReturnRepo.findBySymbolAndTradeDate(buyRecommend.getSymbol(), buyRecommend.getTradeDate());
        if(buy){
            if(Math.signum(actReturn.getCloseOpen()) == 1){
                totalWinner++;
                winStats.addValue(actReturn.getCloseOpen());
            } else {
                lossStats.addValue(actReturn.getCloseOpen());
                totalLosers++;
            }
                totalStats.addValue((actReturn.getCloseOpen()));
        } else {
            if(Math.signum(actReturn.getCloseOpen()) == -1){
                totalWinner++;
                totalStats.addValue(Math.abs((actReturn.getCloseOpen())));
                winStats.addValue(Math.abs(actReturn.getCloseOpen()));
            } else {
                totalLosers++;
                lossStats.addValue(actReturn.getCloseOpen() * -1);
                totalStats.addValue((actReturn.getCloseOpen()) * -1 );
            }
        }
    }

    private String[] getBuyRow(BuyRecommend buy, boolean isBuy,String tradeDate, boolean nextDay) {
        String isBuyString = "n";
        if (isBuy) {
            isBuyString = "y";
        }
        
        String nextDayString = "n";
        if(nextDay){
            nextDayString = "y";
        }
        String[] buyRowValue = new String[] { buy.getSymbol(), tradeDate, isBuyString, nextDayString };
        return buyRowValue;
    }

}
