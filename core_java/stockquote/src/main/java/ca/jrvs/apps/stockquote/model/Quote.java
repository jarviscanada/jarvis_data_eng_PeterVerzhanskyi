package ca.jrvs.apps.stockquote.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Quote {

    private String symbol;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal price;
    private int volume;
    private LocalDate latestTradingDay;
    private BigDecimal previousClose;
    private BigDecimal change;
    private String changePercent;

    public Quote(String symbol, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal price,
                 int volume, LocalDate latestTradingDay, BigDecimal previousClose,
                 BigDecimal change, String changePercent) {
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.price = price;
        this.volume = volume;
        this.latestTradingDay = latestTradingDay;
        this.previousClose = previousClose;
        this.change = change;
        this.changePercent = changePercent;
    }

    // Add getters
    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getVolume() {
        return volume;
    }

    public LocalDate getLatestTradingDay() {
        return latestTradingDay;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public BigDecimal getChange() {
        return change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "symbol='" + symbol + '\'' +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", price=" + price +
                ", volume=" + volume +
                ", latestTradingDay=" + latestTradingDay +
                ", previousClose=" + previousClose +
                ", change=" + change +
                ", changePercent='" + changePercent + '\'' +
                '}';
    }
}
