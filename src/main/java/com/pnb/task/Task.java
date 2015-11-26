package com.pnb.task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;

import com.pnb.domain.jpa.TaskMetaData;
import com.pnb.repo.jpa.TaskMetaRepo;

public abstract class Task {

    protected LocalDate taskDate;
    protected LocalDate taskEndDate;
    private long startTime;
    private long endTime;
    private String taskName;
    protected TaskMetaData taskMetaData;

    @Autowired
    TaskMetaRepo taskMetaRepo;

    public Task() {
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void start(LocalDate taskDate, LocalDate endDate) {
        this.taskDate = taskDate;
        this.taskEndDate = endDate;
        preProcessing();
        this.taskMetaData = process();
        postProcessing();
    }

    private void preProcessing() {
        Date date = new Date();
        this.startTime = date.getTime();
    }

    private void postProcessing() {
        if (this.taskMetaData != null) {
            Date date = new Date();
            this.endTime = date.getTime();
            long totalTime = (endTime - startTime);
            BigDecimal totalTimeInSec = BigDecimal.valueOf(totalTime).divide(BigDecimal.valueOf(1000L), 2, BigDecimal.ROUND_HALF_UP);
            taskMetaData.setTotalTime(totalTimeInSec);
            taskMetaRepo.saveAndFlush(taskMetaData);
        }
    }

    protected abstract TaskMetaData process();

    public String getTaskName() {
        return taskName;
    }

    public TaskMetaData getTaskMetaData() {
        return this.taskMetaData;
    }

}
