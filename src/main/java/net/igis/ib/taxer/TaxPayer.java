package net.igis.ib.taxer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@JsonDeserialize(builder = TaxPayer.TaxPayerBuilder.class)
@Builder(builderClassName = "TaxPayerBuilder", toBuilder = true)
public class TaxPayer {
    private String tin;
    private String fullName;
    private String sigName;
    private String region;
    private String regionalUnit;
    private String cityTown;
    private String street;
    private String building;
    private String corpus;
    private String apartment;
    private String zip;
    private String phone;
    private String email;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TaxPayerBuilder {
    }
}
