package net.igis.ib.taxer.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class Zip5Adapter extends XmlAdapter<String, BigInteger> {

    private static final ThreadLocal<DecimalFormat> formatThreadLocal = ThreadLocal.withInitial(() -> {
        return new DecimalFormat("00000");
    });

    @Override
    public String marshal(BigInteger v) {
        return formatThreadLocal.get().format(v);
    }

    @Override
    public BigInteger unmarshal(String v) {
        return BigInteger.valueOf(Integer.parseInt(v));
    }

}