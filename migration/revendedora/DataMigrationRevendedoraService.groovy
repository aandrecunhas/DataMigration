package com.sysdata.ecarteira.data.migration.revendedora

import com.sysdata.coadquirencia.Cidade
import com.sysdata.coadquirencia.Empresa
import com.sysdata.coadquirencia.Endereco
import com.sysdata.coadquirencia.Estado
import com.sysdata.coadquirencia.GrupoSitef
import com.sysdata.coadquirencia.Pessoa
import com.sysdata.coadquirencia.SegmentoAtuacaoFranquia
import com.sysdata.coadquirencia.StatusConfiguracaoNumeroLogico
import com.sysdata.coadquirencia.StatusSubadquirencia
import com.sysdata.coadquirencia.TipoMerchant
import com.sysdata.ecarteira.PlanoRevendedora
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.StatusNotificacaoCadastroRevendedora
import com.sysdata.ecarteira.data.migration.DataMigrationService

class DataMigrationRevendedoraService implements DataMigrationService {
    def dataMigrationUtilService

    void exportData(Integer max = null){
        dataMigrationUtilService.exportData(RevendedoraCarteira, null, getFields(), getFormatters(), max)
    }

    List<String> getFields(){
        ["participante.nome",
         "participante.cpf",
         "participante.endereco.logradouro",
         "participante.endereco.numero",
         "participante.endereco.cidade.nome",
         "participante.endereco.cidade.estado.sigla",
         "contaPagamento",
         "planoEscolhido.nomeDoPlano",
         "statusNotificacaoStarsoft",
         "statusNotificacaoConductor",
         "segmentoAtuacao",
         "statusSubadquirencia",
         "statusMTef",
         "dataTombamento",
         "grupoMTef.nome",
         "codigo"]
    }

    Map getFormatters() {
        ["dataTombamento": {v-> dataMigrationUtilService.formatDateDefault(v)}]
    }

    void importData(){
        dataMigrationUtilService.importData(RevendedoraCarteira) { line ->
            createObject(line)
        }
    }

    void createObject(String line){
        List<String> fields = getFields()
        List<String> values = line.split(";")

        Map instanceMap = dataMigrationUtilService.joinListIntoMap(fields, values)

        Pessoa pessoa = new Pessoa()
        pessoa.nome = instanceMap['participante.nome']
        pessoa.cpf = instanceMap['participante.cpf']

        Endereco endereco = new Endereco()
        endereco.logradouro = instanceMap['participante.endereco.logradouro']
        endereco.numero = instanceMap['participante.endereco.numero'] == "null" ? null:instanceMap['participante.endereco.numero']
        def nomeCidade = instanceMap['participante.endereco.cidade.nome']
        def siglaEstado = instanceMap['participante.endereco.cidade.estado.sigla']
        def estado
        def cidade
        if(estado && cidade) {
            estado = Estado.findBySigla(siglaEstado)
            cidade = Cidade.findByNomeAndEstado(nomeCidade, estado)
            endereco.cidade = cidade
        }
        endereco.save(flush: true)

        pessoa.endereco = endereco
        pessoa.save(flush: true)

        RevendedoraCarteira revendedora = new RevendedoraCarteira()
        revendedora.participante = pessoa
        revendedora.contaPagamento = instanceMap['contaPagamento']
        revendedora.planoEscolhido = PlanoRevendedora.findByNomeDoPlano(instanceMap['planoEscolhido.nomeDoPlano'])
        revendedora.statusNotificacaoStarsoft = StatusNotificacaoCadastroRevendedora.valueOf(instanceMap['statusNotificacaoStarsoft'])
        revendedora.statusNotificacaoConductor = StatusNotificacaoCadastroRevendedora.valueOf(instanceMap['statusNotificacaoConductor'])
        revendedora.segmentoAtuacao = SegmentoAtuacaoFranquia.ATACADO
        revendedora.statusSubadquirencia = StatusSubadquirencia.valueOf(instanceMap['statusSubadquirencia'])
        revendedora.statusMTef = StatusConfiguracaoNumeroLogico.valueOf(instanceMap['statusMTef'])
        revendedora.dataTombamento = Date.parse("dd/MM/yyy HH:mm", instanceMap['dataTombamento'])
        revendedora.grupoMTef = GrupoSitef.findByNome(instanceMap['grupoMTef.nome'])
        revendedora.codigo = instanceMap['codigo']
        revendedora.empresa = Empresa.findByCodigo("1001")
        revendedora.tipoMerchant = TipoMerchant.REVENDEDORA
        revendedora.save(flush: true)
        log.debug("Revendedora #${revendedora.codigo} importada")
        log.debug(revendedora.dump())
    }
}
