package com.sysdata.ecarteira.data.migration

import com.sysdata.coadquirencia.Adquirente
import com.sysdata.coadquirencia.Banco
import com.sysdata.coadquirencia.Bandeira
import com.sysdata.coadquirencia.Cidade
import com.sysdata.coadquirencia.ConfiguracaoNumeroLogico
import com.sysdata.coadquirencia.ConfiguracaoRoteamento
import com.sysdata.coadquirencia.DadosBancarios
import com.sysdata.coadquirencia.DetalheRoteamentoNumeroLogico
import com.sysdata.coadquirencia.Empresa
import com.sysdata.coadquirencia.Endereco
import com.sysdata.coadquirencia.Estado
import com.sysdata.coadquirencia.GrupoSitef
import com.sysdata.coadquirencia.IntervaloParcelas
import com.sysdata.coadquirencia.NumeroLogicoAdquirente
import com.sysdata.coadquirencia.Pessoa
import com.sysdata.coadquirencia.RegraRoteamento
import com.sysdata.coadquirencia.RoteamentoNumeroLogico
import com.sysdata.coadquirencia.SegmentoAtuacaoFranquia
import com.sysdata.coadquirencia.Status
import com.sysdata.coadquirencia.StatusConfiguracaoNumeroLogico
import com.sysdata.coadquirencia.StatusEnvio
import com.sysdata.coadquirencia.StatusSubadquirencia
import com.sysdata.coadquirencia.TipoEquipamento
import com.sysdata.coadquirencia.TipoMerchant
import com.sysdata.coadquirencia.TipoTransacao
import com.sysdata.ecarteira.HistoricoPlanoRevendedora
import com.sysdata.ecarteira.MudancaRevendedoraPlano
import com.sysdata.ecarteira.PlanoRevendedora
import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.ecarteira.StatusNotificacaoCadastroRevendedora
import com.sysdata.security.User
import com.sysdata.util.Util
//SEGUE A ORDEM DE IMPORTACAO
/*
1 - REVENDEDORAS
2 - DADOS BANCARIOS
3 - HISTORICO PLANO REVENDEDORA
4 - MUDANCA DE PLANO REVENDEDORA
5 - ROTEAMENTO NL + CONFIG NL
6 - REGRA DE ROTEAMENTO
 */
class ImportRevendedoraDataMrdService {

    def batchProcessingService
    def dataMigrationUtilService


    /*[INICIO] Exportar Regra Roteamento NL*/
    List<String> getFieldsRegraRoteamento(){
        ['intervalo.loja',
        'intervalo.tipoTransacao',
        'intervalo.min',
        'intervalo.max',
        'bandeira.codigo',
        'roteamentoNumeroLogico.numeroLogico.numeroLogico',
        'roteamentoNumeroLogico.loja.codigo']
    }

    List<Long> getRegraRoteamentoIDS(Integer max = null){
        DetalheRoteamentoNumeroLogico.createCriteria().list {
            regraRoteamento {
                eq("tipoEquipamento", TipoEquipamento.MTEF)
            }
            if(max)
                maxResults(max)
            projections {
                property('id')
            }
        }
    }

    void exportRegraRoteamento(Integer max = null) {
        log.debug("Exportacao de roteamento iniciado")
        def ids = getRegraRoteamentoIDS(max)
        def conteudo = getContentRegraRoteamento(ids)
        generateFile("regras.csv", conteudo)
        log.debug("Exportacao de roteamento finalizado")

    }

    String getContentRegraRoteamento(List<Long> ids){
        def content = generateHeader(getFieldsRegraRoteamento())

        ids.each { id ->
            DetalheRoteamentoNumeroLogico detalhe = DetalheRoteamentoNumeroLogico.get(id)
            content += generateDetail(detalhe)
        }

        return content
    }


    String generateDetail(DetalheRoteamentoNumeroLogico detalheRoteamento){
        def detail = ""
        getFieldsRegraRoteamento().each { field ->
            detail += Util.getPropriedade(detalheRoteamento, field).toString() + ";"
        }
        detail += "\n"

        detail
    }
    /*[FIM] Exportar Regra Roteamento NL*/

    /*[INICIO] Importar Regra Roteamento NL*/

    void importRegraRoteamento(String nome = "regras.csv", String path = "/tmp"){
        log.debug("Iniciando importacao de roteamento NL")
        File file = new File(path+"/"+nome)
        def count = 0
        file.eachLine { line ->
            if(count > 0) {
                createRegraRoteamento(line)
            }
            count++
            if(count % 100 == 0){
                batchProcessingService.gormClean()
            }
        }

        log.debug("Importacao de roteamento NL finalizada")

    }

    void createRegraRoteamento(String line){
        List<String> fields = getFieldsRegraRoteamento()
        List<String> values = line.split(";")


        Map instanceMap = joinListIntoMap(fields, values)
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

    /*[FIM] Importar Regra Roteamento NL*/

}
