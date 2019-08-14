package com.sysdata.ecarteira.data.migration.transacao

import com.sysdata.arquivo.Arquivo
import com.sysdata.coadquirencia.AgentePagador
import com.sysdata.coadquirencia.Banco
import com.sysdata.coadquirencia.Bandeira
import com.sysdata.coadquirencia.ContaBancaria
import com.sysdata.coadquirencia.Organizacao
import com.sysdata.coadquirencia.Pagamento
import com.sysdata.coadquirencia.Papel
import com.sysdata.coadquirencia.Participante
import com.sysdata.coadquirencia.Pessoa
import com.sysdata.coadquirencia.StatusExtrato
import com.sysdata.coadquirencia.StatusPagamento
import com.sysdata.coadquirencia.StatusRetencao
import com.sysdata.coadquirencia.TipoConta
import com.sysdata.coadquirencia.TipoPagamento
import com.sysdata.coadquirencia.TipoTransacao
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationPagamentoService implements DataMigrationService {

    def dataMigrationUtilService

    void exportData(Integer max = null){
        Closure criteria = {
            arquivo {
                or {
                    like("nome", "ADQUIRENCIA_PAGTO_CAP_CONDUCTOR%")
                    like("nome", "Split%")
                }
            }
        }
        dataMigrationUtilService.exportData(Pagamento, criteria, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ['data',
        'dataPagamento',
        'papel.codigo',
        'pagador.codigo',
        'valor',
        'contaBancaria.banco.codigo',
        'contaBancaria.agencia',
        'contaBancaria.dvAgencia',
        'contaBancaria.conta',
        'contaBancaria.dvConta',
        'contaBancaria.tipoConta',
        'contaBancaria.participante.cnpj',
        'contaBancaria.participante.cpf',
        'contaBancaria.participante.class',
        'arquivo.nome',
        'tipoPagamento',
        'statusPagamento',
        'numeroPagamento',
        'descricao',
        'statusExtratoPagamento',
        'tipoTransacao',
        'bandeira.codigo',
        'agentePagador',
        'valorOriginal',
        'statusRetencao',
        'pagamentoOriginal.id',
        'id']
    }

    Map getFormatters() {
        ['data': {v-> v? dataMigrationUtilService.formatDateDefault(v):null},
        'dataPagamento': {v-> v? dataMigrationUtilService.formatDateDefault(v):null}]
    }

    void importData(){
        dataMigrationUtilService.importData(Pagamento) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")


        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)
        Pagamento pagamento = new Pagamento()
        pagamento.data = Date.parse("dd/MM/yyy HH:mm", instanceMap['data'])
        pagamento.dataPagamento = instanceMap['dataPagamento'] ? Date.parse("dd/MM/yyy HH:mm", instanceMap['dataPagamento']):null
        pagamento.papel = Papel.findByCodigo(instanceMap['papel.codigo'])
        pagamento.pagador = Papel.findByCodigo(instanceMap['pagador.codigo'])
        pagamento.valor = instanceMap['valor'] as BigDecimal
        ContaBancaria contaBancaria = new ContaBancaria()
        contaBancaria.banco = Banco.findByCodigo(instanceMap['contaBancaria.banco.codigo'])
        contaBancaria.agencia = instanceMap['contaBancaria.agencia']
        contaBancaria.dvAgencia = instanceMap['contaBancaria.dvAgencia']
        contaBancaria.conta = instanceMap['contaBancaria.conta']
        contaBancaria.dvConta = instanceMap['contaBancaria.dvConta']
        contaBancaria.tipoConta = TipoConta.CONTA_CORRENTE_INDIVIDUAL//instanceMap['contaBancaria.tipoConta'] ? TipoConta.valueOf(instanceMap['contaBancaria.tipoConta']):null
        String instanceType = instanceMap['contaBancaria.participante.class']
        Participante participante
        if(instanceType == "class com.sysdata.coadquirencia.Organizacao") participante = Organizacao.findByCnpj(instanceMap['contaBancaria.participante.cnpj'])
        if(instanceType == "class com.sysdata.coadquirencia.Pessoa") participante = Pessoa.findByCpf(instanceMap['contaBancaria.participante.cpf'])
        contaBancaria.participante = participante
        contaBancaria.save(flush: true)
        pagamento.contaBancaria = contaBancaria
        pagamento.arquivo = Arquivo.findByNome(instanceMap['arquivo.nome'])
        pagamento.tipoPagamento = TipoPagamento.valueOf(instanceMap['tipoPagamento'])
        pagamento.statusPagamento = StatusPagamento.valueOf(instanceMap['statusPagamento'])
        pagamento.numeroPagamento = instanceMap['numeroPagamento'] as Integer
        pagamento.descricao = instanceMap['descricao']
        pagamento.statusExtratoPagamento = StatusExtrato.valueOf(instanceMap['statusExtratoPagamento'])
        pagamento.tipoTransacao = instanceMap['tipoTransacao'] ? TipoTransacao.valueOf(instanceMap['tipoTransacao']):null
        pagamento.bandeira = instanceMap['bandeira.codigo'] ? Bandeira.findByCodigo(instanceMap['bandeira.codigo']):null
        pagamento.agentePagador = AgentePagador.valueOf(instanceMap['agentePagador'])
        pagamento.valorOriginal = instanceMap['valorOriginal'] as BigDecimal
        pagamento.statusRetencao = StatusRetencao.valueOf(instanceMap['statusRetencao'])
        pagamento.referenceIdPagamentoOriginal = instanceMap['pagamentoOriginal.id']
        pagamento.referenceId = instanceMap['id']
        pagamento.save(flush: true)

        log.debug("Pagamento #${pagamento.id} importado")
        log.debug(pagamento.dump())
    }
}
