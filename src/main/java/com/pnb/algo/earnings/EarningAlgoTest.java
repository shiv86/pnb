package com.pnb.algo.earnings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.Earning;
import com.pnb.repo.jpa.EarningsRepo;

@Service
public class EarningAlgoTest {

    @Autowired
    private EarningsRepo earnRepo;

    @Autowired
    private EarningRankAlgo rankAlgo;

    //@PostConstruct
    public void start(){
        
        LocalDate earnDate = LocalDate.of(2008,04,23);
        List<Earning> histEarnings = earnRepo.getHistoricalPopulateEarnings("WIRE", earnDate.toString());
        EarningRank rank = rankAlgo.getRank("WIRE", earnDate, histEarnings);
        
        Earning earning = earnRepo.findBySymbolAndDate("WIRE", earnDate);
        BigDecimal currentCon = bd(earning.getConsensusEPS());
        BigDecimal prevCon = bd(histEarnings.get(histEarnings.size() -1).getConsensusEPS());
        
        BigDecimal surpriseIndex = rank.getSurpriseIndex(Earning.getConsensusType(currentCon, prevCon));
        System.out.println(surpriseIndex);
    }
    
    private BigDecimal bd(Double someInt) {
        return BigDecimal.valueOf(someInt);
    }

}
