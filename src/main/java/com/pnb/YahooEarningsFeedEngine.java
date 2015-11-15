package com.pnb;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.pnb.domain.jpa.Earnings;
import com.pnb.domain.jpa.Earnings.ANNCMT_TIME;
import com.pnb.domain.jpa.EarningsMetaData;
import com.pnb.domain.jpa.EarningsMetaData.STATUS;
import com.pnb.repo.jpa.EarningsMetaDataRepo;
import com.pnb.repo.jpa.EarningsRepo;

@Component
public class YahooEarningsFeedEngine {

    @Autowired
    EarningsRepo earningsRepo;

    @Autowired
    EarningsMetaDataRepo earnMetaRepo;

    private static Map<String, Earnings> earningsFirst = null;

    private final static String EARNINGS_ANNCMT_URL = "http://biz.yahoo.com/research/earncal/";
    private final static String EARNINGS_SURPRISE_URL = "http://biz.yahoo.com/z/";
    private final static String POST_FIX = ".html";
    private final static LocalDate DEFAULT_START_DATE = LocalDate.now().minusDays(30);
    private final static LocalDate DEFAULT_END_DATE = LocalDate.now().plusDays(10);
    private LocalDate startDate = LocalDate.of(2015, 01, 01);// DEFAULT_START_DATE;//
    private LocalDate endDate = startDate.now();// DEFAULT_END_DATE; //

    public enum EARNINGS_TYPE {
        ANNCMT(EARNINGS_ANNCMT_URL), SURPRISE(EARNINGS_SURPRISE_URL), BOTH("");
        private String url;

        EARNINGS_TYPE(String url) {
            this.url = url;
        }

        String getUrl() {
            return url;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        YahooEarningsFeedEngine yahooEarningEngine = new YahooEarningsFeedEngine();
        yahooEarningEngine.start();
    }


    public void test() {
        Earnings earnings = earningsRepo.findBySymbolAndDate("ABR", LocalDate.of(2015, 11, 6));
        System.out.println("Earnings is:" + earnings.toString());
    }

    @PostConstruct
    public void start() {
        try {
            while (!startDate.equals(endDate)) {
                Thread.sleep(4000);
                buildEarningForDate(startDate);
                this.startDate = startDate.plusDays(1);
            }

            System.out.println("Finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    private void buildEarningForDate(LocalDate tDate) throws Exception {
        earningsFirst = new HashMap<String, Earnings>();
        EARNINGS_TYPE earningType = null;
        
        try {
            
            buildInitialEarningsAccmt(tDate);
            earningType = EARNINGS_TYPE.ANNCMT;
            if (tDate.isBefore(LocalDate.now())) {
                earningType = EARNINGS_TYPE.BOTH;
                populateEarningsEps(tDate);
            }
        } catch (IOException e) {
            System.err.println("Error occurred with the following request:");
            System.err.println(e);
        }

       System.out.println("Start Date BEGIN:" + tDate.toString());
       earningsFirst.values().forEach(e -> {
           System.out.println(e);
           Earnings existingEarning = earningsRepo.findBySymbolAndDate(e.getSymbol(), e.getDate());
           if(existingEarning != null){
               if(!existingEarning.isAnncmtPopulated() && !ANNCMT_TIME.NOT_SUPPLIED.equals(e.getAnnoucementTime())){
                   existingEarning.setEarningsAnnoucementPopulated(true);
                   existingEarning.setAnnoucementTime(e.getAnnoucementTime());
                   existingEarning.setRawAnnoucementTime(e.getRawAnnoucementTime());
               } 
               if(!existingEarning.isEPSPopulated() && e.isEPSPopulated()){
                   existingEarning.setEPSPopulated(true);
                   existingEarning.setConsensusEPS(e.getConsensusEPS());
                   existingEarning.setReportedEPS(e.getReportedEPS());
                   existingEarning.setSurprisePercentage(e.getSurprisePercentage());
               }
               earningsRepo.saveAndFlush(existingEarning);
           } else {
               earningsRepo.saveAndFlush(e);
           }
           
       });

       EarningsMetaData metaData = new EarningsMetaData(tDate,earningType,STATUS.COMPLETED_DATA_LOAD,"");
       earnMetaRepo.saveAndFlush(metaData);

        System.out.println("Start Date END:" + tDate.toString());
        System.out.println("----<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<----");
    }

    private void buildInitialEarningsAccmt(LocalDate localDate) throws Exception {
        Document earningDoc;
        try {
            earningDoc = getDocument(localDate, EARNINGS_TYPE.ANNCMT);

            Element table = earningDoc.select("tbody").get(5);
            /*-
             <tbody>
                 <tr>
                  <td valign="top" width="99%">
                   <table cellpadding="2" cellspacing="0" border="0" width="100%">
                    <tbody>
                     <tr bgcolor="a0b8c8">
                      <td colspan="4"> <b>Earnings Announcements for Thursday, January 28</b></td>
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
                            case "Before Market Open":
                                anncmtTime = ANNCMT_TIME.BEFORE_OPEN;
                                break;
                            case "After Market Close ":
                                anncmtTime = ANNCMT_TIME.AFTER_CLOSE;
                                break;
                            case "Time Not Supplied":
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
                                    } else if (hour > 4 && "PM".equalsIgnoreCase(ampm)) {
                                        anncmtTime = ANNCMT_TIME.AFTER_CLOSE;
                                    } else {
                                        anncmtTime = ANNCMT_TIME.DURING_MKT_HRS;
                                    }
                                } catch (Exception e) {
                                    anncmtTime = ANNCMT_TIME.NOT_SUPPLIED;
                                }
                        }

                        Earnings earning =
                                new Earnings(tds.get(companyInt).text(), tds.get(symbolInt).text(), anncmtTime, rawAnncmtTime, localDate);
                        earning.setEarningsAnnoucementPopulated(true);
                        if(!StringUtils.isEmpty(earning.getSymbol())){
                            earningsFirst.put(earning.getSymbol(), earning);
                        }
                    }
                }
            }
        } catch (Exception e) {
            String eMessage = e.getMessage();
            if (shouldRecordError(localDate, eMessage)) {
                EarningsMetaData error = new EarningsMetaData(localDate, EARNINGS_TYPE.ANNCMT,STATUS.ERROR, e.getMessage());
                earnMetaRepo.saveAndFlush(error);
            }
            System.err.println(e);
            System.err.println(">>>>>>");
        }
    }

    private void populateEarningsEps(LocalDate localDate) throws Exception {
        Document earningsEPS;
        try {
            earningsEPS = getDocument(localDate, EARNINGS_TYPE.SURPRISE);

            Element table = earningsEPS.select("tbody").get(4);
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
                        Earnings earning = earningsFirst.get(symbol);
                        if (earning != null) {
                            populateEPSValues(localDate, tds, earning);
                        } else {
                            Earnings newEarnings = new Earnings(company, symbol, ANNCMT_TIME.NOT_SUPPLIED, null, localDate);
                            populateEPSValues(localDate, tds, newEarnings);
                            if(!StringUtils.isEmpty(newEarnings.getSymbol())){
                                earningsFirst.put(symbol, newEarnings);
                            }
                            System.err.println("Symbol: " + tds.get(1).text() + " Require Announcement Time" + "For Earnings date "
                                    + localDate.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (shouldRecordError(localDate, e.getMessage())) {
                EarningsMetaData error = new EarningsMetaData(localDate, EARNINGS_TYPE.SURPRISE,STATUS.ERROR, e.getMessage());
                earnMetaRepo.saveAndFlush(error);
            }
            System.err.println(e);
            System.err.println("<<<<<<<<<<<<<<<");
        }
    }

    private void populateEPSValues(LocalDate localDate, Elements tds, Earnings earning) {
        Optional<String> suprisePercentage = Optional.of(tds.get(2).text());
        Optional<String> reportedEPS = Optional.of(tds.get(3).text());
        Optional<String> consensusEPS = Optional.of(tds.get(4).text());

        suprisePercentage.filter(x -> YahooEarningsFeedEngine.isNumeric(x)).ifPresent(x -> earning.setSurprisePercentage(Double.valueOf(x)));
        consensusEPS.filter(x -> YahooEarningsFeedEngine.isNumeric(x)).ifPresent(x -> earning.setConsensusEPS(Double.valueOf(x)));
        reportedEPS.filter(x -> YahooEarningsFeedEngine.isNumeric(x)).ifPresent(x -> earning.setReportedEPS(Double.valueOf(x)));

        if (earning.getSurprisePercentage() == null && (earning.getReportedEPS() != null && earning.getConsensusEPS() != null)) {
            double difference = earning.getReportedEPS() - earning.getConsensusEPS();
            double suprisePct = (difference / earning.getConsensusEPS()) * 100;
            if(earning.getConsensusEPS() == 0){
                suprisePct = difference * 100;
            }
            earning.setSurprisePercentage(suprisePct);
            EarningsMetaData surpError = new EarningsMetaData(localDate, EARNINGS_TYPE.SURPRISE,STATUS.ERROR, "Manually calculated Surprise % " + earning.getSymbol());
            earnMetaRepo.saveAndFlush(surpError);
        } 
        
        earning.setEPSPopulated(true);
    }

    public boolean isValidConnection(Response response) {
        boolean isValidConnection = false;
        if (response.statusCode() == 200) {
            isValidConnection = true;
        }
        return isValidConnection;
    }

    public Document getDocument(LocalDate date, EARNINGS_TYPE earningType) throws Exception {
        String localDate = date.toString().replace("-", "");
        Thread.sleep(2000);
        String actualURLRequest = earningType.getUrl() + localDate + POST_FIX;
        Connection calConnectionObj = null;
        try {
            calConnectionObj = Jsoup.connect(actualURLRequest);
        } catch (Exception e1) {
            Thread.sleep(4000);
            try {
                calConnectionObj = Jsoup.connect(actualURLRequest);
            } catch (Exception e2) {
                Thread.sleep(4000);
                calConnectionObj = Jsoup.connect(actualURLRequest);
            }
        }

        Response calResponse = null;

        try {
            calResponse = calConnectionObj.execute();
        } catch (Exception e1) {
            Thread.sleep(4000);
            try {
                calResponse = calConnectionObj.execute();
            } catch (Exception e2) {
                Thread.sleep(4000);
                calResponse = calConnectionObj.execute();
            }
        }

        if (isValidConnection(calResponse)) {
            return calConnectionObj.get();
        } else {
            throw new Exception("Invalid Connection:" + calResponse.statusCode() + ":" + earningType);
        }

    }

    private boolean shouldRecordError(LocalDate localDate, String eMessage) {
        return isWeekDay(localDate) || (!(eMessage.contains("404 error") && isWeekend(localDate)));
    }

    private boolean isWeekend(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() > 5;
    }

    private boolean isWeekDay(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() < 5;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
    }

}
