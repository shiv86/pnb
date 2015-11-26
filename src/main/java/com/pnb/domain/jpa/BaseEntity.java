package com.pnb.domain.jpa;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_date")
    private LocalDate createdDate;
    @Column(name = "updated_date")
    private LocalDate updatedDate;
    @Column(name = "updated_time")
    private LocalTime updateTime;

    @PrePersist
    private void onSave() {
        if (this.createdDate == null) {
            this.createdDate = LocalDate.now();
        }
        this.updatedDate = LocalDate.now();
        this.updateTime = LocalTime.now();
    }

}
