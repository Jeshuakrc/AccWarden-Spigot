package com.jkantrell.accarden.io.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DataBaseRow {

    //FIELDS
    private final String idName_;
    private final Object idVal_;
    private final Map<String, Object> vals_ = new HashMap<>();

    //CONSTRUCTORS
    public DataBaseRow(String idName, Object idValue) {
        this.idName_ = idName;
        this.idVal_ = idValue;
    }

    //GETTERS
    public String getIdName() {
        return this.idName_;
    }
    public Object getIdValue() {
        return this.idVal_;
    }
    public Map<String,Object> getValues() {
        return Map.copyOf(this.vals_);
    }

    //METHODS
    public void addColumn(String name, Object value) {
        this.vals_.put(name,value);
    }

}
