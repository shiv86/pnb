package com.pnb.task.yahoo;

import java.time.LocalDate;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import com.pnb.domain.jpa.Earning.EARNINGS_TYPE;
import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.task.Task;
import com.pnb.util.YahooUtil;

public abstract class YahooTask extends Task {

    protected TaskMetaData buildErrorMeta(String taskName, EARNINGS_TYPE earningsType, Document earningDoc, LocalDate taskDate, String eMessage) {
        StringBuffer sb = new StringBuffer();
        
        if (StringUtil.isBlank(sb.toString())) {
            sb.append("Something went wrong for this date please check logs or debug:" + YahooUtil.getYahooURL(earningsType, taskDate));
        }
        return new TaskMetaData(taskDate, taskName, TASK_TYPE.DATA_LOAD, earningsType.toString(), STATUS.ERROR, sb.toString());
    }
}