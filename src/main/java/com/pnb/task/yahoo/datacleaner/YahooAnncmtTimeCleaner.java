package com.pnb.task.yahoo.datacleaner;

import com.pnb.domain.jpa.TaskMetaData;


/*
 * @Earning.annoucementTime is at times missing and is defaulted to ANNCMT_TIME.NOT_SUPPLIED
 * However this value can be derived from other Earning of the same symbol.
 */
public class YahooAnncmtTimeCleaner extends YahooDataCleaner {

    @Override
    protected TaskMetaData process() {
        // TODO Auto-generated method stub
        return null;
    }

}
