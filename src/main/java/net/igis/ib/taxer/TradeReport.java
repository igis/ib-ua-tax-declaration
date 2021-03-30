package net.igis.ib.taxer;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class TradeReport {
    @Singular
    List<Item> items;
    double totalBuyCost;
    double totalSellCost;
    double itemTotalProfitLoss;
    double totalProfitLoss;
    double totalProfit;
    double totalLoss;
    double totalTaxPdfo;
    double totalTaxVz;

    @Value
    @Builder(toBuilder = true)
    public static class Item {
        @NonNull
        String symbol;
        String description;
        int quantity;
        double buyCost;
        double sellCost;
        double profitLoss;
        double taxPdfo;
        double taxVz;
    }
}
