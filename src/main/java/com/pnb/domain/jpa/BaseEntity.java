package com.pnb.domain.jpa;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.PrePersist;

public class BaseEntity {

    @Column(name = "created_date")
    private LocalDate createdDate;
    @Column(name = "updated_date")
    private LocalDate updatedDate;
    
    @PrePersist
    private void onSave(){
        if(this.createdDate == null){
            this.createdDate = LocalDate.now();
        }
        this.updatedDate = LocalDate.now();
    }

}
