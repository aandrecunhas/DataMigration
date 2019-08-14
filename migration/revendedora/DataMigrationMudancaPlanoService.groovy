package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.ecarteira.MudancaRevendedoraPlano
import com.sysdata.ecarteira.PlanoRevendedora
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.data.migration.DataMigrationService
import com.sysdata.security.User

class DataMigrationMudancaPlanoService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        dataMigrationUtilService.exportData(MudancaRevendedoraPlano, null, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["userLogado.username",
         "planoAntes.nomeDoPlano",
         "planoDepois.nomeDoPlano",
         "data",
         "historico.revendedora.codigo"]
    }

    Map getFormatters() {
        ['data':{v-> dataMigrationUtilService.formatDateDefault(v)}]
    }

    void importData(){
        dataMigrationUtilService.importData(MudancaRevendedoraPlano) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")



        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        RevendedoraCarteira revendedora = RevendedoraCarteira.findByCodigo(instanceMap['historico.revendedora.codigo'])
        MudancaRevendedoraPlano mudanca = new MudancaRevendedoraPlano()
        mudanca.historico = revendedora.historicoPlanoRevendedora
        mudanca.planoDepois = PlanoRevendedora.findByNomeDoPlano(instanceMap['planoDepois.nomeDoPlano'])
        mudanca.planoAntes = PlanoRevendedora.findByNomeDoPlano(instanceMap['planoAntes.nomeDoPlano'])
        mudanca.userLogado = User.findByUsername(instanceMap['userLogado.username'])
        mudanca.data = Date.parse("dd/MM/yyy HH:mm", instanceMap['data'])
        mudanca.save(flush: true)

        log.debug("Mudanca Plano #${mudanca.id} importado")
        log.debug(mudanca.dump())
    }
}
