package com.pnb.domain.jpa;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Table(name = "task_meta")
@Entity
public class TaskMetaData extends BaseEntity {

    @Id
    @SequenceGenerator(name = "task_meta_data_id_seq", sequenceName = "task_meta_data_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_meta_data_id_seq")
    @Column(name = "id")
    private long id;
    @Column(name = "date_of_task")
    private LocalDate dateOfTask;
    @Column(name = "message")
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private STATUS status;

    @Column(name = "task_type")
    @Enumerated(EnumType.STRING)
    private TASK_TYPE taskType;

    @Column(name = "task_sub_type")
    private String taskSubType;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "total_time")
    private BigDecimal totalTime;

    public enum STATUS {
        COMPLETED, WARN, ERROR
    }

    public enum TASK_TYPE {
        DATA_LOAD
    }

    public TaskMetaData() {

    }

    public TaskMetaData(LocalDate dateOfTask, String taskName, TASK_TYPE taskType, String taskSubType, STATUS status, String message) {
        super();
        this.dateOfTask = dateOfTask;
        this.taskName = taskName;
        this.taskType = taskType;
        this.taskSubType = taskSubType;
        this.status = status;
        this.message = message;
    }

    public void setTotalTime(BigDecimal totalTimeInSec) {
        this.totalTime = totalTimeInSec;

    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public TASK_TYPE getTaskType() {
        return taskType;
    }

    public void setTaskType(TASK_TYPE taskType) {
        this.taskType = taskType;
    }

    public String getTaskSubType() {
        return taskSubType;
    }

    public void setTaskSubType(String taskSubType) {
        this.taskSubType = taskSubType;
    }

}