package net.igis.ib.taxer;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class DividendReport {
    @Singular
    List<Item> items;
    double totalAmount;
    double totalAmountUsd;
    double totalTaxPdfo;
    double totalTaxVz;

    @Value
    @Builder(toBuilder = true)
    public static class Item {
        @NonNull
        String symbol;
        String description;
        double quantity;
        double amount;
        double amountUsd;
        double taxPdfo;
        double taxVz;
    }
}
