package com.pnb;

import java.util.Optional;

public class Playground {

    public static void main(String[] args) throws InterruptedException {
        Optional<String> suprisePercentage = Optional.of("250");
        suprisePercentage.filter( x -> "250".equals( x ) )
        .ifPresent( x -> System.out.println( x + " is ok!" ) );
    }
    
    
}
