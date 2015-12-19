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

@Table(name = "earn_return", indexes = { @Index(name = "earn_return_idx", columnList = "symbol,trade_date") })
@Entity
public class EarnReturn extends BaseEntity {

    @Id
    @SequenceGenerator(name = "earn_return_id_seq", sequenceName = "earn_return_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "earn_return_id_seq")
    @Column(name = "id")
    private long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RETURN_TYPE returnType;

    @Column(name = "open_open")
    private Double openOpen;

    @Column(name = "open_close")
    private Double openClose;

    @Column(name = "close_open")
    private Double closeOpen;

    @Column(name = "close_close")
    private Double closeClose;

    public EarnReturn(String symbol, LocalDate tradeDate, RETURN_TYPE returnType, Double openOpen, Double openClose, Double closeOpen,
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

    

}
