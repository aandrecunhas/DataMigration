package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.coadquirencia.ConfiguracaoRoteamento
import com.sysdata.coadquirencia.DetalheRoteamentoNumeroLogico
import com.sysdata.coadquirencia.Empresa
import com.sysdata.coadquirencia.NumeroLogicoAdquirente
import com.sysdata.coadquirencia.RoteamentoNumeroLogico
import com.sysdata.coadquirencia.Status
import com.sysdata.coadquirencia.StatusEnvio
import com.sysdata.coadquirencia.TipoEquipamento
import com.sysdata.ecarteira.PlanoRevendedora
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.data.migration.DataMigrationService


class DataMigrationRoteamentoNumeroLogicoService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            eq("tipoEquipamento", TipoEquipamento.MTEF)
        }

        dataMigrationUtilService.exportData(RoteamentoNumeroLogico, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ['numeroLogico.numeroLogico',
         'configuracaoRoteamento.planoRevendedora.nomeDoPlano',
         'status',
         'statusEnvio',
         'loja.codigo']
    }

    Map getFormatters() {
        [:]
    }

    void importData(){
        dataMigrationUtilService.importData(RoteamentoNumeroLogico) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")


        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        RoteamentoNumeroLogico roteamento = new RoteamentoNumeroLogico()
        roteamento.numeroLogico = NumeroLogicoAdquirente.findByNumeroLogico(instanceMap['numeroLogico.numeroLogico'])

        PlanoRevendedora plano = PlanoRevendedora.findByNomeDoPlano(instanceMap['configuracaoRoteamento.planoRevendedora.nomeDoPlano'])
        ConfiguracaoRoteamento configuracao = ConfiguracaoRoteamento.findByPlanoRevendedora(plano)
        roteamento.configuracaoRoteamento = configuracao
        roteamento.tipoEquipamento = TipoEquipamento.MTEF
        roteamento.status = Status.valueOf(instanceMap['status'])
        roteamento.statusEnvio = StatusEnvio.valueOf(instanceMap['statusEnvio'])
        roteamento.loja = RevendedoraCarteira.findByCodigo(instanceMap['loja.codigo'])
        roteamento.save(flush: true)

        log.debug("Roteamento #${roteamento.id} importado")
        log.debug(roteamento.dump())

    }
}
