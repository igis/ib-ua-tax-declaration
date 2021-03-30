package net.igis.ib.taxer;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TaxFormHeader {
    TaxDeclarationParams commonParams;
    @NonNull
    String fillDate;
    String linkedDoc;
}
