package com.pnb.task.yahoo;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;
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
public class YahooAnncmtTask extends YahooTask {

    @Autowired
    private EarningsRepo earnRepo;

    private static final String TIME_NOT_SUPPLIED = "Time Not Supplied";
    private static final String AFTER_MARKET_CLOSE = "After Market Close";
    private static final String BEFORE_MARKET_OPEN = "Before Market Open";
    private static final String YAHOO_ANNCMT = "YAHOO_ANNCMT";
    private List<Earning> earningAnncmt;

    @Override
    protected TaskMetaData process() {
        Document earningDoc = null;
        earningAnncmt = new ArrayList<Earning>();
        try {
            earningDoc = YahooUtil.getDocument(taskDate, EARNINGS_TYPE.ANNCMT);

            Element table = earningDoc.select("tbody").get(5);
            /*-
             <tbody>
                 <tr>
                  <td valign="top" width="99%">
                   <table cellpadding="2" cellspacing="0" border="0" width="100%">
                    <tbody>
                     <tr bgcolor="a0b8c8">
                      <td colspan="4"> <b>Earning Announcements for Thursday, January 28</b></td>
                     </tr>
                     <tr bgcolor="dcdcdc">
                      <td><font face="arial" size="-1"><b>Company</b></font></td>
                      <td><font face="arial" size="-1"><b>Symbol</b></font></td>
                      <td align="center"><font face="arial" size="-1"><b>Time</b></font></td>
                      <td align="center"><font face="arial" size="-1"><b>Conference<br />Call</b></font></td>
                     </tr>
                     <tr>
                      <td>1-800-FLOWERS.COM</td>
                      <td><a href="http://finance.yahoo.com/q?s=flws">FLWS</a></td>
                      <td align="center"><small>Before Market Open</small></td>
                      <td align="center"><br /></td>
                     </tr>
                     <tr bgcolor="eeeeee">
                      <td>3M Company</td>
                      <td><a href="http://finance.yahoo.com/q?s=mmm">MMM</a></td>
                      <td align="center"><small>Before Market Open</small></td>
                      <td align="center"><br /></td>
                     </tr>
             */

            for (int i = 0; i < table.select("tr").size(); i++) {
                if (i > 2) {
                    Element row = table.select("tr").get(i);
                    Elements tds = row.select("td");
                    if (tds.size() >= 3) {
                        /*-
                         System.out.print("Company:"+ tds.get(0).text()+", ");
                         System.out.print("Symbol:"+ tds.get(1).text()+", ");
                         System.out.println("Time:"+ tds.get(2).text());
                         */

                        int companyInt = 0;
                        int symbolInt = 1;
                        int anncmtTimeInt = 2;
                        if (tds.size() == 6) {
                            anncmtTimeInt = 3;
                        }

                        String rawAnncmtTime = tds.get(anncmtTimeInt).text().trim();
                        ANNCMT_TIME anncmtTime = null;

                        switch (rawAnncmtTime) {
                            case BEFORE_MARKET_OPEN:
                                anncmtTime = ANNCMT_TIME.BEFORE_OPEN;
                                break;
                            case AFTER_MARKET_CLOSE:
                                anncmtTime = ANNCMT_TIME.AFTER_CLOSE;
                                break;
                            case TIME_NOT_SUPPLIED:
                                anncmtTime = ANNCMT_TIME.NOT_SUPPLIED;
                                break;
                            default:
                                try {
                                    String[] anncArray = rawAnncmtTime.split(" ");
                                    String[] time = anncArray[0].split(":");
                                    int hour = Integer.valueOf(time[0]);
                                    int min = Integer.valueOf(time[1]);
                                    String ampm = anncArray[1];
                                    if (((hour < 9) || (hour == 9 && min < 30)) && "AM".equalsIgnoreCase(ampm)) {
                                        anncmtTime = ANNCMT_TIME.BEFORE_OPEN;
                                    } else if (hour >= 4 && "PM".equalsIgnoreCase(ampm)) {
                                        anncmtTime = ANNCMT_TIME.AFTER_CLOSE;
                                    } else {
                                        anncmtTime = ANNCMT_TIME.DURING_MKT_HRS;
                                    }
                                } catch (Exception e) {
                                    anncmtTime = ANNCMT_TIME.NOT_SUPPLIED;
                                }
                        }

                        Earning earning = new Earning(tds.get(companyInt).text(), tds.get(symbolInt).text(), anncmtTime, rawAnncmtTime, taskDate);
                        earning.setEarningsAnnoucementPopulated(true);

                        if (!StringUtils.isEmpty(earning.getSymbol())) {
                            earningAnncmt.add(earning);
                        }
                    }
                }
            }
            earningAnncmt = getEarningsToSave(earningAnncmt);
            earnRepo.save(earningAnncmt);
        } catch (Exception e) {
            String eMessage = e.getMessage();
            if (StringUtil.isBlank(eMessage) || YahooUtil.shouldRecordError(taskDate, eMessage)) {
                return buildErrorMeta(YAHOO_ANNCMT, EARNINGS_TYPE.ANNCMT, earningDoc,taskDate, eMessage);
            }
        }

        String message = new String("Total rows added: " + earningAnncmt.size());
        return new TaskMetaData(taskDate, YAHOO_ANNCMT, TASK_TYPE.DATA_LOAD, EARNINGS_TYPE.ANNCMT.toString(), STATUS.COMPLETED, message);
    }

    private List<Earning> getEarningsToSave(List<Earning> parsedEarnings) {
        List<Earning> earningsToSave = new ArrayList<Earning>();
        for (Earning parsedEarning : parsedEarnings) {
            Earning existingEarning = earnRepo.findBySymbolAndDate(parsedEarning.getSymbol(), parsedEarning.getDate());
            if (existingEarning != null) {
                if (updateAnncmt(parsedEarning, existingEarning)) {
                    existingEarning.setAnnoucementTime(parsedEarning.getAnncmtTime());
                    existingEarning.setEarningsAnnoucementPopulated(true);
                    earningsToSave.add(existingEarning);
                }
            } else {
                earningsToSave.add(parsedEarning);
            }
        }
        return earningsToSave;
    }

    private boolean updateAnncmt(Earning parsedEarning, Earning existingEarning) {
        return !ANNCMT_TIME.NOT_SUPPLIED.equals(parsedEarning.getAnncmtTime()) && ANNCMT_TIME.NOT_SUPPLIED.equals(existingEarning.getAnncmtTime());
    }
}
