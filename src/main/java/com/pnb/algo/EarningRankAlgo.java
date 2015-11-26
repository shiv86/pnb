package com.pnb.algo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.Earning;

@Service
public class EarningRankAlgo {

    public EarningRank getRank(String symbol, LocalDate date,List<Earning> allHisEarningsForSymbol) {

        EarningRank rank = new EarningRank();

        rank.symbol = symbol;

        for (int i = 0; i < allHisEarningsForSymbol.size(); i++) {
            if (i == 0) {
                continue;
            }
            Earning perviousEarning = allHisEarningsForSymbol.get(i - 1);
            Earning currentEarning = allHisEarningsForSymbol.get(i);

            if (!sigDifferenceInDate(currentEarning, perviousEarning)) {
                BigDecimal pervCon = BigDecimal.valueOf(perviousEarning.getConsensusEPS());
                BigDecimal currCon = BigDecimal.valueOf(currentEarning.getConsensusEPS());
                int compCurrWithPerv = currCon.subtract(pervCon).compareTo(BigDecimal.ZERO);

                if (compCurrWithPerv == 1) {
                    // positive revision
                    rank.totalEarningCount++;
                    rank.totalPostiveRevision++;

                    BigDecimal repEPS = BigDecimal.valueOf(currentEarning.getReportedEPS());
                    int compWithRep = repEPS.compareTo(currCon);

                    // positive revision leads to +ive est beat
                    if (compWithRep == 1) {
                        rank.totalPostiveScore++;
                    }

                } else if (compCurrWithPerv == -1) {
                    // negative revision
                    rank.totalEarningCount++;
                    rank.totalNegativeRevision++;

                    BigDecimal repEPS = BigDecimal.valueOf(currentEarning.getReportedEPS());
                    int compWithRep = repEPS.compareTo(currCon);

                    // negative revision leads to m
                    if (compWithRep == -1) {
                        rank.totalNegativeScore++;
                    }

                }

            } else {
                // if sigDiff in date move to next earn.
                i++;
                continue;
            }
        }

        if (rank.totalEarningCount != 0) {
            return rank;
        }

        return null;

    }

    private boolean sigDifferenceInDate(Earning currentEarning, Earning perviousEarning) {
        long MAX_DIFF_IN_DATE = 120;
        long diffInDate = currentEarning.getDate().toEpochDay() - perviousEarning.getDate().toEpochDay();
        if (diffInDate > MAX_DIFF_IN_DATE) {
            return true;
        }
        return false;
    }



}
