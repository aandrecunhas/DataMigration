package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.coadquirencia.Banco
import com.sysdata.coadquirencia.DadosBancarios
import com.sysdata.coadquirencia.Pessoa
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationDadosBancariosService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            participante {
                eq('class', 'com.sysdata.coadquirencia.Pessoa')
            }
        }
        dataMigrationUtilService.exportData(DadosBancarios, criteria, getFields(), getFormatters(), max)

    }

    List<String> getFields(){
        ["banco.codigo",
         "agencia",
         "dvAgencia",
         "conta",
         "dvConta",
         "participante.cpf"]
    }

    Map getFormatters(){
        [:]
    }

    void importData(){
        dataMigrationUtilService.importData(DadosBancarios) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        DadosBancarios dadosBancarios = new DadosBancarios()
        dadosBancarios.banco = Banco.findByCodigo(instanceMap['banco.codigo'])
        dadosBancarios.agencia = instanceMap['agencia']
        dadosBancarios.dvAgencia = instanceMap['dvAgencia']
        dadosBancarios.conta = instanceMap['conta']
        dadosBancarios.dvConta = instanceMap['dvConta']
        def participante = Pessoa.findByCpf(instanceMap['participante.cpf'])
        dadosBancarios.participante = participante
        dadosBancarios.save(flush: true)

        log.debug("Dados Bancarios #${dadosBancarios.id} importado")
        log.debug(dadosBancarios.dump())
    }
}
