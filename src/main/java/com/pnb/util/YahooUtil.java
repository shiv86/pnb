package com.pnb.util;

import java.time.LocalDate;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.Earning.ANNCMT_TIME;
import com.pnb.domain.jpa.PriceHistory;
import com.pnb.repo.jpa.PriceHistoryRepo;

@Service
public class YahooUtil {

    @Autowired
    private PriceHistoryRepo priceHistoryRepo;

    private final static String POST_FIX = ".html";

    public static Document getDocument(LocalDate date, Earning.EARNINGS_TYPE earningType) throws Exception {
        Thread.sleep(2000);
        String actualURLRequest = getYahooURL(earningType, date);
        Connection calConnectionObj = null;

        Response calResponse = null;
        int calRespAttempts = 0;
        int maxAttempt = 5;

        if (isWeekend(date)) {
            maxAttempt = 2;
        }

        while (calRespAttempts <= maxAttempt && calResponse == null) {
            calRespAttempts++;
            try {
                calConnectionObj = Jsoup.connect(actualURLRequest);
                calResponse = calConnectionObj.execute();
            } catch (Exception e1) {
                Thread.sleep(4000);
            }
        }

        if (calResponse != null && isValidConnection(calResponse)) {
            return calConnectionObj.get();
        } else {
            if (calResponse != null) {
                throw new Exception("Invalid HTTP Status Code:" + calResponse.statusCode() + ";" + actualURLRequest);
            } else {
                throw new Exception("The connection response was null;" + actualURLRequest);
            }

        }

    }

    public static String getYahooURL(Earning.EARNINGS_TYPE earningType, LocalDate date) {
        String localDate = date.toString().replace("-", "");
        return earningType.getUrl() + localDate + POST_FIX;
    }

    public static boolean isValidConnection(Response response) {
        boolean isValidConnection = false;
        if (response.statusCode() == 200) {
            isValidConnection = true;
        }
        return isValidConnection;
    }

    public static boolean shouldRecordError(LocalDate localDate, String eMessage) {
        if (isWeekDay(localDate) && localDate.isBefore(LocalDate.now())) {
            return true;
        }
        return false;
    }

    public static boolean isWeekend(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() > 5;
    }

    public static LocalDate nextWeekDay(LocalDate localDate) {
        localDate = localDate.plusDays(1);
        while (isWeekend(localDate)) {
            localDate = localDate.plusDays(1);
        }
        return localDate;
    }

    private static boolean isWeekDay(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() < 5;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
    }

    public LocalDate getTradeDate(Earning earning) {
        ANNCMT_TIME anncmtTime = earning.getAnncmtTime();
        switch (anncmtTime) {
            case AFTER_CLOSE:
                return earning.getDate();
            case BEFORE_OPEN:
                return getPreviousTradingDate(earning);
            case DURING_MKT_HRS:
                return getPreviousTradingDate(earning);
            default:
                return null;

        }
    }

    private LocalDate getPreviousTradingDate(Earning earning) {
        LocalDate checkDate = earning.getDate().minusDays(1);
        PriceHistory previousPrice = null;
        int daysToGoBack = 5;
        int start = 0;
        while (start <= daysToGoBack && previousPrice == null) {
            start++;
            previousPrice = priceHistoryRepo.findBySymbolAndDate(earning.getSymbol(), checkDate);
            if (previousPrice != null && previousPrice.getDate() != null) {
                return previousPrice.getDate();
            }
            checkDate = checkDate.minusDays(1);
        }
        return null;
    }
}
