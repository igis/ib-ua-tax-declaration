package net.igis.ib.taxer;

import lombok.Builder;
import lombok.Value;

import javax.xml.bind.JAXBElement;

@Value
@Builder(toBuilder = true)
public class TaxDeclarationDocs {

    String mainFormFilename;
    JAXBElement<ua.gov.tax.F0100211.DeclarContent> mainFormDoc;

    String tradeFormFilename;
    JAXBElement<ua.gov.tax.F0121211.DeclarContent> tradeFormDoc;
}
