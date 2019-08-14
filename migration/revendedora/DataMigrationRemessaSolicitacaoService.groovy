package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.ecarteira.RemessaSolicitacao
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationRemessaSolicitacaoService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        dataMigrationUtilService.exportData(RemessaSolicitacao, null, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["id"]
    }

    Map getFormatters() {
        [:]
    }

    void importData(){
        dataMigrationUtilService.importData(RemessaSolicitacao) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        RemessaSolicitacao remessa = new RemessaSolicitacao()
        remessa.refMigrationID = instanceMap['id'] as Long
        remessa.save(flush: true)

        log.debug("Remessa Solicitacao #${remessa.id} importado")
        log.debug(remessa.dump())
    }
}
