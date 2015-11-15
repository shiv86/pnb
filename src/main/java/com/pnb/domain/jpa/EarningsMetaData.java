package com.pnb.domain.jpa;

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

import com.pnb.YahooEarningsFeedEngine.EARNINGS_TYPE;

@Table(name = "earnings_meta_data")
@Entity
public class EarningsMetaData extends BaseEntity {

    @Id
    @SequenceGenerator(name = "earnings_meta_data_id_seq", sequenceName = "earnings_meta_data_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "earnings_meta_data_id_seq")
    @Column(name = "id")
    private long id;
    @Column(name = "date_of_task")
    private LocalDate dateOfTask;
    @Column(name = "date_attempted")
    private LocalDate dateAttempted;
    @Column(name = "earning_type")
    @Enumerated(EnumType.STRING)
    private EARNINGS_TYPE earningType;
    @Column(name = "message")
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private STATUS status;

    public enum STATUS {
        COMPLETED_DATA_LOAD, ERROR
    }

    public EarningsMetaData() {

    }

    public EarningsMetaData(LocalDate dateAttempted, EARNINGS_TYPE earningType, STATUS status, String message) {
        super();
        this.dateOfTask = LocalDate.now();
        this.dateAttempted = dateAttempted;
        this.earningType = earningType;
        this.message = message;
        this.status = status;
    }

}