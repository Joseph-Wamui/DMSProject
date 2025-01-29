package com.emt.dms1.customReports;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomJRDataSource implements JRDataSource {
    private Iterator<Map<String, Object>> iterator;
    private Map<String, Object> currentRow;

    public CustomJRDataSource(List<Map<String, Object>> data) {
        this.iterator = data.iterator();
    }

    @Override
    public boolean next() throws JRException {
        if (iterator.hasNext()) {
            currentRow = iterator.next();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        return currentRow.get(jrField.getName());
    }
}