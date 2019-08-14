package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.ecarteira.PlanoRevendedora
import com.sysdata.ecarteira.RemessaSolicitacao
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.SolicitacaoTrocaPlano
import com.sysdata.ecarteira.StatusTrocaPlano
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationSolicitacaoTrocaPlanoService implements DataMigrationService{

    def dataMigrationUtilService

    void exportData(Integer max = null){
        dataMigrationUtilService.exportData(SolicitacaoTrocaPlano, null, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["dataSolicitacao",
        "dia",
        "mes",
        "ano",
        "revendedora.codigo",
        "planoRevendedora.nomeDoPlano",
        "planoSolicitado.nomeDoPlano",
        "statusTrocaPlano",
        "remessaSolicitacao.id"]
    }

    Map getFormatters() {
        ["dataSolicitacao":{v-> dataMigrationUtilService.formatDateDefault(v)}]
    }

    void importData(){
        dataMigrationUtilService.importData(SolicitacaoTrocaPlano) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        SolicitacaoTrocaPlano solicitacao = new SolicitacaoTrocaPlano()
        solicitacao.dataSolicitacao = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataSolicitacao'])
        solicitacao.dia = instanceMap['dia'] as Integer
        solicitacao.mes = instanceMap['mes'] as Integer
        solicitacao.ano = instanceMap['ano'] as Integer
        solicitacao.revendedora = RevendedoraCarteira.findByCodigo(instanceMap['revendedora.codigo'])
        solicitacao.planoRevendedora = PlanoRevendedora.findByNomeDoPlano(instanceMap['planoRevendedora.nomeDoPlano'])
        solicitacao.planoSolicitado = PlanoRevendedora.findByNomeDoPlano(instanceMap['planoSolicitado.nomeDoPlano'])
        solicitacao.statusTrocaPlano = StatusTrocaPlano.valueOf(instanceMap['statusTrocaPlano'])
        solicitacao.remessaSolicitacao = instanceMap['remessaSolicitacao.id'] ? RemessaSolicitacao.findByRefMigrationID(instanceMap['remessaSolicitacao.id'] as Long):null

        solicitacao.save(flush: true)

        log.debug("Remessa Solicitacao #${solicitacao.id} importado")
        log.debug(solicitacao.dump())
    }
}
