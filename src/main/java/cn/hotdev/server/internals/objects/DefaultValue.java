package cn.hotdev.server.internals.objects;

import cn.hotdev.server.internals.enums.DataType;

/**
 * Created by andy on 5/13/15.
 */
public class DefaultValue {

    private DataType dataType;
    private String value;

    public DefaultValue(String value) {
        dataType = DataType.STRING;
        this.value = value;
    }

    public DefaultValue(int value) {
        dataType = DataType.INT;
        this.value = String.valueOf(value);
    }

    public DefaultValue(long value) {
        dataType = DataType.LONG;
        this.value = String.valueOf(value);
    }

    public DefaultValue(boolean value) {
        dataType = DataType.BOOL;
        this.value = String.valueOf(value);
    }

    public String getValue() {
        return value;
    }

    public DataType getDataType() {
        return dataType;
    }
}
