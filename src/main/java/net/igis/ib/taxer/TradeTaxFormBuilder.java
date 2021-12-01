package net.igis.ib.taxer;

import org.apache.commons.math3.util.Precision;
import ua.gov.tax.F0121211.*;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TradeTaxFormBuilder {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public JAXBElement<DeclarContent> build(TaxFormHeader formHeader, TradeReport tradeReport,
                                            double previousPeriodLoss) {

        TaxDeclarationParams commonParams = formHeader.getCommonParams();
        TaxPayer taxPayer = commonParams.getTaxPayer();

        DHead head = objectFactory.createDHead();
        head.setCDOC("F01");
        head.setCDOCSUB("212");
        head.setCDOCVER("11");
        head.setCDOCTYPE(BigInteger.valueOf(1));
        head.setCDOCCNT(BigInteger.valueOf(1));
        head.setCDOCSTAN(1);
        head.setDFILL(formHeader.getFillDate());

        head.setCREG(commonParams.getRegionCode());
        head.setCRAJ(commonParams.getRegionalUnitCode());
        head.setPERIODMONTH(commonParams.getPeriodMonth());
        head.setPERIODTYPE(commonParams.getPeriodType());
        head.setPERIODYEAR(commonParams.getPeriodYear());
        head.setCSTIORIG(BigInteger.valueOf(commonParams.getStiCode()));

        if (formHeader.getLinkedDoc() != null) {
            DHead.LINKEDDOCS linkedDocs = objectFactory.createDHeadLINKEDDOCS();
            DHead.LINKEDDOCS.DOC doc = objectFactory.createDHeadLINKEDDOCSDOC();

            doc.setNUM(BigInteger.valueOf(1));
            doc.setTYPE(BigInteger.valueOf(1));
            doc.setCDOC("F01");
            doc.setCDOCSUB("002");
            doc.setCDOCVER("11");
            doc.setCDOCTYPE(BigInteger.valueOf(0));
            doc.setCDOCCNT(BigInteger.valueOf(1));
            doc.setCDOCSTAN(1);
            doc.setFILENAME(formHeader.getLinkedDoc());

            linkedDocs.getDOC().add(doc);
            head.setLINKEDDOCS(objectFactory.createDHeadLINKEDDOCS(linkedDocs));
        }

        DBody body = objectFactory.createDBody();

        body.setHTIN(taxPayer.getTin());
        body.setHBOS(taxPayer.getSigName());

        body.setHZ(1);
        body.setHZY(commonParams.getPeriodYear());

        int rowNum = 1;
        for (TradeReport.Item trade : tradeReport.getItems()) {
            IntColumn g2 = objectFactory.createIntColumn();
            g2.setROWNUM(rowNum);
            g2.setValue(BigInteger.valueOf(2));

            StrColumn g3s = objectFactory.createStrColumn();
            g3s.setROWNUM(rowNum);
            if ((trade.getQuantity() % 1) == 0) {
                g3s.setValue(String.format("%s %.0fшт.", trade.getSymbol(), trade.getQuantity()));
            } else {
                // Quantity is fractional
                g3s.setValue(String.format("%s %.1fшт.", trade.getSymbol(), trade.getQuantity()));
            }

            Decimal2Column g4 = objectFactory.createDecimal2Column();
            g4.setROWNUM(rowNum);
            g4.setValue(BigDecimal.valueOf(Precision.round(trade.getSellCost(), 2)));

            Decimal2Column g5 = objectFactory.createDecimal2Column();
            g5.setROWNUM(rowNum);
            g5.setValue(BigDecimal.valueOf(Precision.round(trade.getBuyCost(), 2)));

            Decimal2Column g6 = objectFactory.createDecimal2Column();
            g6.setROWNUM(rowNum);
            g6.setValue(BigDecimal.valueOf(Precision.round(trade.getProfitLoss(), 2)));

            body.getT1RXXXXG2().add(g2);
            body.getT1RXXXXG3S().add(g3s);
            body.getT1RXXXXG4().add(g4);
            body.getT1RXXXXG5().add(g5);
            body.getT1RXXXXG6().add(g6);

            rowNum++;
        }

        // Total trade results
        body.setR001G4(objectFactory.createDBodyR001G4(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalSellCost(), 2))));
        body.setR001G5(objectFactory.createDBodyR001G5(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalBuyCost(), 2))));
        body.setR001G6(objectFactory.createDBodyR001G6(
                BigDecimal.valueOf(Precision.round(tradeReport.getItemTotalProfitLoss(), 2))));

        // Loss in previous report period (year)
        if (previousPeriodLoss != 0.0) {
            body.setR002G6(objectFactory.createDBodyR002G6(
                    BigDecimal.valueOf(Precision.round(previousPeriodLoss, 2))));
        }

        // Profit/Loss in current report period
        body.setR003G6(objectFactory.createDBodyR003G6(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalProfitLoss(), 2))));

        // Net Profit in current report period
        body.setR031G6(objectFactory.createDBodyR031G6(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalProfit(), 2))));

        // Loss in current report period
        if (tradeReport.getTotalLoss() != 0.0) {
            body.setR032G6(objectFactory.createDBodyR032G6(
                    BigDecimal.valueOf(Precision.round(Math.abs(tradeReport.getTotalLoss()), 2))));
        }

        // PDFO
        body.setR004G6(objectFactory.createDBodyR004G6(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalTaxPdfo(), 2))));
        body.setR042G6(objectFactory.createDBodyR042G6(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalTaxPdfo(), 2))));

        // VZ
        body.setR005G6(objectFactory.createDBodyR005G6(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalTaxVz(), 2))));
        body.setR052G6(objectFactory.createDBodyR052G6(
                BigDecimal.valueOf(Precision.round(tradeReport.getTotalTaxVz(), 2))));

        DeclarContent declarContent = objectFactory.createDeclarContent();
        declarContent.setDECLARHEAD(head);
        declarContent.setDECLARBODY(body);

        JAXBElement<DeclarContent> declar = objectFactory.createDECLAR(declarContent);
        return declar;
    }
}
