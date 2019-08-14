package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.ecarteira.HistoricoPlanoRevendedora
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationHistoricoPlanoRevendedoraService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        dataMigrationUtilService.exportData(HistoricoPlanoRevendedora, null, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["revendedora.codigo"]
    }

    Map getFormatters() {
        [:]
    }

    void importData(){
        dataMigrationUtilService.importData(HistoricoPlanoRevendedora) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        HistoricoPlanoRevendedora historico = new HistoricoPlanoRevendedora()
        historico.revendedora = RevendedoraCarteira.findByCodigo(instanceMap['revendedora.codigo'])
        historico.save(flush: true)

        log.debug("Historico Plano #${historico.id} importado")
        log.debug(historico.dump())
    }
}
