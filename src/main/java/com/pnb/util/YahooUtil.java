package com.pnb.util;

import java.time.LocalDate;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.pnb.domain.jpa.Earning;

public class YahooUtil {

    private final static String POST_FIX = ".html";

    public static Document getDocument(LocalDate date, Earning.EARNINGS_TYPE earningType) throws Exception {
        Thread.sleep(2000);
        String actualURLRequest = getYahooURL(earningType, date);
        Connection calConnectionObj = null;

        Response calResponse = null;
        int calRespAttempts = 0;
        int maxAttempt = 5;
        
        if(isWeekend(date)){
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
        if (isWeekDay(localDate)) {
            return true;
        }
        return false;
    }

    private static boolean isWeekend(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() > 5;
    }

    private static boolean isWeekDay(LocalDate localDate) {
        return localDate.getDayOfWeek().getValue() < 5;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
    }
}
