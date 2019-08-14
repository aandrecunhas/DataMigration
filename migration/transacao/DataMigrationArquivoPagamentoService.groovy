package com.sysdata.ecarteira.data.migration.transacao

import com.sysdata.arquivo.StatusArquivo
import com.sysdata.arquivo.TipoArquivo
import com.sysdata.coadquirencia.ArquivoPagamento
import com.sysdata.coadquirencia.PagStarSoft
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationArquivoPagamentoService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            or {
                like("nome", "ADQUIRENCIA_PAGTO_CAP_CONDUCTOR%")
                like("nome", "Split%")
            }
        }
        dataMigrationUtilService.exportData(ArquivoPagamento, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ['servicoBancario.identificador',
        'lote',
        'nome',
        'status',
        'tipo',
        'conteudo']
    }

    Map getFormatters() {
        ['conteudo':{v-> v.replace("\n", "#")}]
    }

    void importData(){
        dataMigrationUtilService.importData(ArquivoPagamento) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")


        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        ArquivoPagamento arquivoPagamento = new ArquivoPagamento()
        arquivoPagamento.servicoBancario = instanceMap['servicoBancario.identificador']? PagStarSoft.findByIdentificador(instanceMap['servicoBancario.identificador']):null
        arquivoPagamento.lote = instanceMap['lote'] as Integer
        arquivoPagamento.nome = instanceMap['nome']
        arquivoPagamento.status = StatusArquivo.valueOf(instanceMap['status'])
        arquivoPagamento.tipo = TipoArquivo.valueOf(instanceMap['tipo'])
        arquivoPagamento.conteudo = instanceMap['conteudo'].toString().replace("#", "\n")
        arquivoPagamento.save(flush: true)

        log.debug("Arquivo Pagamento #${arquivoPagamento.id} importado")
        log.debug(arquivoPagamento.dump())
    }
}
