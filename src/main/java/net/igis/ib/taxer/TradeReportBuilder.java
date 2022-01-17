package net.igis.ib.taxer;

import com.ib.flex.FlexStatement;
import com.ib.flex.Lot;
import com.ib.flex.Trade;

import java.io.IOException;
import java.util.*;

public class TradeReportBuilder {

    public static TradeReport build(Collection<FlexStatement> flexStatements, double previousPeriodLoss)
            throws IOException, InterruptedException {

        Map<String, List<TradeWithLots>> allTradesWithLots = new HashMap<>();

        for (FlexStatement flexStatement : flexStatements) {
            Map<String, List<TradeWithLots>> tradesWithLots = collectTradesWithLots(flexStatement);
            for (String symbol : tradesWithLots.keySet()) {
                List<TradeWithLots> tradesWithLotsForSymbol = allTradesWithLots.get(symbol);
                if (tradesWithLotsForSymbol == null) {
                    tradesWithLotsForSymbol = new ArrayList<>();
                    allTradesWithLots.put(symbol, tradesWithLotsForSymbol);
                }
                tradesWithLotsForSymbol.addAll(tradesWithLots.get(symbol));
            }
        }

        double totalBuyCost = 0;
        double totalBuyCostUah = 0;
        double totalSellCost = 0;
        double totalSellCostUah = 0;

        List<TradeReport.Item> allItems = buildTradeReportItemList(allTradesWithLots);

        for (TradeReport.Item item : allItems) {
            totalBuyCost += item.getBuyCostUsd();
            totalBuyCostUah += item.getBuyCost();

            totalSellCost += item.getSellCostUsd();
            totalSellCostUah += item.getSellCost();
        }

        double itemTotalProfitLoss = totalSellCost - totalBuyCost;
        double itemTotalProfitLossUah = totalSellCostUah - totalBuyCostUah;
        double totalProfitLossUah = itemTotalProfitLossUah - previousPeriodLoss;
        double totalTaxPdfo = totalProfitLossUah > 0.0 ? (totalProfitLossUah / 100.0) * 18.0 : 0.0;
        double totalTaxVz = totalProfitLossUah > 0.0 ? (totalProfitLossUah / 100.0) * 1.5 : 0.0;

        double totalProfitUah = totalProfitLossUah > 0.0 ? totalProfitLossUah : 0;
        double totalLossUah = totalProfitLossUah < 0.0 ? Math.abs(totalProfitLossUah) : 0;

        System.out.println("=========================================================================");
        System.out.println(String.format("Total: %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f",
                totalBuyCostUah, totalSellCostUah, itemTotalProfitLoss, totalProfitLossUah,
                totalProfitUah, totalLossUah, totalTaxPdfo, totalTaxVz));

        TradeReport tradeReport = TradeReport.builder()
                .items(allItems)
                .totalBuyCost(totalBuyCostUah)
                .totalBuyCostUsd(totalBuyCost)
                .totalSellCost(totalSellCostUah)
                .totalSellCostUsd(totalSellCost)
                .itemTotalProfitLoss(itemTotalProfitLossUah)
                .itemTotalProfitLossUsd(itemTotalProfitLoss)
                .totalProfitLoss(totalProfitLossUah)
                .totalProfit(totalProfitUah)
                .totalLoss(totalLossUah)
                .totalTaxPdfo(totalTaxPdfo)
                .totalTaxVz(totalTaxVz)
                .build();

        return tradeReport;
    }

    private static Map<String, List<TradeWithLots>> collectTradesWithLots(FlexStatement flexStatement) {

        Map<String, List<TradeWithLots>> tradesWithLots = new HashMap<>();
        TradeWithLots currentTradeWithLots = null;
        for (Object tradeOrLot : flexStatement.getTrades().getTradeOrLot()) {
            if (tradeOrLot instanceof Trade) {
                Trade trade = (Trade) tradeOrLot;
                if (trade.getOpenCloseIndicator().equals("C")) {
                    currentTradeWithLots = new TradeWithLots(trade);
                    List<TradeWithLots> tradesWithLotsForSymbol = tradesWithLots.get(trade.getSymbol());
                    if (tradesWithLotsForSymbol == null) {
                        tradesWithLotsForSymbol = new ArrayList<>();
                        tradesWithLots.put(trade.getSymbol(), tradesWithLotsForSymbol);
                    }
                    tradesWithLotsForSymbol.add(currentTradeWithLots);
                }
            } else if (tradeOrLot instanceof Lot) {
                Lot lot = (Lot) tradeOrLot;
                if (lot.getOpenCloseIndicator().equals("C")) {
                    currentTradeWithLots.lots.add(lot);
                }
            }
        }

        return tradesWithLots;
    }

    private static List<TradeReport.Item> buildTradeReportItemList(Map<String, List<TradeWithLots>> tradesWithLots)
            throws IOException, InterruptedException {

        List<TradeReport.Item> items = new ArrayList<>(tradesWithLots.size());

        for (Map.Entry<String, List<TradeWithLots>> trades : tradesWithLots.entrySet()) {
            String symbol = trades.getKey();
            List<TradeWithLots> tradesWithLotsForSymbol = trades.getValue();
            String description = tradesWithLotsForSymbol.get(0).trade.getDescription();

            System.out.println("=========================================================================");
            System.out.println(String.format("%s (%s)", symbol, description));

            double totalBuyQty = 0;
            double totalBuyCost = 0;
            double totalBuyCostUah = 0;
            double totalSellQty = 0;
            double totalSellCost = 0;
            double totalSellCostUah = 0;

            for (TradeWithLots tradeWithLots : tradesWithLotsForSymbol) {
                System.out.println("-------------------------------------------------------------------------");

                Trade trade = tradeWithLots.trade;

                String tradeDateTime = trade.getDateTime();
                String tradeDate = tradeDateTime.split(";")[0];
                double sellCost = trade.getNetCash();

                double sellExchangeRate = ExchangeRates.getExchangeRate(tradeDate);
                double sellCostUah = sellCost * sellExchangeRate;

                System.out.println(String.format("Trade: %s, %.2f (%.2f), %s, %s",
                        trade.getQuantity(), sellCostUah, sellCost, trade.getDateTime(), sellExchangeRate));

                totalSellQty += Math.abs(trade.getQuantity());
                totalSellCost += sellCost;
                totalSellCostUah += sellCostUah;

                for (Lot lot : tradeWithLots.lots) {
                    String openDateTime = lot.getOpenDateTime();
                    String openDate = openDateTime.split(";")[0];
                    double buyCost = lot.getCost();

                    double buyExchangeRate = ExchangeRates.getExchangeRate(openDate);
                    double buyCostUah = buyCost * buyExchangeRate;

                    System.out.println(String.format("Lot: %s, %.2f (%.2f), %s, %s",
                            lot.getQuantity(), buyCostUah, buyCost, lot.getOpenDateTime(), buyExchangeRate));

                    totalBuyQty += lot.getQuantity();
                    totalBuyCost += buyCost;
                    totalBuyCostUah += buyCostUah;
                }
            }

            System.out.println("-------------------------------------------------------------------------");

            if (totalSellQty != totalBuyQty) {
                throw new RuntimeException(
                        String.format("Total buy quantity and sell quantity for %s don't match: buy %.2f, sell %.2f",
                                symbol, totalBuyQty, totalSellQty));
            }

            double profitLoss = totalSellCost - totalBuyCost;
            double profitLossUah = totalSellCostUah - totalBuyCostUah;

            double taxPdfo = profitLossUah > 0.0 ? (profitLossUah / 100.0) * 18.0 : 0.0;
            double taxVz = profitLossUah > 0.0 ? (profitLossUah / 100.0) * 1.5 : 0.0;

            System.out.println(String.format("Total: %s, %.2f, %.2f, %.2f (%.2f), %.2f, %.2f",
                    totalBuyQty, totalBuyCostUah, totalSellCostUah, profitLossUah, profitLoss, taxPdfo, taxVz));

            TradeReport.Item item = TradeReport.Item.builder()
                    .symbol(symbol)
                    .description(description)
                    .quantity(totalBuyQty)
                    .buyCost(totalBuyCostUah)
                    .buyCostUsd(totalBuyCost)
                    .sellCost(totalSellCostUah)
                    .sellCostUsd(totalSellCost)
                    .profitLoss(profitLossUah)
                    .profitLossUsd(profitLoss)
                    .taxPdfo(taxPdfo)
                    .taxVz(taxVz)
                    .build();
            items.add(item);

            System.out.println();
        }

        return items;
    }

    static class TradeWithLots {
        Trade trade;
        List<Lot> lots = new ArrayList<>();

        public TradeWithLots(Trade trade) {
            this.trade = trade;
        }
    }

}
