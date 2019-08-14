package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.coadquirencia.Adquirente
import com.sysdata.coadquirencia.Bandeira
import com.sysdata.coadquirencia.DetalheRoteamentoNumeroLogico
import com.sysdata.coadquirencia.IntervaloParcelas
import com.sysdata.coadquirencia.NumeroLogicoAdquirente
import com.sysdata.coadquirencia.RegraRoteamento
import com.sysdata.coadquirencia.RoteamentoNumeroLogico
import com.sysdata.coadquirencia.SegmentoAtuacaoFranquia
import com.sysdata.coadquirencia.TipoEquipamento
import com.sysdata.coadquirencia.TipoTransacao
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationRegraRoteamentoService implements DataMigrationService{

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            regraRoteamento {
                eq("tipoEquipamento", TipoEquipamento.MTEF)
            }
        }
        dataMigrationUtilService.exportData(DetalheRoteamentoNumeroLogico, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ['intervalo.loja',
         'intervalo.tipoTransacao',
         'intervalo.min',
         'intervalo.max',
         'bandeira.codigo',
         'roteamentoNumeroLogico.numeroLogico.numeroLogico',
         'roteamentoNumeroLogico.loja.codigo']
    }

    Map getFormatters() {
        [:]
    }

    void importData(){
        dataMigrationUtilService.importData(DetalheRoteamentoNumeroLogico) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")


        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        DetalheRoteamentoNumeroLogico detalhe = new DetalheRoteamentoNumeroLogico()
        Bandeira bandeira = Bandeira.findByCodigo(instanceMap['bandeira.codigo'])
        detalhe.bandeira = bandeira
        TipoTransacao tipoTransacao = TipoTransacao.valueOf(instanceMap['intervalo.tipoTransacao'])
        Integer min = instanceMap['intervalo.min'] as Long
        Integer max = instanceMap['intervalo.max'] as Long

        IntervaloParcelas intervalo = IntervaloParcelas.findByTipoTransacaoAndMinAndMax(tipoTransacao, min, max)
        detalhe.intervalo = intervalo

        NumeroLogicoAdquirente numeroLogico = NumeroLogicoAdquirente.findByNumeroLogico(instanceMap['roteamentoNumeroLogico.numeroLogico.numeroLogico'])
        RevendedoraCarteira revendedora = RevendedoraCarteira.findByCodigo(instanceMap['roteamentoNumeroLogico.loja.codigo'])
        RoteamentoNumeroLogico roteamento = RoteamentoNumeroLogico.findByNumeroLogicoAndLoja(numeroLogico, revendedora)
        detalhe.roteamentoNumeroLogico = roteamento

        RegraRoteamento regra = new RegraRoteamento()
        regra.adquirente = Adquirente.get(4)
        regra.intervalo = intervalo
        regra.bandeira = bandeira
        regra.segmentoAtuacao = SegmentoAtuacaoFranquia.ATACADO
        regra.tipoEquipamento = TipoEquipamento.MTEF
        regra.configuracaoRoteamento = roteamento.configuracaoRoteamento
        regra.save(flush: true)

        detalhe.regraRoteamento = regra

        detalhe.save(flush: true)

        log.debug("Detalhe Roteamento (Regra) #${detalhe.id} importado")
        log.debug(detalhe.dump())
    }
}
