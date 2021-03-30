package net.igis.ib.taxer;

import ua.gov.tax.F0100211.DeclarContent;

import javax.xml.bind.JAXBElement;
import java.util.UUID;

public class TaxDeclarationBuilder {

    public static TaxDeclarationDocs build(TaxFormHeader taxFormHeader,
                                           TaxReport taxReport,
                                           TradeReport tradeReport,
                                           double tradePreviousPeriodLoss) {

        String taxFormDocId = UUID.randomUUID().toString();
        String mainTaxFormDocFilename = String.format("F0100211_Zvit_%s.xml", taxFormDocId);
        String tradeTaxFormDocFilename = String.format("F0121211_DodatokF1_%s.xml", taxFormDocId);
        System.out.println(mainTaxFormDocFilename);
        System.out.println(tradeTaxFormDocFilename);

        TaxFormHeader mainTaxFormHeader = taxFormHeader.toBuilder()
                .linkedDoc(tradeTaxFormDocFilename)
                .build();
        MainTaxFormBuilder mainTaxFormBuilder = new MainTaxFormBuilder();
        JAXBElement<DeclarContent> mainTaxFormDoc = mainTaxFormBuilder.build(
                mainTaxFormHeader, taxReport);

        TaxFormHeader tradesTaxFormHeader = taxFormHeader.toBuilder()
                .linkedDoc(mainTaxFormDocFilename)
                .build();
        TradeTaxFormBuilder tradeTaxFormBuilder = new TradeTaxFormBuilder();
        JAXBElement<ua.gov.tax.F0121211.DeclarContent> tradeTaxFormDoc = tradeTaxFormBuilder.build(
                tradesTaxFormHeader, tradeReport, tradePreviousPeriodLoss);

        return TaxDeclarationDocs.builder()
                .mainFormFilename(mainTaxFormDocFilename)
                .mainFormDoc(mainTaxFormDoc)
                .tradeFormFilename(tradeTaxFormDocFilename)
                .tradeFormDoc(tradeTaxFormDoc)
                .build();
    }
}
