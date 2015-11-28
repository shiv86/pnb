package com.pnb.algo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EarningRank {

    public String symbol;
    public int totalPostiveRevision = 0;
    public int totalPostiveScore = 0;
    public int totalNegativeRevision = 0;
    public int totalNegativeScore = 0;
    public int totalEarningCount = 0;
    public QUAD quad = QUAD.BELOW_THRESHOLD;

    public BigDecimal THRESHOLD = BigDecimal.valueOf(0.85);
    
    public enum QUAD {
        QPRPS, QPRNS, QNRPS, QNRNS, BELOW_THRESHOLD
    }

    public enum CONSENSUS_REVISION {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    /*
     * Surprise Index varies from -1 TO 1:
     * -ive values indicating the probability of -ive surprise %
     * +ive values inficating the provability of +ive surprise %
     */

    public BigDecimal getSurpriseIndex(BigDecimal consensus, CONSENSUS_REVISION revision) {

        BigDecimal surpriseIndex = BigDecimal.ZERO;

        switch (revision) {
            case POSITIVE:
                if (totalPostiveRevision > 5) {
                    BigDecimal probPositiveSurprise = bd(totalPostiveScore).divide(bd(totalPostiveRevision),2, RoundingMode.HALF_UP);
                    BigDecimal probNegativeSurprise = bd(totalPostiveRevision - totalPostiveScore).divide(bd(totalPostiveRevision),2, RoundingMode.HALF_UP);

                    if (probPositiveSurprise.compareTo(THRESHOLD) == 1) {
                        surpriseIndex = probPositiveSurprise;
                        this.quad = QUAD.QPRPS;
                        
                    } else if (probNegativeSurprise.compareTo(THRESHOLD) == 1) {
                        surpriseIndex = probNegativeSurprise;
                        this.quad = QUAD.QPRNS;
                        
                    }
                }
                break;
            case NEGATIVE:
                if(totalNegativeRevision > 4){
                    
                    BigDecimal probNegativeSurprise = bd(totalNegativeScore).divide(bd(totalNegativeRevision),2, RoundingMode.HALF_UP);
                    BigDecimal probPositiveSurprise = bd(totalNegativeRevision - totalNegativeScore).divide(bd(totalNegativeRevision),2, RoundingMode.HALF_UP);
                    if (probPositiveSurprise.compareTo(THRESHOLD) == 1) {
                        surpriseIndex = probPositiveSurprise.multiply(bd(-1));
                        this.quad = QUAD.QNRPS;
                    } else if (probNegativeSurprise.compareTo(THRESHOLD) == 1) {
                        this.quad = QUAD.QNRNS;
                        surpriseIndex = probNegativeSurprise.multiply(bd(-1));
                    }
                }
                break;
            case NEUTRAL:
                return BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;

        }
        return surpriseIndex;
    }

    private BigDecimal bd(int someInt) {
        return BigDecimal.valueOf(someInt);
    }

}