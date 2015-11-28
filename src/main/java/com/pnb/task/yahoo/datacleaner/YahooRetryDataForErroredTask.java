package com.pnb.task.yahoo.datacleaner;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.repo.jpa.TaskMetaRepo;
import com.pnb.task.yahoo.YahooAnncmtTask;
import com.pnb.task.yahoo.YahooEarningsTask;

@Service
public class YahooRetryDataForErroredTask extends YahooDataCleaner {

    private static final String YAHOO_RETRY_DATALOAD = "YahooRetryDataLoad";

    @Autowired
    TaskMetaRepo taskRepo;

    @Autowired
    private YahooAnncmtTask yahooAnncmtTask;

    @Autowired
    private YahooEarningsTask yahooEarningTask;

    @Override
    protected TaskMetaData process() {
        List<Date> javaUtilDatesWithErrors = taskRepo.getAllDateWithYahooDataLoadErrors();
        List<LocalDate> datesWithErrors = getLocalDates(javaUtilDatesWithErrors);
        List<TaskMetaData> taskMetaWithErrorDates = taskRepo.getAllTaskMetaWithYahooDataLoadErrors();
        for (LocalDate localDate : datesWithErrors) {
            yahooAnncmtTask.start(localDate, localDate);
            yahooEarningTask.start(localDate, localDate);
        }
        taskRepo.delete(taskMetaWithErrorDates);
        return new TaskMetaData(taskEndDate, YAHOO_RETRY_DATALOAD, TASK_TYPE.DATA_LOAD, "PARENT_DATA_LOAD", STATUS.COMPLETED, null);
    }

    private List<LocalDate> getLocalDates(List<Date> javaUtilDatesWithErrors) {
        List<LocalDate> datesWithErrors = new ArrayList<LocalDate>();
        for (Date date : javaUtilDatesWithErrors) {
            datesWithErrors.add(LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(date) ));
        }
        return datesWithErrors;
    }

}
