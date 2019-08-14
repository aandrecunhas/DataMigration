package com.sysdata.ecarteira.data.migration.transacao

import com.sysdata.arquivo.ArquivoProcessado
import com.sysdata.coadquirencia.DetalheTransacao
import com.sysdata.coadquirencia.TipoDetalheTransacao
import com.sysdata.coadquirencia.Transacao
import com.sysdata.coadquirencia.TransacaoSitef
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationTransacaoSitefService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            transacao {
                merchant {
                    eq('class', 'com.sysdata.ecarteira.RevendedoraCarteira')
                }
            }
        }
        dataMigrationUtilService.exportData(TransacaoSitef, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["codigoLoja",
        "nsuSitef",
        "statusSitef",
        "produto",
        "rede",
        "pdv",
        "ipSitef",
        "modoEntrada",
        "tipoSitef",
        "estabelecimento",
        "codigoAutorizacao",
        "cartao",
        "codigoPedido",
        "cupomFiscal",
        "arquivo.nome", //
        "versao",
        "transacao.cartao",
        "transacao.nsuHost"]
        //tipoDetalhe deve ser MTEF
    }

    Map getFormatters() {
        [:]
    }

    void importData(){
        dataMigrationUtilService.importData(TransacaoSitef) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        TransacaoSitef transacaoSitef = new TransacaoSitef()
        transacaoSitef.codigoLoja = instanceMap['codigoLoja']
        transacaoSitef.nsuSitef = instanceMap['nsuSitef'] as int
        transacaoSitef.statusSitef = instanceMap['statusSitef']
        transacaoSitef.produto = instanceMap['produto']
        transacaoSitef.rede = instanceMap['rede']
        transacaoSitef.pdv = instanceMap['pdv']
        transacaoSitef.ipSitef = instanceMap['ipSitef']
        transacaoSitef.modoEntrada = instanceMap['modoEntrada']
        transacaoSitef.tipoSitef = instanceMap['tipoSitef'] == 'null' ? null:instanceMap['tipoSitef']
        transacaoSitef.estabelecimento = instanceMap['estabelecimento']
        transacaoSitef.codigoAutorizacao = instanceMap['codigoAutorizacao']
        transacaoSitef.cartao = instanceMap['cartao']
        transacaoSitef.codigoPedido = instanceMap['codigoPedido']? null:instanceMap['codigoPedido']
        transacaoSitef.cupomFiscal = instanceMap['cupomFiscal']? null:instanceMap['cupomFiscal']
        transacaoSitef.tipoDetalhe = TipoDetalheTransacao.MTEF
        transacaoSitef.versao = instanceMap['versao'] as Integer
        Transacao transacao = Transacao.findByCartaoAndNsuHost(instanceMap['transacao.cartao'], instanceMap['transacao.nsuHost'])
        transacaoSitef.transacao = transacao
        transacaoSitef.arquivo = ArquivoProcessado.findByNome(instanceMap['arquivo.nome'])
        transacaoSitef.save(flush: true)

        log.debug("Transacao Sitef #${transacaoSitef.id} importado")
        log.debug(transacaoSitef.dump())
    }
}
