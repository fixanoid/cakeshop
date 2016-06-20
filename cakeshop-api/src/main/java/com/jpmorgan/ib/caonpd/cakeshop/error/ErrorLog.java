package com.jpmorgan.ib.caonpd.cakeshop.error;

import com.jpmorgan.ib.caonpd.cakeshop.util.StringUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public class ErrorLog {

    public static final FastDateFormat tsFormatter = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss,S");

    public long ts;

    public Object err;

    public ErrorLog(Object err) {
        this.ts = System.nanoTime();
        this.err = err;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("[" + tsFormatter.format(ts/1000) + "] ");
        if (err instanceof String) {
            out.append(err);
        } else if (err instanceof Throwable) {
            out.append(ExceptionUtils.getStackTrace((Throwable) err));
        } else {
            out.append(StringUtils.toString(err));
        }
        return out.toString();
    }

}