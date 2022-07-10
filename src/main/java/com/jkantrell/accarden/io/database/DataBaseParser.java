package com.jkantrell.accarden.io.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DataBaseParser<T extends Enitty> {

    T toEntity(ResultSet src) throws SQLException;

    DataBaseRow toRow(T src);

}
