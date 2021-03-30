package net.igis.ib.taxer;

import org.apache.commons.math3.util.Precision;

public class TaxReportBuilder {

    public static TaxReport build(TradeReport tradeReport,
                                  DividendReport dividendReport,
                                  boolean dividendIncludeTaxPdfo) {

        double tradeTotalProfit = tradeReport.getTotalProfit();
        double tradeTotalTaxPdfo = tradeReport.getTotalTaxPdfo();
        double tradeTotalTaxVz = tradeReport.getTotalTaxVz();

        double dividendTotalProfit = dividendReport.getTotalAmount();
        double dividendTotalTaxPdfo = dividendIncludeTaxPdfo ? dividendReport.getTotalTaxPdfo() : 0.0;
        double dividendTotalTaxVz = dividendReport.getTotalTaxVz();

        double totalProfit = tradeTotalProfit + dividendTotalProfit;
        double totalTaxPdfo = tradeTotalTaxPdfo + dividendTotalTaxPdfo;
        double totalTaxVz = tradeTotalTaxVz + dividendTotalTaxVz;

        double r0105G3 = Precision.round(tradeTotalProfit, 2);
        double r0105G6 = Precision.round(tradeTotalTaxPdfo, 2);
        double r0105G7 = Precision.round(tradeTotalTaxVz, 2);

        String r0107G2S = "Дивіденди та проценти";
        double r0107G3 = Precision.round(dividendTotalProfit, 2);
        double r0107G6 = Precision.round(dividendTotalTaxPdfo, 2);
        double r0107G7 = Precision.round(dividendTotalTaxVz, 2);

        double r010G3 = Precision.round(totalProfit, 2);
        double r010G6 = Precision.round(totalTaxPdfo, 2);
        double r010G7 = Precision.round(totalTaxVz, 2);

        TaxReport taxReport = TaxReport.builder()
                .r010G3(r010G3)
                .r010G6(r010G6)
                .r010G7(r010G7)
                .r0105G3(r0105G3)
                .r0105G6(r0105G6)
                .r0105G7(r0105G7)
                .r0107G2S(r0107G2S)
                .r0107G3(r0107G3)
                .r0107G6(r0107G6)
                .r0107G7(r0107G7)
                .build();

        return taxReport;
    }
}
