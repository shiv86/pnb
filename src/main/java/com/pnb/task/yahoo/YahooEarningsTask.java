package com.pnb.task.yahoo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.Earning.ANNCMT_TIME;
import com.pnb.domain.jpa.Earning.EARNINGS_TYPE;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.repo.jpa.EarningsRepo;
import com.pnb.util.YahooUtil;

@Component
public class YahooEarningsTask extends YahooTask {

    private static final String YAHOO_EARNINGS = "YAHOO_EARNINGS";
    @Autowired
    private EarningsRepo earnRepo;
    private List<Earning> earningAnncmt;

    @Override
    protected TaskMetaData process() {
        Document earningsEPS = null;
        earningAnncmt = new ArrayList<Earning>();
        try {
            
            if (taskDate.isAfter((LocalDate.now())) || taskDate.isEqual((LocalDate.now()))) {
               return new TaskMetaData(taskDate, YAHOO_EARNINGS, TASK_TYPE.DATA_LOAD, EARNINGS_TYPE.EPS.toString(), STATUS.COMPLETED, "CANNOT_PROCESS_FUTURE_TASK_DATE");
            }
            earningsEPS = YahooUtil.getDocument(taskDate, EARNINGS_TYPE.EPS);

            Elements allElements = earningsEPS.select("tbody");
            if (allElements.size() > 4) {
                Element table = allElements.get(4);
                for (int i = 0; i < table.select("tr").size(); i++) {
                    if (i > 1) {
                        Element row = table.select("tr").get(i);
                        Elements tds = row.select("td");
                        if (tds.size() >= 3) {

                            /*-
                             System.out.print("Symbol:"+ tds.get(1).text()+", ");
                             System.out.print("Suprise:"+ tds.get(2).text()+", ");
                             System.out.print("ReportedEPS:"+ tds.get(3).text()+", ");
                             System.out.println("Consensus:"+ tds.get(4).text());
                             */

                            String company = tds.get(0).text();
                            String symbol = tds.get(1).text();
                            if (!StringUtils.isEmpty(symbol)) {
                                Earning existingEarning = earnRepo.findBySymbolAndDate(symbol, taskDate);
                                if (existingEarning != null) {
                                    populateEPSValues(taskDate, tds, existingEarning);
                                    earningAnncmt.add(existingEarning);
                                } else {
                                    Earning newEarnings = new Earning(company, symbol, ANNCMT_TIME.NOT_SUPPLIED, null, taskDate);
                                    populateEPSValues(taskDate, tds, newEarnings);
                                    earningAnncmt.add(newEarnings);
                                }
                            }

                        }
                    }
                }
                earnRepo.save(earningAnncmt);
            }
        } catch (Exception e) {
            if (YahooUtil.shouldRecordError(taskDate, e.getMessage())) {
                return buildErrorMeta(YAHOO_EARNINGS, EARNINGS_TYPE.EPS, earningsEPS,taskDate, e.getMessage());
            }
        }
        String message = new String("Total rows added: " + earningAnncmt.size());
        return new TaskMetaData(taskDate, YAHOO_EARNINGS, TASK_TYPE.DATA_LOAD, EARNINGS_TYPE.EPS.toString(), STATUS.COMPLETED, message);
    }

    private void populateEPSValues(LocalDate localDate, Elements tds, Earning earning) {

        Optional<String> suprisePercentage = Optional.of(tds.get(2).text());
        Optional<String> reportedEPS = Optional.of(tds.get(3).text());
        Optional<String> consensusEPS = Optional.of(tds.get(4).text());

        suprisePercentage.filter(x -> YahooUtil.isNumeric(x)).ifPresent(x -> earning.setSurprisePercentage(Double.valueOf(x)));
        consensusEPS.filter(x -> YahooUtil.isNumeric(x)).ifPresent(x -> earning.setConsensusEPS(Double.valueOf(x)));
        reportedEPS.filter(x -> YahooUtil.isNumeric(x)).ifPresent(x -> earning.setReportedEPS(Double.valueOf(x)));

        if (earning.getSurprisePercentage() == null) {
            earning.setManuallyCalculatedPercentage();
        }

        earning.setEPSPopulated(true);
    }

}
