package net.igis.ib.taxer;

import com.ib.flex.FlexStatement;
import com.ib.flex.Lot;
import com.ib.flex.Trade;

import java.io.IOException;
import java.util.*;

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

        System.out.println("=========================================================================");
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

        class TradeWithLots {
            Trade trade;
            List<Lot> lots = new ArrayList<>();

            public TradeWithLots(Trade trade) {
                this.trade = trade;
            }
        }

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

        List<TradeReport.Item> items = new ArrayList<>(tradesWithLots.size());

        for (Map.Entry<String, List<TradeWithLots>> trades : tradesWithLots.entrySet()) {
            String symbol = trades.getKey();
            List<TradeWithLots> tradesWithLotsForSymbol = trades.getValue();
            String description = tradesWithLotsForSymbol.get(0).trade.getDescription();

            System.out.println("=========================================================================");
            System.out.println(String.format("%s (%s)", symbol, description));

            double totalBuyQty = 0;
            double totalBuyCost = 0;
            double totalSellQty = 0;
            double totalSellCost = 0;

            for (TradeWithLots tradeWithLots : tradesWithLotsForSymbol) {
                System.out.println("-------------------------------------------------------------------------");

                Trade trade = tradeWithLots.trade;

                String tradeDateTime = trade.getDateTime();
                String tradeDate = tradeDateTime.split(";")[0];
                double sellCost = trade.getNetCash();

                double sellExchangeRate = ExchangeRates.getExchangeRate(tradeDate);
                double sellCostUah = sellCost * sellExchangeRate;

                System.out.println(String.format("Trade: %s, %s, %s, %s",
                        trade.getQuantity(), sellCostUah, trade.getDateTime(), sellExchangeRate));

                totalSellQty += Math.abs(trade.getQuantity());
                totalSellCost += sellCostUah;

                for (Lot lot : tradeWithLots.lots) {
                    String openDateTime = lot.getOpenDateTime();
                    String openDate = openDateTime.split(";")[0];
                    double buyCost = lot.getCost();

                    double buyExchangeRate = ExchangeRates.getExchangeRate(openDate);
                    double buyCostUah = buyCost * buyExchangeRate;

                    System.out.println(String.format("Lot: %s, %s, %s, %s",
                            lot.getQuantity(), buyCostUah, lot.getOpenDateTime(), buyExchangeRate));

                    totalBuyQty += lot.getQuantity();
                    totalBuyCost += buyCostUah;
                }
            }

            System.out.println("-------------------------------------------------------------------------");

            if (totalSellQty != totalBuyQty) {
                throw new RuntimeException(
                        String.format("Total buy quantity and sell quantity for %s don't match: buy %.2f, sell %.2f",
                                symbol, totalBuyQty, totalSellQty));
            }

            double profitLoss = totalSellCost - totalBuyCost;

            double taxPdfo = profitLoss > 0.0 ? (profitLoss / 100.0) * 18.0 : 0.0;
            double taxVz = profitLoss > 0.0 ? (profitLoss / 100.0) * 1.5 : 0.0;

            System.out.println(String.format("Total: %s, %s, %.2f, %.2f, %.2f, %.2f",
                    totalBuyQty, totalBuyCost, totalSellCost, profitLoss, taxPdfo, taxVz));

            TradeReport.Item item = TradeReport.Item.builder()
                    .symbol(symbol)
                    .description(description)
                    .quantity(totalBuyQty)
                    .buyCost(totalBuyCost)
                    .sellCost(totalSellCost)
                    .profitLoss(profitLoss)
                    .taxPdfo(taxPdfo)
                    .taxVz(taxVz)
                    .build();
            items.add(item);

            System.out.println();
        }

        return items;
    }

}
