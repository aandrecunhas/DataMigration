package com.sysdata.ecarteira.data.migration.transacao

import com.sysdata.coadquirencia.Adquirente
import com.sysdata.coadquirencia.Bandeira
import com.sysdata.coadquirencia.OrigemTransacao
import com.sysdata.coadquirencia.SituacaoConciliacao
import com.sysdata.coadquirencia.StatusAgendamento
import com.sysdata.coadquirencia.StatusCancelamento
import com.sysdata.coadquirencia.StatusConciliacao
import com.sysdata.coadquirencia.StatusConsolidacao
import com.sysdata.coadquirencia.StatusTransacao
import com.sysdata.coadquirencia.StatusTransacaoAdquirente
import com.sysdata.coadquirencia.StatusValidacao
import com.sysdata.coadquirencia.TipoEquipamento
import com.sysdata.coadquirencia.TipoTransacao
import com.sysdata.coadquirencia.Transacao
import com.sysdata.ecarteira.PlanoRevendedora
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationTransacaoService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            merchant {
                eq('class', 'com.sysdata.ecarteira.RevendedoraCarteira')
            }
        }
        dataMigrationUtilService.exportData(Transacao, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){

        ["data",
         "dataHora",
         "dataConciliacaoTransacao",
         "valor",
         "terminal",
         "estabelecimento",
         "estabelecimentoAdquirente",
         "tipo",
         "totalParcelas",
        "merchant.codigo",
        "statusAgendamento",
        "statusAgendamentoAdquirente",
        "statusConsolidacao",
        "cartao",
        "nsuHost",
        "nsuSitef",
        "codigoAutorizacao",
        "origemTransacao",
         "bandeira.codigo",
        "status",
        "statusAdquirente",
        "situacaoConciliacao.nome",
        "statusConciliacao.nome",
        "statusValidacao",
        "statusValidacaoData",
        "taxaAdmPagar",
        "valorPagar",
        "valorFranquiaReceber",
        "valorCanceladoSolicitado",
        "valorCancelado",
        "statusCancelamento",
        "planoRevendedora.nomeDoPlano"]
        //Mudar para tipo equipamento MTEF
    }

    Map getFormatters() {
        ["dataHora":{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
         "data":{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
         "dataConciliacaoTransacao":{v-> v? dataMigrationUtilService.formatDateDefault(v):null}]
    }

    void importData(){
        dataMigrationUtilService.importData(Transacao) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)

        String cartao = instanceMap['cartao']
        Long nsuHost = instanceMap['nsuHost'] as Long
        Transacao transacao = Transacao.findByCartaoAndNsuHost(cartao, nsuHost)
        if(!transacao) transacao = new Transacao()
        transacao.tipoEquipamento = TipoEquipamento.MTEF
        transacao.adquirente = Adquirente.get(4)
        transacao.data = Date.parse("dd/MM/yyy HH:mm", instanceMap['data'])
        transacao.dataHora = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataHora'])
        String dataConciliacaoTransacao = instanceMap['dataConciliacaoTransacao']
        if(dataConciliacaoTransacao == 'null') dataConciliacaoTransacao = null
        transacao.dataConciliacaoTransacao = dataConciliacaoTransacao ? Date.parse("dd/MM/yyy HH:mm", dataConciliacaoTransacao):null
        transacao.valor = instanceMap['valor'] as Double
        transacao.terminal = instanceMap['terminal']
        transacao.estabelecimento = instanceMap['estabelecimento']
        transacao.estabelecimentoAdquirente = instanceMap['estabelecimentoAdquirente']
        transacao.tipo = TipoTransacao.valueOf(instanceMap['tipo'])
        transacao.totalParcelas = instanceMap['totalParcelas'] as Integer
        transacao.merchant = RevendedoraCarteira.findByCodigo(instanceMap['merchant.codigo'])
        transacao.statusAgendamento = StatusAgendamento.valueOf(instanceMap['statusAgendamento'])
        transacao.statusAgendamentoAdquirente = StatusAgendamento.valueOf(instanceMap['statusAgendamentoAdquirente'])
        transacao.statusConsolidacao = StatusConsolidacao.valueOf(instanceMap['statusConsolidacao'])
        transacao.cartao = cartao
        transacao.nsuHost = nsuHost
        transacao.nsuSitef = instanceMap['nsuSitef'] as Integer
        transacao.codigoAutorizacao = instanceMap['codigoAutorizacao']
        transacao.origemTransacao = OrigemTransacao.valueOf(instanceMap['origemTransacao'])
        transacao.bandeira = instanceMap['bandeira.codigo'] ? Bandeira.findByCodigo(instanceMap['bandeira.codigo']):null
        transacao.status = StatusTransacao.valueOf(instanceMap['status'])
        transacao.statusAdquirente = StatusTransacaoAdquirente.valueOf(instanceMap['statusAdquirente'])
        transacao.situacaoConciliacao = instanceMap['situacaoConciliacao.nome'] ? SituacaoConciliacao.findByNome(instanceMap['situacaoConciliacao.nome']):null
        transacao.statusConciliacao = instanceMap['statusConciliacao.nome'] ? StatusConciliacao.findByNome(instanceMap['statusConciliacao.nome']):null
        transacao.statusValidacao = StatusValidacao.valueOf(instanceMap['statusValidacao'])
        transacao.statusValidacaoData = StatusValidacao.valueOf(instanceMap['statusValidacaoData'])
        transacao.taxaAdmPagar = instanceMap['taxaAdmPagar']!='null'? instanceMap['taxaAdmPagar'] as BigDecimal:null
        transacao.valorPagar = instanceMap['valorPagar']!='null'? instanceMap['valorPagar'] as BigDecimal:null
        transacao.valorFranquiaReceber = instanceMap['valorFranquiaReceber']!='null'? instanceMap['valorFranquiaReceber'] as BigDecimal:null
        transacao.valorCanceladoSolicitado = instanceMap['valorCanceladoSolicitado'] as BigDecimal
        transacao.valorCancelado = instanceMap['valorCancelado'] as BigDecimal
        transacao.statusCancelamento = StatusCancelamento.findByNome(instanceMap['statusCancelamento'])
        transacao.planoRevendedora = PlanoRevendedora.findByNomeDoPlano(instanceMap['planoRevendedora.nomeDoPlano'])
        transacao.save(flush: true)

        log.debug("Transacao #${transacao.id} importado")
        log.debug(transacao.dump())
    }
}
