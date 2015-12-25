package com.pnb.task.yahoo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVReader;

import com.pnb.domain.jpa.PriceHistory;
import com.pnb.domain.jpa.PriceHistory.FREQUENCY;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.repo.jpa.PriceHistoryRepo;
import com.pnb.repo.jpa.RepoService;

@Service
public class YahooPriceHistoryTask extends YahooTask {

    @Autowired
    private PriceHistoryRepo priceHisRepo;

    @Autowired
    private RepoService priceRepo;

    private int aLastTaskIndexMonth = 0;
    private int bLastTaskday = 2;
    private int cLastTaskYear = 1962;

    @Override
    protected TaskMetaData process() {
        LocalDate today = LocalDate.now();
        List<String> allSymWhichRequirePriceUpdates = priceHisRepo.getAllSymbolsForPriceHistoryUpdate();
        int totalSymbolSaved = 0;
        for (String symbol : allSymWhichRequirePriceUpdates) {
            LocalDate lastTaskRunDate = priceHisRepo.getLastPriceHistoryTaskRunDate(symbol);
            List<PriceHistory> allPricesForSymbol = new ArrayList<PriceHistory>();
            if(!grtThanFiveDays(today, lastTaskRunDate)){
                continue;
            }
            CSVReader reader = null;
            try {
                Thread.sleep(1000L);
                URL stockURL = new URL(getURL(symbol, lastTaskRunDate));
                BufferedReader in = new BufferedReader(new InputStreamReader(stockURL.openStream()));
                reader = new CSVReader(in);
                String[] row = null;
                while ((row = reader.readNext()) != null) {
                    if (row[0].equalsIgnoreCase(("Date"))) {
                        continue;
                    }
                    LocalDate priceDate = LocalDate.parse(row[0]);
                    Double open = Double.valueOf(row[1]);
                    Double high = Double.valueOf(row[2]);
                    Double low = Double.valueOf(row[3]);
                    Double close = Double.valueOf(row[4]);
                    Long volume = Long.valueOf(row[5]);
                    Double adjClose = Double.valueOf(row[6]);
                    PriceHistory priceHistory = new PriceHistory(symbol, priceDate, open, high, low, close, volume, adjClose, FREQUENCY.DAILY);
                    allPricesForSymbol.add(priceHistory);
                }

                priceRepo.saveAllPricesForSymbols(allPricesForSymbol);
                totalSymbolSaved++;
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                StringBuffer sb = new StringBuffer();
                if (e.getMessage().length() >= 200) {
                    sb.append(e.getMessage().substring(0, 200));
                } else {
                    sb.append(e.getMessage());
                }
                priceRepo.saveError(symbol, sb, this.taskEndDate);
            } finally {

            }
        }
        return new TaskMetaData(taskEndDate, "PRICE_HISTORY", TASK_TYPE.DATA_LOAD, "PARENT_DATA_LOAD", STATUS.COMPLETED, totalSymbolSaved
                + " out of " + allSymWhichRequirePriceUpdates.size() + " have been saved.");
    }

    private boolean grtThanFiveDays(LocalDate today, LocalDate lastTaskRunDate) {
        if(lastTaskRunDate != null){
            if(lastTaskRunDate.isAfter(today.minusDays(5))){
              return false;
            }
        }
        return true;
    }

    private String getURL(String symbol, LocalDate lastTaskRunDate) {
        if (lastTaskRunDate != null) {
            aLastTaskIndexMonth = lastTaskRunDate.getMonthValue() - 1;
            bLastTaskday = lastTaskRunDate.getDayOfMonth() + 1;
            cLastTaskYear = lastTaskRunDate.getYear();
        } else {
            aLastTaskIndexMonth = 0;
            bLastTaskday = 2;
            cLastTaskYear = 1962;
        }

        LocalDate today = LocalDate.now();
        int dCurrentIndexMonth = today.getMonthValue() - 1;
        int eCurrentDay = today.getDayOfMonth();
        int fCurrentYear = today.getYear();

        return "http://real-chart.finance.yahoo.com/table.csv?s=" + symbol + "&a=" + aLastTaskIndexMonth + "&b=" + bLastTaskday + "&c="
                + cLastTaskYear + "&d=" + dCurrentIndexMonth + "&e=" + eCurrentDay + "&f=" + fCurrentYear + "&g=d&ignore=.csv";
    }

}
