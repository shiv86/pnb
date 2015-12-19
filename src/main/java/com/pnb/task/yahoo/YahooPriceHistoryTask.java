package com.pnb.task.yahoo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVReader;

import com.pnb.domain.jpa.PriceHistory;
import com.pnb.domain.jpa.PriceHistory.FREQUENCY;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.repo.jpa.EarningsRepo;
import com.pnb.repo.jpa.RepoService;

@Service
public class YahooPriceHistoryTask extends YahooTask {

    @Autowired
    private EarningsRepo earnRepo;

    @Autowired
    private RepoService priceRepo;

    @Override
    protected TaskMetaData process() {
        List<String> allUSSym = Arrays.asList("^VIX");//earnRepo.getAllUSSymbol();
        for (String symbol : allUSSym) {

            List<PriceHistory> allPricesForSymbol = new ArrayList<PriceHistory>();
            CSVReader reader = null;
            try {
                Thread.sleep(1000L);
                URL stockURL = new URL(getURL(symbol));
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
        return new TaskMetaData(taskEndDate, "PRICE_HISTORY", TASK_TYPE.DATA_LOAD, "PARENT_DATA_LOAD", STATUS.COMPLETED, null);
    }

    private static String getURL(String symbol) {
        return "http://real-chart.finance.yahoo.com/table.csv?s=" + symbol + "&a=00&b=2&c=1962&d=10&e=28&f=2015&g=d&ignore=.csv";
    }

}
