package com.pnb.algo.earnings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.EarnReturn;
import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.PriceHistory;
import com.pnb.domain.jpa.RETURN_PERIOD;
import com.pnb.domain.jpa.RETURN_TYPE;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.repo.jpa.EarnReturnRepo;
import com.pnb.repo.jpa.EarningsRepo;
import com.pnb.repo.jpa.PriceHistoryRepo;
import com.pnb.repo.jpa.RepoService;
import com.pnb.task.Task;
import com.pnb.util.YahooUtil;

@Service
public class EarnReturnsTask extends Task {

    @Autowired
    private EarningsRepo earningRepo;

    @Autowired
    private PriceHistoryRepo priceRepo;

    @Autowired
    private EarnReturnRepo returnRepo;

    @Autowired
    private RepoService repoService;
    
    @Autowired
    private YahooUtil yahooUtil;

    int countOO = 0;
    int countOC = 0;
    int countCO = 0;
    int countCC = 0;
    int countTotal = 0;

    @Override
    protected TaskMetaData process() {

        countOO = 0;
        countOC = 0;
        countCO = 0;
        countCC = 0;
        countTotal = 0;

        int totalSkippeDueToNull = 0;
        List<String> allUSSymbols = earningRepo.getAllUSSymbol();
        int size = allUSSymbols.size();
        int processed = 0;
        for (String currentSymb : allUSSymbols) {
            processed++;
            List<Earning> allEarning = earningRepo.findBySymbolOrderByDateDesc(currentSymb);
            List<EarnReturn> allReturnsCalculated = new ArrayList<EarnReturn>();

            for (Earning earn : allEarning) {
                LocalDate tradeD = yahooUtil.getTradeDate(earn);
                String symb = earn.getSymbol();
                EarnReturn returnAmt = getReturn(symb, tradeD);
                if (returnAmt != null) {
                    allReturnsCalculated.add(returnAmt);
                } else {
                    totalSkippeDueToNull++;
                }

            }
            repoService.saveAllEarnReturn(allReturnsCalculated);
        }
        
        System.out.println(totalSkippeDueToNull + "/" + size + " Were skipped");
        System.out.println("Final Count|");
        System.out.println("countOO:" + countOO);
        System.out.println("countOC:" + countOC);
        System.out.println("countCO:" + countCO);
        System.out.println("countCC:" + countCC);
        System.out.println("countTotal:" + countTotal);

        return null;
    }

    private EarnReturn getReturn(String symb, LocalDate tradeDate) {

        if (tradeDate != null && returnRepo.findBySymbolAndTradeDate(symb, tradeDate) == null) {

            LocalDate startTradeDate = tradeDate;
            PriceHistory tradePrice = priceRepo.findBySymbolAndDate(symb, startTradeDate);
            PriceHistory nextDay = null;

            int attempt = 0;
            while (attempt < 5 && nextDay == null) {
                attempt++;
                tradeDate = tradeDate.plusDays(1);
                nextDay = priceRepo.findBySymbolAndDate(symb, tradeDate);
            }

            if (priceIsNull(tradePrice)) {
                System.err.println(symb + ":" + tradeDate + "|" + "trade prices are null");
                return null;
            }

            if (priceIsNull(nextDay)) {
                System.err.println(symb + ":" + tradeDate + "|" + " next date prices are null");
                return null;
            }

            Double openOpen = rtn(tradePrice, nextDay, RETURN_PERIOD.OPEN_OPEN);
            Double openClose = rtn(tradePrice, nextDay, RETURN_PERIOD.OPEN_CLOSE);
            Double closeOpen = rtn(tradePrice, nextDay, RETURN_PERIOD.CLOSE_OPEN);
            Double closeClose = rtn(tradePrice, nextDay, RETURN_PERIOD.CLOSE_CLOSE);

            if (openOpen > openClose && openOpen > closeOpen && openOpen > closeClose) {
                countOO++;
            }
            if (openClose > openOpen && openClose > closeOpen && openClose > closeClose) {
                countOC++;
            }
            if (closeOpen > openClose && closeOpen > openOpen && closeOpen > closeClose) {
                countCO++;
            }
            if (closeClose > openClose && closeClose > closeOpen && closeClose > openOpen) {
                countCC++;
            }
            countTotal++;

            return new EarnReturn(symb, startTradeDate, RETURN_TYPE.PERCENTAGE, openOpen, openClose, closeOpen, closeClose);

        }

        return null;

    }

    private Double rtn(PriceHistory tradePrice, PriceHistory sellPrice, RETURN_PERIOD period) {

        switch (period) {
            case OPEN_OPEN:
                return getReturnAmt(tradePrice.getOpen(), sellPrice.getOpen());
            case OPEN_CLOSE:
                return getReturnAmt(tradePrice.getOpen(), sellPrice.getClose());
            case CLOSE_OPEN:
                return getReturnAmt(tradePrice.getClose(), sellPrice.getOpen());
            case CLOSE_CLOSE:
                return getReturnAmt(tradePrice.getClose(), sellPrice.getClose());
            default:
                return null;
        }

    }

    private boolean priceIsNull(PriceHistory priceHist) {
        if (priceHist != null && priceHist.getOpen() != null && priceHist.getClose() != null) {
            return false;
        }
        return true;
    }

    private Double getReturnAmt(Double startPrice, Double endPrice) {
        return ((endPrice - startPrice) / startPrice) * 100;
    }
}
