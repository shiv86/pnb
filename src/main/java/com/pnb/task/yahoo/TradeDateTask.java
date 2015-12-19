package com.pnb.task.yahoo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pnb.domain.jpa.Earning;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.repo.jpa.EarningsRepo;
import com.pnb.task.Task;
import com.pnb.util.YahooUtil;

/*
 * Once off task to to populate the TradeDate for earnings results.
 */

@Component
public class TradeDateTask extends Task {

    private static final String TRADE_DATE_TASK = "TRADE_DATE_TASK";
    @Autowired
    private EarningsRepo earnRepo;
    @Autowired
    private YahooUtil yahooUtil;
    
    @Override
    protected TaskMetaData process() {
        int totalAdded = 0;
        List<Earning> earnings  = earnRepo.findAll();
        List<Earning> earningsToUpdate = new ArrayList<Earning>();
        try {
            for(Earning earning : earnings){
                LocalDate tradeDate = yahooUtil.getTradeDate(earning);
                if(tradeDate != null){        
                    totalAdded++;
                    earning.setTradeDate(tradeDate);
                    earningsToUpdate.add(earning);
                }    
            }
            earnRepo.save(earningsToUpdate);
        } catch (Exception e) {
            System.err.println("Exception has occurred:");
            System.err.println(e);
        }

        return new TaskMetaData(taskDate, TRADE_DATE_TASK, TASK_TYPE.PERSIST, "ONCE_OFF", STATUS.COMPLETED, "Total TradeDate populated: "
                + totalAdded+"/"+earnings.size());

    }

}
