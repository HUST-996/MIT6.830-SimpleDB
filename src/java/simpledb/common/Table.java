package simpledb.common;

import simpledb.storage.DbFile;

public class Table {

    DbFile file;

    String name;

    String pkeyField;

    public Table(DbFile file, String name, String pkeyField) {
        this.file = file;
        this.name = name;
        this.pkeyField = pkeyField;
    }

    public Table(DbFile file, String pkeyField) {
        this.file = file;
        this.pkeyField = pkeyField;
    }
}
