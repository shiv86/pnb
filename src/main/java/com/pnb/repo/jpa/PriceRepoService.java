package com.pnb.repo.jpa;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.PriceHistory;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;

@Service
public class PriceRepoService {

    @Autowired
    private PriceHistoryRepo priceRepo;
    @Autowired
    TaskMetaRepo taskRepo;

    @Transactional(value = TxType.REQUIRES_NEW)
    public void saveAllPricesForSymbols(List<PriceHistory> allPricesForSymbol) {
        priceRepo.save(allPricesForSymbol);
        priceRepo.flush();
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void saveError(String symbol, StringBuffer sb, LocalDate taskEndDate) {
        taskRepo.saveAndFlush(new TaskMetaData(taskEndDate, "PRICE_HISTORY", TASK_TYPE.DATA_LOAD, "Symbol:" + symbol, STATUS.ERROR, sb.toString()));
    }

}
