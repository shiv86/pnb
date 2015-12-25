package com.pnb.task.yahoo;

import java.time.LocalDate;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import com.pnb.YahooEarningsFeedEngine;
import com.pnb.domain.jpa.Earning.EARNINGS_TYPE;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.task.Task;
import com.pnb.util.YahooUtil;

public abstract class YahooTask extends Task {
    
    private static final String IGNORE_LIST = "QIHU";

    protected TaskMetaData buildErrorMeta(String taskName, EARNINGS_TYPE earningsType, Document earningDoc, LocalDate taskDate, String eMessage) {
        StringBuffer sb = new StringBuffer();

        if (StringUtil.isBlank(sb.toString())) {
            sb.append("Error for :" + YahooUtil.getYahooURL(earningsType, taskDate));
        }

        if (!StringUtil.isBlank(eMessage)) {
            if (eMessage.length() > 150) {
                sb.append(eMessage.substring(0, 150));
            } else {
                sb.append(eMessage);
            }
        }

        return new TaskMetaData(taskDate, taskName, TASK_TYPE.DATA_LOAD, earningsType.toString(), STATUS.ERROR, sb.toString());
    }
    
    protected boolean ignoreSecurity(String symbol){
        if(IGNORE_LIST.contains(symbol.toUpperCase())){
            return true;
        }
        return false;
    }

}
