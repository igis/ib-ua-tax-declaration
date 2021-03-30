package net.igis.ib.taxer.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Decimal2ColumnAdapter extends XmlAdapter<String, BigDecimal> {

    private static final ThreadLocal<DecimalFormat> formatThreadLocal = ThreadLocal.withInitial(() -> {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        return new DecimalFormat("#0.00", otherSymbols);
    });

    @Override
    public String marshal(BigDecimal v) {
        return formatThreadLocal.get().format(v);
    }

    @Override
    public BigDecimal unmarshal(String v) {
        return BigDecimal.valueOf(Double.parseDouble(v));
    }

}