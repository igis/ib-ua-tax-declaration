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
    double totalBuyCostUsd;
    double totalSellCost;
    double totalSellCostUsd;
    double itemTotalProfitLoss;
    double itemTotalProfitLossUsd;
    double totalProfitLoss;
    double totalProfitLossUsd;
    double totalProfit;
    double totalProfitUsd;
    double totalLoss;
    double totalLossUsd;
    double totalTaxPdfo;
    double totalTaxVz;

    @Value
    @Builder(toBuilder = true)
    public static class Item {
        @NonNull
        String symbol;
        String description;
        double quantity;
        double buyCost;
        double buyCostUsd;
        double sellCost;
        double sellCostUsd;
        double profitLoss;
        double profitLossUsd;
        double taxPdfo;
        double taxVz;
    }
}
