package net.igis.ib.taxer;

import ua.gov.tax.F0100211.DeclarContent;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class TaxDeclarationWriter {

    private File outputDir;

    public TaxDeclarationWriter(File outputDir) {
        this.outputDir = outputDir;
    }

    public void write(TaxDeclarationDocs taxDeclarationDocs) throws JAXBException, IOException {
        writeMainTaxFormDoc(taxDeclarationDocs.getMainFormDoc(), taxDeclarationDocs.getMainFormFilename());
        writeTradeTaxFormDoc(taxDeclarationDocs.getTradeFormDoc(), taxDeclarationDocs.getTradeFormFilename());
    }

    private void writeMainTaxFormDoc(JAXBElement<DeclarContent> taxFormDoc, String filename)
            throws JAXBException, IOException {

        writeTaxFormDoc(taxFormDoc, ua.gov.tax.F0100211.DeclarContent.class, "F0100211.xsd", filename);
    }

    private void writeTradeTaxFormDoc(JAXBElement<ua.gov.tax.F0121211.DeclarContent> taxFormDoc, String filename)
            throws JAXBException, IOException {

        writeTaxFormDoc(taxFormDoc, ua.gov.tax.F0121211.DeclarContent.class, "F0121211.xsd", filename);
    }

    private <T> void writeTaxFormDoc(JAXBElement<T> taxFormDoc, Class<T> clazz,
                                            String noNamespaceLocation, String filename)
            throws JAXBException, IOException {

        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "windows-1251");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, noNamespaceLocation);

        marshaller.marshal(taxFormDoc, new OutputStreamWriter(System.out, Charset.forName("utf8")));

        File file = new File(outputDir, String.format("%s.xml", filename));
        file.getParentFile().mkdirs();

        marshaller.marshal(taxFormDoc, new OutputStreamWriter(new FileOutputStream(file), Charset.forName("cp1251")));
    }
}
