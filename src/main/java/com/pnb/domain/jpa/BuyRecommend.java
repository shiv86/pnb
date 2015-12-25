package com.pnb.domain.jpa;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(name = "buyrecommend",
      indexes = { @Index(name = "buy_recommen_idx", columnList = "symbol,earnings_date") },
      uniqueConstraints={@UniqueConstraint(columnNames = {"symbol" , "earnings_date"})})
@Entity
public class BuyRecommend extends BaseEntity {

    @Id
    @SequenceGenerator(name = "buyrecommen_id_seq", sequenceName = "buyrecommen_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "buyrecommen_id_seq")
    @Column(name = "id")
    private long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "surprise_index")
    private BigDecimal surpriseIndex;

    @Column(name = "earnings_date")
    private LocalDate earningDate;

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "predict_correct")
    private boolean predictCorrect;

    @Column(name = "surprise_pct")
    private Double surprisePercentage;

    public BuyRecommend() {
    }

    public BuyRecommend(String symbol, BigDecimal surpriseIndex, LocalDate earningDate, LocalDate tradeDate) {
        super();
        this.symbol = symbol;
        this.surpriseIndex = surpriseIndex;
        this.earningDate = earningDate;
        this.tradeDate = tradeDate;
    }

    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getSurpriseIndex() {
        return surpriseIndex;
    }

    public LocalDate getEarningDate() {
        return earningDate;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public boolean isPredictCorrect() {
        return predictCorrect;
    }

    public void setPredictCorrect(boolean predictCorrect) {
        this.predictCorrect = predictCorrect;
    }

    public Double getSurprisePercentage() {
        return surprisePercentage;
    }

    public void setSurprisePercentage(Double surprisePercentage) {
        this.surprisePercentage = surprisePercentage;
    }

}
