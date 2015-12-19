package com.pnb.task.yahoo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.domain.jpa.TaskMetaData.STATUS;
import com.pnb.domain.jpa.TaskMetaData.TASK_TYPE;
import com.pnb.repo.jpa.EarningsRepo;
import com.pnb.task.Task;

@Component
public class YahooParentDataLoadTask extends Task {

    private static final String YAHOO_PARENT_DATALOAD = "YahooParentDataLoad";
    private List<Task> tasks = new ArrayList<Task>();

    // private LocalDate startDate = null; //= LocalDate.of(2015, 11, 01);// DEFAULT_START_DATE;//
    // private LocalDate endDate = LocalDate.of(2015, 11, 02);//startDate.now();// DEFAULT_END_DATE; //

    @Autowired
    private YahooAnncmtTask yahooAnncmtTask;

    @Autowired
    private YahooEarningsTask yahooEarningTask;

    @Autowired
    private EarningsRepo earnRepo;

    private LocalDate actualStartDate = null;

    @Override
    public TaskMetaData process() {
        tasks.add(yahooAnncmtTask);
        tasks.add(yahooEarningTask);
        List<TaskMetaData> metaDataResult = new ArrayList<TaskMetaData>();
        LocalDate lastEarningsTaskDate = earnRepo.getLastEarningsTaskRunDate();
        // taskDate = lastEarningsTaskDate.plusDays(1);
        taskDate = LocalDate.of(2015, 12, 14);
        //taskEndDate = taskDate.plusDays(10);
        taskEndDate = LocalDate.of(2015, 12, 14);
        actualStartDate = taskDate;
        while (!taskDate.isAfter((taskEndDate))) {
            for (Task task : tasks) {
                task.start(taskDate, taskEndDate);
                if (task.getTaskMetaData() != null) {
                    metaDataResult.add(task.getTaskMetaData());
                }
            }
            taskDate = taskDate.plusDays(1);
        }

        TaskMetaData taskMetaData = null;
        STATUS status = STATUS.COMPLETED;
        StringBuffer sb = new StringBuffer();
        status = setStatusAndMessage(metaDataResult, sb);
        taskMetaData = new TaskMetaData(taskEndDate, YAHOO_PARENT_DATALOAD, TASK_TYPE.DATA_LOAD, "PARENT_DATA_LOAD", status, sb.toString());
        return taskMetaData;
    }

    private STATUS setStatusAndMessage(List<TaskMetaData> metaDataResult, StringBuffer sb) {
        STATUS status = STATUS.COMPLETED;
        sb.append("Start:" + actualStartDate.toString() + "|End:" + taskEndDate.toString());
        int errorCount = 0;
        for (TaskMetaData metaData : metaDataResult) {
            if (STATUS.ERROR.equals(metaData.getStatus())) {
                errorCount++;
                status = status.ERROR;
            }
        }
        sb.append("|ErrorCount: " + errorCount);
        return status;
    }
}
