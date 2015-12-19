package com.pnb.domain.jpa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

import com.pnb.algo.earnings.EarningRank.CONSENSUS_REVISION;

@Table(name = "earning", indexes = { @Index(name = "earnings_idx", columnList = "symbol,earnings_date") })
@Entity
public class Earning extends BaseEntity {

    @Id
    @SequenceGenerator(name = "earnings_id_seq", sequenceName = "earnings_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "earnings_id_seq")
    @Column(name = "id")
    private long id;
    @Column(name = "company_name")
    private String companyName;
    @Column(name = "symbol")
    private String symbol;
    @Column(name = "anncmt_time")
    @Enumerated(EnumType.STRING)
    private ANNCMT_TIME annoucementTime;
    @Column(name = "raw_anncmt_time")
    private String rawAnnoucementTime;
    @Column(name = "surprise_pct")
    private Double surprisePercentage;
    @Column(name = "reported_eps")
    private Double reportedEPS;
    @Column(name = "consensus_eps")
    private Double consensusEPS;
    @Column(name = "earnings_date")
    private LocalDate date;
    @Column(name = "trade_date")
    private LocalDate tradeDate;
    @Column(name = "quarter")
    @Enumerated(EnumType.STRING)
    private QUARTER quarter;
    @Column(name = "earnings_anncmt_populated")
    private boolean earningsAnncmtPopulated;
    @Column(name = "earnings_eps_populated")
    private boolean earningsSurprisePopulated;
    @Column(name = "error")
    private String errors;

    private final static String EARNINGS_ANNCMT_URL = "http://biz.yahoo.com/research/earncal/";
    private final static String EARNINGS_SURPRISE_URL = "http://biz.yahoo.com/z/";

    public Earning() {
    }

    public enum EARNINGS_TYPE {
        ANNCMT(EARNINGS_ANNCMT_URL), EPS(EARNINGS_SURPRISE_URL);
        private String url;

        EARNINGS_TYPE(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    public Earning(String companyName, String symbol, ANNCMT_TIME annoucementTime, String rawAnnouncementTime, LocalDate date) {
        this.companyName = companyName;
        this.symbol = symbol;
        this.annoucementTime = annoucementTime;
        this.rawAnnoucementTime = rawAnnouncementTime;
        this.date = date;
        this.quarter = QUARTER.getQuarter(date.getMonthValue());
    }

    enum QUARTER {
        Q1, Q2, Q3, Q4;

        public static QUARTER getQuarter(int earningAnnoucementMonth) {
            QUARTER getQuarter = null;
            switch (earningAnnoucementMonth) {
                case 1:
                case 2:
                case 3:
                    return Q4;
                case 4:
                case 5:
                case 6:
                    return Q1;
                case 7:
                case 8:
                case 9:
                    return Q2;
                case 10:
                case 11:
                case 12:
                    return Q3;
                default:
                    System.out.println("Invalid Month");
            }
            return getQuarter;
        }
    }

    public enum ANNCMT_TIME {
        BEFORE_OPEN, AFTER_CLOSE, NOT_SUPPLIED, DURING_MKT_HRS,
    }

    public enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }

    public void setSurprisePercentage(Double surprisePercentage) {
        this.surprisePercentage = surprisePercentage;
    }

    // Usually required when consensus == 0
    public void setManuallyCalculatedPercentage() {
        if (this.getSurprisePercentage() == null && (this.getReportedEPS() != null && this.getConsensusEPS() != null)) {
            double difference = this.getReportedEPS() - this.getConsensusEPS();
            double suprisePct = (difference / this.getConsensusEPS()) * 100;
            if (this.getConsensusEPS() == 0) {
                suprisePct = difference * 100;
            }
            this.setSurprisePercentage(suprisePct);
        }
    }

    public void setReportedEPS(Double reportedEPS) {
        this.reportedEPS = reportedEPS;
    }

    public void setConsensusEPS(Double consensusEPS) {
        this.consensusEPS = consensusEPS;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Earning other = (Earning) obj;
        if (symbol == null) {
            if (other.symbol != null)
                return false;
        } else if (!symbol.equals(other.symbol))
            return false;
        return true;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getSymbol() {
        return symbol;
    }

    public ANNCMT_TIME getAnncmtTime() {
        return annoucementTime;
    }

    public Double getSurprisePercentage() {
        return surprisePercentage;
    }

    public Double getReportedEPS() {
        return reportedEPS;
    }

    public Double getConsensusEPS() {
        return consensusEPS;
    }

    public BigDecimal getConsensusEPSBD() {
        return bd(consensusEPS);
    }

    public LocalDate getDate() {
        return date;
    }

    public QUARTER getQuarter() {
        return quarter;
    }

    @Override
    public String toString() {
        return "Earning [companyName=" + companyName + ", symbol=" + symbol + ", annoucementTime=" + annoucementTime + ", surprisePercentage="
                + surprisePercentage + ", reportedEPS=" + reportedEPS + ", consensusEPS=" + consensusEPS + ", date=" + date + ", quarter="
                + quarter + "]";
    }

    public boolean isAnncmtPopulated() {
        return earningsAnncmtPopulated;
    }

    public void setEarningsAnnoucementPopulated(boolean earningsCalPopulated) {
        this.earningsAnncmtPopulated = earningsCalPopulated;
    }

    public boolean isEPSPopulated() {
        return earningsSurprisePopulated;
    }

    public void setEPSPopulated(boolean earningsSurprisePopulated) {
        this.earningsSurprisePopulated = earningsSurprisePopulated;
    }

    public String getRawAnnoucementTime() {
        return rawAnnoucementTime;
    }

    public void setRawAnnoucementTime(String rawAnnoucementTime) {
        this.rawAnnoucementTime = rawAnnoucementTime;
    }

    public void setAnnoucementTime(ANNCMT_TIME annoucementTime) {
        this.annoucementTime = annoucementTime;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    private BigDecimal bd(Double someInt) {
        return BigDecimal.valueOf(someInt);
    }

    public static CONSENSUS_REVISION getConsensusType(BigDecimal currentCon, BigDecimal perviousCon) {

        int compCon = currentCon.compareTo(perviousCon);
        if (compCon == 1) {
            return CONSENSUS_REVISION.POSITIVE;
        } else if (compCon == -1) {
            return CONSENSUS_REVISION.NEGATIVE;
        }
        return CONSENSUS_REVISION.NEUTRAL;
    }

    public static CONSENSUS_REVISION getConsensusRevision(Earning earningToBePredicted, List<Earning> allHisEarningForSymAsc) {
        BigDecimal currentCon = earningToBePredicted.getConsensusEPSBD();
        BigDecimal perviousCon = allHisEarningForSymAsc.get(allHisEarningForSymAsc.size() - 1).getConsensusEPSBD();
        CONSENSUS_REVISION conRevision = Earning.getConsensusType(currentCon, perviousCon);
        return conRevision;
    }
    
    public static boolean negativeSurpriseIsPredicted(BigDecimal surpriseIndex) {
        return surpriseIndex.signum() == -1;
    }

    public static boolean postiveSurpriseIsPredicted(BigDecimal surpriseIndex) {
        return surpriseIndex.signum() == 1;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    

}