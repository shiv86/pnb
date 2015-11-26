package com.pnb.task.yahoo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.repo.jpa.EarningsRepo;

/*
 * @Earning.symbol is not always the best way to best identifier for uniqueness.
 * For example Earning.symbol= 'GPRO' is currently the symbol for Go Pro. However historically
 * This symbol belonged to another company called Gen-Probe which was acquired by another company.
 * As a result select * from earning where symbol = 'GPRO' gives results for both companies.
 * The objective of this task to clean the dataset by changing all historical symbols and post fixing
 * it with "-X"
 */
@Component
public class YahooRemoveHistoricalTickFromCurrentTask extends YahooDataCleaner {

    private static final String YAHOO_HIST_TICK_CLEANER = "YAHOO_HIST_TICK_CLEANER";

    @Autowired
    private EarningsRepo earnRepo;

    @Override
    protected TaskMetaData process() {
        List<String> allSymb = earnRepo.getAllUniqueSymbol();
        List<String> unCleanSymb = new ArrayList<String>();

        for (String sym : allSymb) {
            List<Earning> allEarningsForSymbol = earnRepo.findBySymbolOrderByDateDesc(sym);
            int i = 0;
            for (Earning earning : allEarningsForSymbol) {
                
             
                // Only concerned with earnings for NASDAQ AND NYSE
                if (earning.getSymbol().contains(".")) {
                    break;
                }
                
                if(i == 0){
                    i++;
                    continue;
                }
                
                String currentCompanyName = allEarningsForSymbol.get( i - 1).getCompanyName();
                LocalDate currentEarningDate = allEarningsForSymbol.get( i - 1).getDate();
                i++;
                
                String historicalCompanyName = earning.getCompanyName();
                LocalDate historicalEarningDate = earning.getDate();

                if (!areEquivalent(currentCompanyName, historicalCompanyName)
                        && signDifferenceBetweenEarnings(currentEarningDate, historicalEarningDate)) {
                    unCleanSymb.add(earning.getSymbol());
                    break;
                }
            }
        }
        return null;
    }

    private boolean signDifferenceBetweenEarnings(LocalDate currentEarningDate, LocalDate historicalEarningsDate) {
        int YEAR_AND_A_HALF_IN_DAYS = 528;
        if((currentEarningDate.toEpochDay() - historicalEarningsDate.toEpochDay()) > YEAR_AND_A_HALF_IN_DAYS){
            return true;
        }
      
        return false;
    }

    private boolean areEquivalent(String currentCompanyName, String historicalCompanyName) {
        String normCurrentCompanyName = getNormalizedName(currentCompanyName);
        String normHistoricalCompanyName = getNormalizedName(historicalCompanyName);
        if (normCurrentCompanyName.equals(normHistoricalCompanyName)) {
            return true;
        }
        if (normCurrentCompanyName.length() >= 3 && normHistoricalCompanyName.length() >= 3) {
            if (normCurrentCompanyName.substring(0, 3).equals(normHistoricalCompanyName.substring(0, 3))) {
                return true;
            }
        }

        return checkIfPartsOfNameIsSimilar(currentCompanyName, historicalCompanyName);

    }

    private boolean checkIfPartsOfNameIsSimilar(String currentCompanyName, String historicalCompanyName) {

        String[] partsOfCurrentCompanyName = currentCompanyName.split(" ");
        String[] partsOfHistoricalCompanyName = historicalCompanyName.split(" ");

        if (checkIfStringIsSimilar(partsOfCurrentCompanyName[0], partsOfHistoricalCompanyName[0])) {
            return true;
        } else if ((partsOfCurrentCompanyName.length >= 2 && partsOfHistoricalCompanyName.length >= 2)) {
            if (checkIfStringIsSimilar(partsOfCurrentCompanyName[1], partsOfHistoricalCompanyName[1])) {
                return true;
            }
            if (historicalCompanyName.contains(partsOfCurrentCompanyName[0]) || historicalCompanyName.contains(partsOfCurrentCompanyName[1])) {
                return true;
            }
            if (currentCompanyName.contains(partsOfHistoricalCompanyName[0]) || currentCompanyName.contains(partsOfHistoricalCompanyName[1])) {
                return true;
            }

        }

        return false;
    }

    private boolean checkIfStringIsSimilar(String firstPartOfCurrentCompanyName, String firstPartOfHistoricalCompanyName) {
        if (!StringUtil.isBlank(firstPartOfCurrentCompanyName) && !StringUtil.isBlank(firstPartOfHistoricalCompanyName)) {

            firstPartOfCurrentCompanyName = firstPartOfCurrentCompanyName.toLowerCase().trim();
            firstPartOfHistoricalCompanyName = firstPartOfHistoricalCompanyName.toLowerCase().trim();

            int currentCmpNameLen = firstPartOfCurrentCompanyName.length();
            int histCmpNameLen = firstPartOfHistoricalCompanyName.length();

            String endOfFirstPartCurrent =
                    firstPartOfCurrentCompanyName.substring(currentCmpNameLen >= 4 ? currentCmpNameLen - 4 : 0, currentCmpNameLen - 1);
            String endOfLastParHist = firstPartOfHistoricalCompanyName.substring(histCmpNameLen >= 4 ? histCmpNameLen - 4 : 0, histCmpNameLen - 1);

            if (endOfFirstPartCurrent.equalsIgnoreCase(endOfLastParHist)) {
                return true;
            }
        }
        return false;
    }

    private String getNormalizedName(String companyName) {
        return companyName.replace("-", "").replace(" ", "").replace(",", "").replace(".", "").trim().toLowerCase();
    }

}
