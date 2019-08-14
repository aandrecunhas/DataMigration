package com.sysdata.ecarteira.data.migration.transacao

import com.sysdata.coadquirencia.Adquirente
import com.sysdata.coadquirencia.Bandeira
import com.sysdata.coadquirencia.Cenario
import com.sysdata.coadquirencia.Lancamento
import com.sysdata.coadquirencia.LancamentoResumoVenda
import com.sysdata.coadquirencia.Pagamento
import com.sysdata.coadquirencia.Papel
import com.sysdata.coadquirencia.SituacaoLancamento
import com.sysdata.coadquirencia.StatusExtrato
import com.sysdata.coadquirencia.StatusLancamento
import com.sysdata.coadquirencia.TipoEquipamento
import com.sysdata.coadquirencia.TipoLancamento
import com.sysdata.coadquirencia.TipoTransacao
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationLancamentoResumoVendaService implements DataMigrationService {
    def dataMigrationUtilService
    def contaService

    void exportData(Integer max = null){
        Closure criteria = {
            eq('tipoEquipamento', TipoEquipamento.MTEF)
        }
        dataMigrationUtilService.exportData(LancamentoResumoVenda, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["numeroResumoVenda",
        "bandeira.codigo",
        "tipoTransacao",
        "parcela",
        "dataPagamentoPrevista",
        "dataPagamentoOriginal",
        "dataEnvioBanco",
        "taxaAdm",
        "valorBruto",
        "valorTaxaAdm",
        "situacaoLancamento",
        "valor",
        "data",
        "dataEfetivacao",
        "diaEfetivacao",
        "mesEfetivacao",
        "anoEfetivacao",
        "dataPagamento",
        "tipoLancamento",
         "conta.papel.codigo",
         "conta.dono.codigo",
        "statusLancamento",
        "statusExtratoVenda",
        "lancamentoOriginal.id",
        "pagamento.id",
        "id"]
        //Nao esquecer de tipo Equipamento MTEF
        //Cenario.I
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
        dataMigrationUtilService.importData(LancamentoResumoVenda) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")



        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        LancamentoResumoVenda lancamento = new LancamentoResumoVenda()
        lancamento.numeroResumoVenda = instanceMap['numeroResumoVenda'] as Long
        lancamento.bandeira = instanceMap['bandeira.codigo'] ? Bandeira.findByCodigo(instanceMap['bandeira.codigo']):null
        lancamento.tipoTransacao = instanceMap['tipoTransacao'] ? TipoTransacao.valueOf(instanceMap['tipoTransacao']):null
        lancamento.parcela = instanceMap['parcela'] as Integer
        lancamento.dataPagamentoPrevista = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamentoPrevista'])
        lancamento.dataPagamentoOriginal = instanceMap['dataPagamentoOriginal'] ? Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamentoOriginal']):null
        lancamento.dataEnvioBanco = instanceMap['dataEnvioBanco'] ? Date.parse("dd/MM/yyy HH:mm", instanceMap['dataEnvioBanco']):null
        lancamento.taxaAdm = instanceMap['taxaAdm'] as BigDecimal
        lancamento.valorBruto = instanceMap['valorBruto'] as BigDecimal
        lancamento.valorTaxaAdm = instanceMap['valorTaxaAdm'] as BigDecimal
        lancamento.situacaoLancamento = SituacaoLancamento.valueOf(instanceMap['situacaoLancamento'])
        lancamento.valor = instanceMap['valor'] as BigDecimal
        lancamento.data = Date.parse("dd/MM/yyy HH:mm", instanceMap['data'])
        lancamento.dataEfetivacao = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataEfetivacao'])
        lancamento.diaEfetivacao = instanceMap['diaEfetivacao'] as Integer
        lancamento.mesEfetivacao = instanceMap['mesEfetivacao'] as Integer
        lancamento.anoEfetivacao = instanceMap['anoEfetivacao'] as Integer
        lancamento.dataPagamento = instanceMap['dataPagamento'] ? Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamento']):null
        lancamento.tipoLancamento = TipoLancamento.valueOf(instanceMap['tipoLancamento'])
        lancamento.statusLancamento = StatusLancamento.valueOf(instanceMap['statusLancamento'])
        lancamento.statusExtratoVenda = StatusExtrato.valueOf(instanceMap['statusExtratoVenda'])
        lancamento.pagamento = instanceMap['pagamento.id'] ? Pagamento.findByReferenceId(instanceMap['pagamento.id'] as Long):null
        lancamento.tipoEquipamento = TipoEquipamento.MTEF
        lancamento.adquirente = Adquirente.get(4)
        lancamento.cenario = Cenario.I
        Papel papel = Papel.findByCodigo(instanceMap['conta.papel.codigo'])
        Papel dono = Papel.findByCodigo(instanceMap['conta.dono.codigo'])
        lancamento.conta = contaService.getConta(papel, dono)
        lancamento.lancamentoReferenceId = instanceMap['id']

        lancamento.save(flush: true)

        log.debug("Lancamento RV #${lancamento.id} importado")
        log.debug(lancamento.dump())
    }
}
