package com.sysdata.ecarteira.data.migration

interface DataMigrationService {

    void exportData(Integer max)

    List<String> getFields()

    Map getFormatters()

    void importData()

    void createObject(String line)

}
