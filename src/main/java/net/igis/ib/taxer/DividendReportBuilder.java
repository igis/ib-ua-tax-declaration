package net.igis.ib.taxer;

import com.ib.flex.ChangeInDividendAccrual;
import com.ib.flex.FlexStatement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DividendReportBuilder {

    public static DividendReport build(Collection<FlexStatement> flexStatements)
            throws IOException, InterruptedException {

        List<DividendReport.Item> allItems = new ArrayList<>();

        double totalAmount = 0;

        for (FlexStatement flexStatement : flexStatements) {
            List<DividendReport.Item> items = buildDividendReportItemList(flexStatement);

            for (DividendReport.Item item : items) {
                totalAmount += item.getAmount();

                allItems.add(item);
            }
        }

        double totalTaxPdfo = totalAmount > 0.0 ? (totalAmount / 100.0) * 18.0 : 0.0;
        double totalTaxVz = totalAmount > 0.0 ? (totalAmount / 100.0) * 1.5 : 0.0;

        System.out.println("=================");
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

    public static List<DividendReport.Item> buildDividendReportItemList(FlexStatement flexStatement)
            throws IOException, InterruptedException {

        String reportToDate = flexStatement.getToDate();

        Map<String, List<ChangeInDividendAccrual>> allPostings = flexStatement
                .getChangeInDividendAccruals().getChangeInDividendAccrual().stream()
                .filter(x -> x.getCode().equals("Po"))
                .filter(x -> !x.getPayDate().isEmpty())
                .filter(x -> Integer.parseInt(x.getPayDate()) <= Integer.parseInt(reportToDate))
                .collect(Collectors.groupingBy(ChangeInDividendAccrual::getSymbol));

        List<DividendReport.Item> items = new ArrayList<>(allPostings.size());

        for (String symbol : allPostings.keySet()) {
            System.out.println("=================");
            System.out.println(String.format("%s", symbol));

            Map<String, List<ChangeInDividendAccrual>> postingsByPayDate = allPostings.get(symbol).stream()
                    .collect(Collectors.groupingBy(ChangeInDividendAccrual::getPayDate));

            for (String date : postingsByPayDate.keySet()) {
                System.out.println(String.format("Pay date: %s", date));

                List<ChangeInDividendAccrual> postings = postingsByPayDate.get(date).stream()
                        .sorted((a, b) -> Integer.compare(Integer.parseInt(a.getReportDate()), Integer.parseInt(b.getReportDate())))
                        .collect(Collectors.toList());

/*
            for (ChangeInDividendAccrual posting : postings) {
                String reportDate = posting.getReportDate();
                String payDate = posting.getPayDate();
                double netAmount = posting.getNetAmount();

                double exchangeRate = ExchangeRates.getExchangeRate(payDate);
                double netAmountUah = netAmount * exchangeRate;

                System.out.println(String.format("Dividend posting: %s, %.2f, %s, %s, %s",
                        posting.getQuantity(), netAmountUah, reportDate, payDate, exchangeRate));
            }
*/

                ChangeInDividendAccrual posting = postings.get(postings.size() - 1);

                String payDate = posting.getPayDate();
                double netAmount = posting.getNetAmount();
                double exchangeRate = ExchangeRates.getExchangeRate(payDate);
                double netAmountUah = netAmount * exchangeRate;

                DividendReport.Item item = DividendReport.Item.builder()
                        .symbol(symbol)
                        .description(posting.getDescription())
                        .quantity(posting.getQuantity())
                        .amount(netAmountUah)
                        .build();

                System.out.println(String.format("Dividend: %s, %s, %.2f, %s, %s",
                        item.getSymbol(), posting.getQuantity(), netAmountUah, payDate, exchangeRate));

                items.add(item);
            }
        }

        return items;
    }
}
