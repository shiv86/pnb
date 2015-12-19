package com.pnb.algo.earnings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.algo.earnings.EarningRank.CONSENSUS_REVISION;
import com.pnb.algo.earnings.EarningRank.QUAD;
import com.pnb.domain.jpa.Earning;
import com.pnb.repo.jpa.EarningsRepo;

@Service
public class SurpriseIndexService {

    @Autowired
    private EarningsRepo earnRepo;

    @Autowired
    private EarningRankAlgo rankAlgo;

    public SurpriseIndex getSurpriseIndex(String symbol, LocalDate taskDate) {
        SurpriseIndex surpriseIndex = new SurpriseIndex();
        Earning earningToBePredicted = earnRepo.findBySymbolAndDate(symbol, taskDate);
        List<Earning> allHisEarningForSym = earnRepo.getHistoricalPopulateEarnings(symbol, taskDate.toString());
        if (earningToBePredicted != null && earningToBePredicted.getConsensusEPS() != null && allHisEarningForSym.size() > 1) {
            EarningRank earnRank = rankAlgo.getRank(symbol, taskDate, allHisEarningForSym);
            if (earnRank != null) {
                CONSENSUS_REVISION conRevision = Earning.getConsensusRevision(earningToBePredicted, allHisEarningForSym);
                surpriseIndex.value = earnRank.getSurpriseIndex(conRevision);
                surpriseIndex.quad = earnRank.quad;
            }
        }
        return surpriseIndex;
    }
    
    public class SurpriseIndex {
        public BigDecimal value = BigDecimal.ZERO;
        public QUAD quad = QUAD.BELOW_THRESHOLD;
    }

}
