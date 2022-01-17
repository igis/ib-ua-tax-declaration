package net.igis.ib.taxer;

import com.ib.flex.ChangeInDividendAccrual;
import com.ib.flex.FlexStatement;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DividendReportBuilder {

    public static DividendReport build(Collection<FlexStatement> flexStatements)
            throws IOException, InterruptedException {

        String reportToDateLatest = null;
        Map<String, List<ChangeInDividendAccrual>> allPayouts = new HashMap<>();

        for (FlexStatement flexStatement : flexStatements) {
            String reportToDate = flexStatement.getToDate();
            if (reportToDateLatest == null || Integer.parseInt(reportToDate) > Integer.parseInt(reportToDateLatest)) {
                reportToDateLatest = reportToDate;
            }

            Map<String, List<ChangeInDividendAccrual>> payouts = collectChanges(flexStatement);
            for (String symbol : payouts.keySet()) {
                List<ChangeInDividendAccrual> payoutsForSymbol = allPayouts.get(symbol);
                if (payoutsForSymbol == null) {
                    payoutsForSymbol = new ArrayList<>();
                    allPayouts.put(symbol, payoutsForSymbol);
                }
                payoutsForSymbol.addAll(payouts.get(symbol));
            }
        }

        List<DividendReport.Item> allItems = new ArrayList<>();

        double totalAmount = 0;

        List<DividendReport.Item> items = buildDividendReportItemList(allPayouts, reportToDateLatest);

        for (DividendReport.Item item : items) {
            totalAmount += item.getAmount();

            allItems.add(item);
        }

        double totalTaxPdfo = totalAmount > 0.0 ? (totalAmount / 100.0) * 18.0 : 0.0;
        double totalTaxVz = totalAmount > 0.0 ? (totalAmount / 100.0) * 1.5 : 0.0;

        System.out.println("=========================================================================");
        System.out.println(String.format("Total: %.2f, %.2f, %.2f",
                totalAmount, totalTaxPdfo, totalTaxVz));

        DividendReport report = DividendReport.builder()
                .items(allItems)
                .totalAmount(totalAmount)
                .totalTaxPdfo(totalTaxPdfo)
                .totalTaxVz(totalTaxVz)
                .build();

        return report;
    }

    private static Map<String, List<ChangeInDividendAccrual>> collectChanges(
            FlexStatement flexStatement) {

        Map<String, List<ChangeInDividendAccrual>> allPayouts = flexStatement
                .getChangeInDividendAccruals().getChangeInDividendAccrual().stream()
                .filter(x -> !x.getPayDate().isEmpty())
                .collect(Collectors.groupingBy(ChangeInDividendAccrual::getSymbol));

        return allPayouts;
    }

    private static List<DividendReport.Item> buildDividendReportItemList(
            Map<String, List<ChangeInDividendAccrual>> allPayouts, String reportToDate)
            throws IOException, InterruptedException {

        List<DividendReport.Item> items = new ArrayList<>(allPayouts.size());

        for (String symbol : allPayouts.keySet()) {
            System.out.println("=========================================================================");
            System.out.println(String.format("%s", symbol));

            Map<String, List<ChangeInDividendAccrual>> payoutsByPayDate = allPayouts.get(symbol).stream()
                    .filter(x -> x.getCode().equals("Po"))
                    .filter(x -> Integer.parseInt(x.getPayDate()) <= Integer.parseInt(reportToDate))
                    .collect(Collectors.groupingBy(ChangeInDividendAccrual::getPayDate));

            List<String> payDates = payoutsByPayDate.keySet().stream()
                    .sorted(Comparator.comparingInt(Integer::parseInt))
                    .collect(Collectors.toList());

            for (String date : payDates) {
                System.out.println("-------------------------------------------------------------------------");
                System.out.println(String.format("Pay date: %s", date));

                List<ChangeInDividendAccrual> payouts = payoutsByPayDate.get(date).stream()
                        .sorted(Comparator.comparingInt(a -> Integer.parseInt(a.getReportDate())))
                        .collect(Collectors.toList());

                ChangeInDividendAccrual payout = payouts.get(payouts.size() - 1);

                String payDate = payout.getPayDate();
                double netAmount = Math.abs(payout.getNetAmount());
                double exchangeRate = ExchangeRates.getExchangeRate(payDate);
                double netAmountUah = netAmount * exchangeRate;

                DividendReport.Item item = DividendReport.Item.builder()
                        .symbol(symbol)
                        .description(payout.getDescription())
                        .quantity(payout.getQuantity())
                        .amount(netAmountUah)
                        .build();

                System.out.println(String.format("Dividend: %s, %s, %.2f, %s, %s",
                        item.getSymbol(), payout.getQuantity(), netAmountUah, payDate, exchangeRate));

                items.add(item);
            }

            System.out.println();
        }

        return items;
    }
}
