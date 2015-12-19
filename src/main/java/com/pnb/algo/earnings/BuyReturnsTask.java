package com.pnb.algo.earnings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.BuyRecommend;
import com.pnb.domain.jpa.BuyReturn;
import com.pnb.domain.jpa.PriceHistory;
import com.pnb.domain.jpa.RETURN_PERIOD;
import com.pnb.domain.jpa.RETURN_TYPE;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.repo.jpa.BuyRecommendRepo;
import com.pnb.repo.jpa.BuyReturnRepo;
import com.pnb.repo.jpa.PriceHistoryRepo;
import com.pnb.task.Task;

@Service
public class BuyReturnsTask extends Task {

    @Autowired
    private BuyRecommendRepo buyRecommendRepo;

    @Autowired
    private PriceHistoryRepo priceRepo;

    @Autowired
    private BuyReturnRepo buyReturnRepo;

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
        List<BuyRecommend> allBuysRecommended = buyRecommendRepo.findAllOrderByTradeDateAsc();
        List<BuyReturn> allReturnsCalculated = new ArrayList<BuyReturn>();

        for (BuyRecommend buy : allBuysRecommended) {
            LocalDate tradeD = buy.getTradeDate();
            String symb = buy.getSymbol();
            BuyReturn buyReturn = getBuyReturn(symb, tradeD);
            if (buyReturn != null) {
                allReturnsCalculated.add(buyReturn);
            } else {
                totalSkippeDueToNull++;
            }

        }
        System.out.println(totalSkippeDueToNull + "/" + allBuysRecommended.size() + " Were skipped");
        System.out.println("Final Count|");
        System.out.println("countOO:" + countOO);
        System.out.println("countOC:" + countOC);
        System.out.println("countCO:" + countCO);
        System.out.println("countCC:" + countCC);
        System.out.println("countTotal:" + countTotal);

        buyReturnRepo.save(allReturnsCalculated);
        return null;
    }

    private BuyReturn getBuyReturn(String symb, LocalDate tradeDate) {

        if (buyReturnRepo.findBySymbolAndTradeDate(symb, tradeDate) == null) {

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

            return new BuyReturn(symb, startTradeDate, RETURN_TYPE.PERCENTAGE, openOpen, openClose, closeOpen, closeClose);

        }

        return null;

    }

    private Double rtn(PriceHistory tradePrice, PriceHistory sellPrice, RETURN_PERIOD period) {

        switch (period) {
            case OPEN_OPEN:
                return getReturn(tradePrice.getOpen(), sellPrice.getOpen());
            case OPEN_CLOSE:
                return getReturn(tradePrice.getOpen(), sellPrice.getClose());
            case CLOSE_OPEN:
                return getReturn(tradePrice.getClose(), sellPrice.getOpen());
            case CLOSE_CLOSE:
                return getReturn(tradePrice.getClose(), sellPrice.getClose());
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

    private Double getReturn(Double startPrice, Double endPrice) {
        return ((endPrice - startPrice) / startPrice) * 100;
    }
}
