package com.pnb.repo.jpa;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.BuyRecommend;
import com.pnb.domain.jpa.EarnReturn;
import com.pnb.domain.jpa.PriceHistory;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;

@Service
public class RepoService {

    @Autowired
    private PriceHistoryRepo priceRepo;
    @Autowired
    private TaskMetaRepo taskRepo;
    @Autowired
    private BuyRecommendRepo buyRecommendRepo;
    @Autowired
    private EarnReturnRepo earnReturnRepo;

    @Transactional(value = TxType.REQUIRES_NEW)
    public void saveAllPricesForSymbols(List<PriceHistory> allPricesForSymbol) {
        priceRepo.save(allPricesForSymbol);
        priceRepo.flush();
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void saveAllEarnReturn(List<EarnReturn> allEarnReturn) {
        earnReturnRepo.save(allEarnReturn);
        earnReturnRepo.flush();
    }

    
    @Transactional(value = TxType.REQUIRES_NEW)    
    public void saveBuyRecommendation(BuyRecommend buyRecommendation){
        buyRecommendRepo.save(buyRecommendation);
        priceRepo.flush();
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void saveError(String symbol, StringBuffer sb, LocalDate taskEndDate) {
        taskRepo.saveAndFlush(new TaskMetaData(taskEndDate, "PRICE_HISTORY", TASK_TYPE.DATA_LOAD, "Symbol:" + symbol, STATUS.ERROR, sb.toString()));
    }

}
