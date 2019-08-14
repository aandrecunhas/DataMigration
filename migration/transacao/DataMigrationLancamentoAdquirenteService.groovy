package com.sysdata.ecarteira.data.migration.transacao

import com.sysdata.coadquirencia.Adquirente
import com.sysdata.coadquirencia.Bandeira
import com.sysdata.coadquirencia.Cenario
import com.sysdata.coadquirencia.LancamentoAdquirente
import com.sysdata.coadquirencia.Papel
import com.sysdata.coadquirencia.SituacaoLancamento
import com.sysdata.coadquirencia.StatusExtrato
import com.sysdata.coadquirencia.StatusLancamento
import com.sysdata.coadquirencia.TipoLancamento
import com.sysdata.coadquirencia.TipoTransacao
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationLancamentoAdquirenteService implements DataMigrationService {

    def dataMigrationUtilService
    def contaService
    void exportData(Integer max = null){

        dataMigrationUtilService.exportData(LancamentoAdquirente, null, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ['numeroResumoVenda',
        'bandeira.codigo',
        'tipoTransacao',
        'parcela',
        'totalParcelas',
        'valorPago',
        'valorBrutoEstabelecimento',
        'valorTaxaAdmEstabelecimento',
        'valorEstabelecimento',
        'resumoAdquirenteId',
        'dataPagamentoPrevista',
        'dataPagamentoOriginal',
        'dataEnvioBanco',
        'taxaAdm',
        'valorBruto',
        'valorTaxaAdm',
        'situacaoLancamento',
        'valor',
        'conta.papel.codigo',
        'conta.dono.codigo',
        'data',
        'dataEfetivacao',
        'diaEfetivacao',
        'mesEfetivacao',
        'anoEfetivacao',
        'dataPagamento',
        'tipoLancamento',
        'statusLancamento',
        'statusExtratoVenda',
        'lancamentoOriginal.id',
        'id']
        //Cenario.I
        //adquirente rede
    }

    Map getFormatters() {
        ['dataPagamentoPrevista':{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
        'dataPagamentoOriginal':{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
        'dataEnvioBanco':{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
        'data':{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
        'dataEfetivacao':{v-> v? dataMigrationUtilService.formatDateDefault(v):null},
        'dataPagamento':{v-> v? dataMigrationUtilService.formatDateDefault(v):null}]
    }

    void importData(){
        dataMigrationUtilService.importData(LancamentoAdquirente) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")


        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        LancamentoAdquirente lancamento = new LancamentoAdquirente()
        lancamento.numeroResumoVenda = instanceMap['numeroResumoVenda'] as Long
        lancamento.bandeira = instanceMap['bandeira.codigo'] ? Bandeira.findByCodigo(instanceMap['bandeira.codigo']):null
        lancamento.tipoTransacao = TipoTransacao.valueOf(instanceMap['tipoTransacao'])
        lancamento.parcela = instanceMap['parcela'] as Integer
        lancamento.totalParcelas = instanceMap['totalParcelas'] as Integer
        lancamento.valorPago = instanceMap['valorPago'] as BigDecimal
        lancamento.valorBrutoEstabelecimento = instanceMap['valorBrutoEstabelecimento'] as BigDecimal
        lancamento.valorTaxaAdmEstabelecimento = instanceMap['valorTaxaAdmEstabelecimento'] as BigDecimal
        lancamento.valorEstabelecimento = instanceMap['valorEstabelecimento'] as BigDecimal
        lancamento.resumoAdquirenteId = instanceMap['resumoAdquirenteId'] as Long
        lancamento.dataPagamentoPrevista = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamentoPrevista'])
        lancamento.dataPagamentoOriginal = instanceMap['dataPagamentoOriginal'] ? Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamentoOriginal']):null
        lancamento.dataEnvioBanco = instanceMap['dataEnvioBanco'] ? Date.parse("dd/MM/yyy HH:mm", instanceMap['dataEnvioBanco']):null
        lancamento.taxaAdm = instanceMap['taxaAdm'] as BigDecimal
        lancamento.valorBruto = instanceMap['valorBruto'] as BigDecimal
        lancamento.valorTaxaAdm = instanceMap['valorTaxaAdm'] as BigDecimal
        lancamento.situacaoLancamento = SituacaoLancamento.valueOf(instanceMap['situacaoLancamento'])
        lancamento.valor = instanceMap['valor'] as BigDecimal
        Papel papel = Papel.findByCodigo(instanceMap['conta.papel.codigo'])
        Papel dono = Papel.findByCodigo(instanceMap['conta.dono.codigo'])
        lancamento.conta = contaService.getConta(papel, dono)
        lancamento.data = Date.parse("dd/MM/yyy HH:mm", instanceMap['data'])
        lancamento.dataEfetivacao = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataEfetivacao'])
        lancamento.diaEfetivacao = instanceMap['diaEfetivacao'] as Integer
        lancamento.mesEfetivacao = instanceMap['mesEfetivacao'] as Integer
        lancamento.anoEfetivacao = instanceMap['anoEfetivacao'] as Integer
        lancamento.dataPagamento = instanceMap['dataPagamento'] ? Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamento']):null
        lancamento.tipoLancamento = TipoLancamento.valueOf(instanceMap['tipoLancamento'])
        lancamento.statusLancamento = StatusLancamento.valueOf(instanceMap['statusLancamento'])
        lancamento.statusExtratoVenda = StatusExtrato.valueOf(instanceMap['statusExtratoVenda'])
        lancamento.lancamentoReferenceId = instanceMap['id']
        lancamento.adquirente = Adquirente.get(4)
        lancamento.cenario = Cenario.I
        lancamento.save(flush: true)

        log.debug("Lancamento Adquirente #${lancamento.id} importado")
        log.debug(lancamento.dump())
    }
}
