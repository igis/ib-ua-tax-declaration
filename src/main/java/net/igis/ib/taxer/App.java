package net.igis.ib.taxer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ib.flex.FlexQueryResponse;
import com.ib.flex.FlexStatement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class App {

    @Parameter(names={"--flex-report", "-f"}, required = true)
    private List<String> flexReportFiles;

    @Parameter(names={"--params", "-p"}, required = true)
    private String paramsFile;

    @Parameter(names={"--output-dir", "-o"}, required = true)
    private String outputDir;

    public static void main(String... args) throws Exception {
        App app = new App();
        JCommander.newBuilder()
                .addObject(app)
                .build()
                .parse(args);
        app.run();
    }

    public void run() throws Exception {
        File paramsFile = new File(this.paramsFile);
        ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
        TaxDeclarationParams taxDeclarationParams = yamlObjectMapper.readValue(paramsFile, TaxDeclarationParams.class);

        JAXBContext jaxbContext = JAXBContext.newInstance(FlexQueryResponse.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        List<FlexQueryResponse> flexReports = new ArrayList<>(flexReportFiles.size());
        for (String flexReportPath : flexReportFiles) {
            System.out.println(String.format("Loading flex report %s", flexReportPath));

            FlexQueryResponse response = (FlexQueryResponse) unmarshaller.unmarshal(new File(flexReportPath));
            flexReports.add(response);
        }

        List<FlexStatement> flexStatements = flexReports.stream()
                .map(x -> x.getFlexStatements().getFlexStatement())
                .collect(Collectors.toList());

        System.out.println("\nPreparing trades report...");
        TradeReport tradeReport = TradeReportBuilder.build(
                flexStatements,
                taxDeclarationParams.getTradePreviousPeriodLoss());
        System.out.println(tradeReport);

        System.out.println("\nPreparing dividends report...");
        DividendReport dividendReport = DividendReportBuilder.build(flexStatements);
        System.out.println(dividendReport);

        TaxReport taxReport = TaxReportBuilder.build(
                tradeReport, dividendReport, taxDeclarationParams.isDividendIncludeTaxPdfo());

        TaxFormHeader taxFormHeader = TaxFormHeader.builder()
                .commonParams(taxDeclarationParams)
                .fillDate(DateTimeFormatter.ofPattern("ddMMyyyy").format(LocalDate.now()))
                .build();

        TaxDeclarationDocs taxDeclarationDocs = TaxDeclarationBuilder.build(
                taxFormHeader, taxReport, tradeReport, taxDeclarationParams.getTradePreviousPeriodLoss());

        System.out.println(String.format("\nWriting tax declaration files to %s", outputDir));
        File outputDirFile = new File(outputDir);
        TaxDeclarationWriter writer = new TaxDeclarationWriter(outputDirFile);
        writer.write(taxDeclarationDocs);
    }

}
