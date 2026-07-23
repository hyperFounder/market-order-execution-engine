package com.hft.engine.marketorderexecutionengine.exception;

public class InsufficientLiquidityException extends RuntimeException {

    private final String symbol;
    private final long quantity;

    public InsufficientLiquidityException(String symbol, long quantity){
        super("Insufficient liquidity");
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public String getSymbol() {return symbol;}
    public long getQuantity() {return quantity;}


}
