package com.sysdata.ecarteira.data.migration.transacao

import com.sysdata.coadquirencia.Lancamento
import com.sysdata.coadquirencia.LancamentoAdquirente
import com.sysdata.coadquirencia.ParcelaTransacao
import com.sysdata.coadquirencia.StatusLancamento
import com.sysdata.coadquirencia.TipoParcela
import com.sysdata.coadquirencia.Transacao
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationParcelaTransacaoService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            transacao {
                merchant {
                    eq('class', 'com.sysdata.ecarteira.RevendedoraCarteira')
                }
            }
        }
        dataMigrationUtilService.exportData(ParcelaTransacao, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["tipoParcela",
        "transacao.cartao",
        "transacao.nsuHost",
        "parcela",
        "taxaAdm",
        "valorBruto",
        "valorTaxaAdm",
        "valorLiquido",
        "dataPagamentoOriginal",
        "dataPagamentoPrevista",
        "statusLancamento",
        "ativo",
        "resumoVenda.id"]
    }

    Map getFormatters() {
        ["dataPagamentoOriginal":{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
        "dataPagamentoPrevista":{v-> v? dataMigrationUtilService.formatDateDefault(v):null}]
    }

    void importData(){
        dataMigrationUtilService.importData(ParcelaTransacao) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        ParcelaTransacao parcela = new ParcelaTransacao()
        parcela.tipoParcela = TipoParcela.valueOf(instanceMap['tipoParcela'])
        Transacao transacao = Transacao.findByCartaoAndNsuHost(instanceMap['transacao.cartao'], instanceMap['transacao.nsuHost'] as Long)
        assert(transacao)
        parcela.transacao = transacao
        parcela.parcela = instanceMap['parcela'] as Integer
        parcela.taxaAdm = instanceMap['taxaAdm']!='null'? instanceMap['taxaAdm'] as BigDecimal:null
        parcela.valorBruto = instanceMap['valorBruto']!='null'? instanceMap['valorBruto'] as BigDecimal:null
        parcela.valorTaxaAdm = instanceMap['valorTaxaAdm']!='null'? instanceMap['valorTaxaAdm'] as BigDecimal:null
        parcela.valorLiquido = instanceMap['valorLiquido']!='null'? instanceMap['valorLiquido'] as BigDecimal:null
        String dataPagamentoOriginal = instanceMap['dataPagamentoOriginal']
        if(dataPagamentoOriginal == 'null') dataPagamentoOriginal = null
        parcela.dataPagamentoOriginal = dataPagamentoOriginal ? Date.parse("dd/MM/yyy HH:mm", dataPagamentoOriginal):null
        parcela.dataPagamentoPrevista = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamentoPrevista'])
        parcela.statusLancamento = StatusLancamento.valueOf(instanceMap['statusLancamento'])
        parcela.ativo = instanceMap['ativo'] == "true" ? true:false
        Lancamento lancamento = Lancamento.findByLancamentoReferenceId(instanceMap['resumoVenda.id'])
        parcela.resumoVenda = lancamento
        parcela.save(flush: true)

        log.debug("Parcela Transacao #${parcela.id} importado")
        log.debug(parcela.dump())
    }
}
