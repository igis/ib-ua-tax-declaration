package net.igis.ib.taxer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

@Value
@JsonDeserialize(builder = TaxDeclarationParams.TaxDeclarationParamsBuilder.class)
@Builder(builderClassName = "TaxDeclarationParamsBuilder", toBuilder = true)
public class TaxDeclarationParams {
    private TaxPayer taxPayer;
    private int regionCode;
    private int regionalUnitCode;
    private int stiCode;
    private String stiName;
    @Builder.Default
    private int periodType = 5;
    private int periodYear;
    @Builder.Default
    private int periodMonth = 12;
    @Builder.Default
    private double tradePreviousPeriodLoss = 0.0;
    @Builder.Default
    private boolean dividendIncludeTaxPdfo = false;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TaxDeclarationParamsBuilder {
    }
}
