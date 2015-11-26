package com.pnb.task.yahoo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pnb.algo.EarningRank;
import com.pnb.algo.EarningRank.CONSENSUS_REVISION;
import com.pnb.algo.EarningRank.QUAD;
import com.pnb.algo.EarningRankAlgo;
import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.repo.jpa.EarningsRepo;

@Component
public class EarningsRevisionGame extends YahooDataCleaner {

    private static final String EARNINGS_REVISION_GAME = "EARNINGS_REVISION_GAME";

    @Autowired
    private EarningsRepo earnRepo;

    @Autowired
    private EarningRankAlgo rankAlgo;

    Map<String, String> quadOne = new HashMap<String, String>();

    @Override
    protected TaskMetaData process() {

        int totalPrediction = 0;
        int success = 0;
        GameResult q1 = new GameResult(QUAD.QPRPS);
        GameResult q2 = new GameResult(QUAD.QPRNS);
        GameResult q3 = new GameResult(QUAD.QNRNS);
        GameResult q4 = new GameResult(QUAD.QNRPS);

        while (!taskDate.isAfter((taskEndDate))) {
            List<Earning> allEarningAnnoucement = earnRepo.findByDateAndConsensusEPSNotNull(taskDate.toString());
            System.out.println("Earning Prediction for Date:" + taskDate.toString());
            for (Earning earningAnnouncement : allEarningAnnoucement) {
                List<Earning> allHisEarningForSym = earnRepo.getHistoricalPopulateEarnings(earningAnnouncement.getSymbol(), taskDate.toString());
                if (allHisEarningForSym.size() > 1) {
                    BigDecimal currentCon = bd(earningAnnouncement.getConsensusEPS());
                    BigDecimal perviousCon = bd(allHisEarningForSym.get(allHisEarningForSym.size() - 1).getConsensusEPS());

                    EarningRank earnRank = rankAlgo.getRank(earningAnnouncement.getSymbol(), taskDate, allHisEarningForSym);

                    if (earnRank != null) {
                        BigDecimal surpriseIndex = earnRank.getSurpriseIndex(currentCon, getConsensusType(currentCon, perviousCon));

                        if (!surpriseIndex.equals(BigDecimal.ZERO)) {

                            totalPrediction++;
                            switch (earnRank.quad) {
                                case QPRPS:
                                    q1.totalNumQuadPrediction++;
                                    break;
                                case QPRNS:
                                    q2.totalNumQuadPrediction++;
                                    break;
                                case QNRNS:
                                    q3.totalNumQuadPrediction++;
                                    break;
                                case QNRPS:
                                    q4.totalNumQuadPrediction++;
                                    break;

                                default:
                                    break;

                            }

                            if (surpriseIndex.signum() == 1 && Math.signum(earningAnnouncement.getReportedEPS()) == 1) {

                                switch (earnRank.quad) {
                                    case QPRPS:
                                        quadOne.put(earningAnnouncement.getSymbol() + "|" + earningAnnouncement.getDate().toString(),
                                                surpriseIndex.toString() + "|" + earningAnnouncement.getReportedEPS().toString());
                                        q1.totalSuccessFullPrediction++;
                                        break;
                                    case QPRNS:
                                        q2.totalSuccessFullPrediction++;
                                        break;

                                    default:
                                        break;

                                }

                                success++;
                            }

                            if (surpriseIndex.signum() == -1 && Math.signum(earningAnnouncement.getReportedEPS()) == -1) {

                                switch (earnRank.quad) {
                                    case QNRNS:
                                        q3.totalSuccessFullPrediction++;
                                        break;
                                    case QNRPS:
                                        q4.totalSuccessFullPrediction++;
                                        break;

                                    default:
                                        break;

                                }

                                success++;
                            }
                            System.out.println(earningAnnouncement.getSymbol() + "|" + surpriseIndex.toString() + "|"
                                    + earningAnnouncement.getReportedEPS());

                        }

                    }
                }
            }
            taskDate = taskDate.plusDays(1);
        }

        System.out.println("Out of " + totalPrediction + " predictions, " + success + " were successfull");
        quadOne.keySet().forEach( x-> { System.out.println(x+"|"+quadOne.get(x));});

        return null;
    }

    public static CONSENSUS_REVISION getConsensusType(BigDecimal currentCon, BigDecimal perviousCon) {

        int compCon = currentCon.compareTo(perviousCon);
        if (compCon == 1) {
            return CONSENSUS_REVISION.POSITIVE;
        } else if (compCon == -1) {
            return CONSENSUS_REVISION.NEGATIVE;
        }
        return CONSENSUS_REVISION.NEUTRAL;
    }

    private BigDecimal bd(Double someInt) {
        return BigDecimal.valueOf(someInt);
    }

    class GameResult {
        QUAD quad;
        int totalNumQuadPrediction;
        int totalSuccessFullPrediction;
        LocalDate startDate;
        LocalDate endDate;

        GameResult(QUAD q) {
            this.quad = quad;
        }
    }

}
