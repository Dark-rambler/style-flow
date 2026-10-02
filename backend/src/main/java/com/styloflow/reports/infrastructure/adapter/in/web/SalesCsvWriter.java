package com.styloflow.reports.infrastructure.adapter.in.web;

import com.styloflow.reports.domain.model.ExportedSale;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/** ';'-separated CSV with a BOM (Excel friendly in Spanish locales). */
@Component
public class SalesCsvWriter {

    private final DateTimeFormatter dateFormat;

    public SalesCsvWriter(ZoneId zone) {
        this.dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone);
    }

    public void header(PrintWriter out) {
        out.print('﻿'); // BOM so Excel detects UTF-8
        out.println("id;date;cashier;customer;subtotal;discount;total;tax;payment_method;status");
    }

    public void row(PrintWriter out, ExportedSale sale) {
        out.println(String.join(";",
                String.valueOf(sale.id()),
                dateFormat.format(sale.date()),
                csv(sale.cashier()),
                csv(sale.customer()),
                sale.subtotal().toPlainString(),
                sale.discount().toPlainString(),
                sale.total().toPlainString(),
                sale.tax().toPlainString(),
                sale.paymentMethod().name(),
                sale.status().name()));
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(";") || escaped.contains("\"") ? "\"" + escaped + "\"" : escaped;
    }
}
