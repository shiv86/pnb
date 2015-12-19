package com.pnb.domain.jpa;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Table(name = "buyreturn", indexes = { @Index(name = "buy_return_idx", columnList = "symbol,trade_date") })
@Entity
public class BuyReturn extends BaseEntity {

    @Id
    @SequenceGenerator(name = "buyreturn_id_seq", sequenceName = "buyreturn_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "buyreturn_id_seq")
    @Column(name = "id")
    private long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "type")
   // @Enumerated(EnumType.STRING)
    private RETURN_TYPE returnType;

    @Column(name = "open_open")
    private Double openOpen;

    @Column(name = "open_close")
    private Double openClose;

    @Column(name = "close_open")
    private Double closeOpen;

    @Column(name = "close_close")
    private Double closeClose;
    
    public BuyReturn(){
    }
    
    public BuyReturn(String symbol, LocalDate tradeDate, RETURN_TYPE returnType, Double openOpen, Double openClose, Double closeOpen,
            Double closeClose) {
        super();
        this.symbol = symbol;
        this.tradeDate = tradeDate;
        this.returnType = returnType;
        this.openOpen = openOpen;
        this.openClose = openClose;
        this.closeOpen = closeOpen;
        this.closeClose = closeClose;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public RETURN_TYPE getReturnType() {
        return returnType;
    }

    public void setReturnType(RETURN_TYPE returnType) {
        this.returnType = returnType;
    }

    public Double getOpenOpen() {
        return openOpen;
    }

    public void setOpenOpen(Double openOpen) {
        this.openOpen = openOpen;
    }

    public Double getOpenClose() {
        return openClose;
    }

    public void setOpenClose(Double openClose) {
        this.openClose = openClose;
    }

    public Double getCloseOpen() {
        return closeOpen;
    }

    public void setCloseOpen(Double closeOpen) {
        this.closeOpen = closeOpen;
    }

    public Double getCloseClose() {
        return closeClose;
    }

    public void setCloseClose(Double closeClose) {
        this.closeClose = closeClose;
    }
    
    

}
