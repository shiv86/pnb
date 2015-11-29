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

@Table(name = "price_history", indexes = { @Index(name = "price_history_idx", columnList = "symbol,date", unique = true) })
@Entity
public class PriceHistory {

    @Id
    @SequenceGenerator(name = "price_history_id_seq", sequenceName = "price_history_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_history_id_seq")
    @Column(name = "id")
    private long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "open")
    private Double open;

    @Column(name = "high")
    private Double high;

    @Column(name = "low")
    private Double low;

    @Column(name = "close")
    private Double close;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "adj_close")
    private Double adjClose;

    @Enumerated(EnumType.STRING)
    @Column(name = "freq")
    private FREQUENCY freq;

    public enum FREQUENCY {
        DAILY, WEEKLY, MONTHLY
    }

    public PriceHistory() {

    }

    public PriceHistory(String symbol, LocalDate date, Double open, Double high, Double low, Double close, Long volume, Double adjClose,
            FREQUENCY freq) {
        super();
        this.symbol = symbol;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.adjClose = adjClose;
        this.freq = freq;
    }

    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDate getDate() {
        return date;
    }

    public Double getOpen() {
        return open;
    }

    public Double getHigh() {
        return high;
    }

    public Double getLow() {
        return low;
    }

    public Double getClose() {
        return close;
    }

    public Long getVolume() {
        return volume;
    }

    public Double getAdjClose() {
        return adjClose;
    }

}
