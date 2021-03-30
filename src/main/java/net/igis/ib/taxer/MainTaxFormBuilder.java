package net.igis.ib.taxer;

import ua.gov.tax.F0100211.*;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MainTaxFormBuilder {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public JAXBElement<DeclarContent> build(TaxFormHeader formHeader, TaxReport taxReport) {

        TaxDeclarationParams commonParams = formHeader.getCommonParams();
        TaxPayer taxPayer = commonParams.getTaxPayer();

        DHead head = objectFactory.createDHead();
        head.setCDOC("F01");
        head.setCDOCSUB("002");
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
            doc.setTYPE(BigInteger.valueOf(2));
            doc.setCDOC("F01");
            doc.setCDOCSUB("212");
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
        body.setHNAME(taxPayer.getFullName());
        body.setHREG(objectFactory.createDBodyHREG(taxPayer.getRegion()));
        body.setHRAJ(objectFactory.createDBodyHRAJ(taxPayer.getRegionalUnit()));
        body.setHCITY(taxPayer.getCityTown());
        body.setHSTREET(taxPayer.getStreet());
        body.setHBUILD(objectFactory.createDBodyHBUILD(taxPayer.getBuilding()));
        if (taxPayer.getCorpus() != null) {
            body.setHCORP(objectFactory.createDBodyHCORP(taxPayer.getCorpus()));
        }
        body.setHAPT(objectFactory.createDBodyHAPT(taxPayer.getApartment()));
        body.setHZIP(objectFactory.createDBodyHZIP(new BigInteger(taxPayer.getZip())));
        body.setHTEL(objectFactory.createDBodyHTEL(taxPayer.getPhone()));
        body.setHEMAIL(objectFactory.createDBodyHEMAIL(taxPayer.getEmail()));
        body.setHBOS(taxPayer.getSigName());

        body.setH01(1);
        body.setH03(1);
        body.setH05(1);
        body.setHZ(1);
        body.setHZY(commonParams.getPeriodYear());
        body.setHSTI(commonParams.getStiName());
        body.setHFILL(formHeader.getFillDate());
        body.setHD1(objectFactory.createDBodyHD1(1));

        body.setR010G3(objectFactory.createDBodyR010G3(BigDecimal.valueOf(taxReport.getR010G3())));
        body.setR010G6(objectFactory.createDBodyR010G6(BigDecimal.valueOf(taxReport.getR010G6())));
        body.setR010G7(objectFactory.createDBodyR010G7(BigDecimal.valueOf(taxReport.getR010G7())));

        body.setR0105G3(objectFactory.createDBodyR0105G3(BigDecimal.valueOf(taxReport.getR0105G3())));
        body.setR0105G6(objectFactory.createDBodyR0105G6(BigDecimal.valueOf(taxReport.getR0105G6())));
        body.setR0105G7(objectFactory.createDBodyR0105G7(BigDecimal.valueOf(taxReport.getR0105G7())));

        if (taxReport.getR0107G3() != 0.0) {
            body.setR0107G2S(objectFactory.createDBodyR0107G2S(taxReport.getR0107G2S()));
            body.setR0107G3(objectFactory.createDBodyR0107G3(BigDecimal.valueOf(taxReport.getR0107G3())));
            body.setR0107G6(objectFactory.createDBodyR0107G6(BigDecimal.valueOf(taxReport.getR0107G6())));
            body.setR0107G7(objectFactory.createDBodyR0107G7(BigDecimal.valueOf(taxReport.getR0107G7())));
        }

        DeclarContent declarContent = objectFactory.createDeclarContent();
        declarContent.setDECLARHEAD(head);
        declarContent.setDECLARBODY(body);

        JAXBElement<DeclarContent> declar = objectFactory.createDECLAR(declarContent);
        return declar;
    }
}
