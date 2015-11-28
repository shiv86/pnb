package com.pnb.repo.jpa;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pnb.domain.jpa.TaskMetaData;

public interface TaskMetaRepo extends JpaRepository<TaskMetaData, Long> {
    
    @Query(value = "select distinct(date_of_task) from task_meta where status = 'ERROR' and task_name in ('YAHOO_ANNCMT','YAHOO_EARNINGS') and date_of_task > '2002-01-01';", nativeQuery = true)
    List<Date> getAllDateWithYahooDataLoadErrors();
    
    @Query(value = "select * from task_meta where status = 'ERROR' and task_name in ('YAHOO_ANNCMT','YAHOO_EARNINGS') and date_of_task > '2002-01-01';", nativeQuery = true)
    List<TaskMetaData> getAllTaskMetaWithYahooDataLoadErrors();

}
