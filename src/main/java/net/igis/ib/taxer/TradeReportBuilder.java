package net.igis.ib.taxer;

import com.ib.flex.FlexStatement;
import com.ib.flex.Lot;
import com.ib.flex.Trade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TradeReportBuilder {

    public static TradeReport build(Collection<FlexStatement> flexStatements, double previousPeriodLoss)
            throws IOException, InterruptedException {

        double totalBuyCost = 0;
        double totalSellCost = 0;

        List<TradeReport.Item> allItems = new ArrayList<>();

        for (FlexStatement flexStatement : flexStatements) {
            List<TradeReport.Item> items = buildTradeReportItemList(flexStatement);

            for (TradeReport.Item item : items) {
                totalBuyCost += item.getBuyCost();
                totalSellCost += item.getSellCost();

                allItems.add(item);
            }
        }

        double itemTotalProfitLoss = totalSellCost - totalBuyCost;
        double totalProfitLoss = itemTotalProfitLoss - previousPeriodLoss;
        double totalTaxPdfo = totalProfitLoss > 0.0 ? (totalProfitLoss / 100.0) * 18.0 : 0.0;
        double totalTaxVz = totalProfitLoss > 0.0 ? (totalProfitLoss / 100.0) * 1.5 : 0.0;

        double totalProfit = totalProfitLoss > 0.0 ? totalProfitLoss : 0;
        double totalLoss = totalProfitLoss < 0.0 ? Math.abs(totalProfitLoss) : 0;

        System.out.println("=================");
        System.out.println(String.format("Total: %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f",
                totalBuyCost, totalSellCost, totalProfitLoss, totalProfit, totalLoss, totalTaxPdfo, totalTaxVz));

        TradeReport tradeReport = TradeReport.builder()
                .items(allItems)
                .totalBuyCost(totalBuyCost)
                .totalSellCost(totalSellCost)
                .itemTotalProfitLoss(itemTotalProfitLoss)
                .totalProfitLoss(totalProfitLoss)
                .totalProfit(totalProfit)
                .totalLoss(totalLoss)
                .totalTaxPdfo(totalTaxPdfo)
                .totalTaxVz(totalTaxVz)
                .build();

        return tradeReport;
    }

    public static List<TradeReport.Item> buildTradeReportItemList(FlexStatement flexStatement)
            throws IOException, InterruptedException {

        Map<String, List<Trade>> allTrades = flexStatement.getTrades().getTradeOrLot().stream()
                .filter(x -> x instanceof Trade)
                .map(Trade.class::cast)
                .filter(x -> x.getOpenCloseIndicator().equals("C"))
                .collect(Collectors.groupingBy(Trade::getSymbol));

        Map<String, List<Lot>> allLots = flexStatement.getTrades().getTradeOrLot().stream()
                .filter(x -> x instanceof Lot)
                .map(Lot.class::cast)
                .filter(x -> x.getOpenCloseIndicator().equals("C"))
                .collect(Collectors.groupingBy(Lot::getSymbol));

        List<TradeReport.Item> items = new ArrayList<>(allTrades.size());

        for (String symbol : allTrades.keySet()) {
            System.out.println("=================");
            System.out.println(String.format("%s", symbol));

            List<Trade> trades = allTrades.get(symbol);

            String description = trades.get(0).getDescription();
            double buyQty = 0;
            double buyCost = 0;
            double sellQty = 0;
            double sellCost = 0;

            for (Trade trade : allTrades.get(symbol)) {

                // Find lots for this trade using the 'dateTime' attribute
                List<Lot> lots = allLots.get(symbol).stream()
                        .filter(x -> x.getDateTime().equals(trade.getDateTime()))
                        .collect(Collectors.toList());

                for (Lot lot : lots) {
                    String openDateTime = lot.getOpenDateTime();
                    String openDate = openDateTime.split(";")[0];
                    double cost = lot.getCost();

                    double exchangeRate = ExchangeRates.getExchangeRate(openDate);
                    double costUah = cost * exchangeRate;

                    System.out.println(String.format("Lot: %s, %s, %s, %s",
                            lot.getQuantity(), costUah, lot.getOpenDateTime(), exchangeRate));

                    buyQty += lot.getQuantity();
                    buyCost += costUah;
                }

                String tradeDateTime = trade.getDateTime();
                String tradeDate = tradeDateTime.split(";")[0];
                double cost = trade.getNetCash();

                double exchangeRate = ExchangeRates.getExchangeRate(tradeDate);
                double costUah = cost * exchangeRate;

                System.out.println(String.format("Trade: %s, %s, %s, %s",
                        trade.getQuantity(), costUah, trade.getDateTime(), exchangeRate));

                sellQty += Math.abs(trade.getQuantity());
                sellCost += costUah;
            }

            if (sellQty != buyQty) {
                throw new RuntimeException(
                        String.format("Buy quantity and sell quantity for %s doesn't match", symbol));
            }

            double profitLoss = sellCost - buyCost;

            double taxPdfo = profitLoss > 0.0 ? (profitLoss / 100.0) * 18.0 : 0.0;
            double taxVz = profitLoss > 0.0 ? (profitLoss / 100.0) * 1.5 : 0.0;

            System.out.println(String.format("Total: %s, %s, %.2f, %.2f, %.2f, %.2f",
                    (int) buyQty, buyCost, sellCost, profitLoss, taxPdfo, taxVz));

            TradeReport.Item item = TradeReport.Item.builder()
                    .symbol(symbol)
                    .description(description)
                    .quantity((int) buyQty)
                    .buyCost(buyCost)
                    .sellCost(sellCost)
                    .profitLoss(profitLoss)
                    .taxPdfo(taxPdfo)
                    .taxVz(taxVz)
                    .build();
            items.add(item);
        }

        return items;
    }

}
