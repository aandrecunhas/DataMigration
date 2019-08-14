package com.sysdata.ecarteira.data.migration

import com.sysdata.coadquirencia.Adquirente
import com.sysdata.coadquirencia.Empresa
import com.sysdata.coadquirencia.GrupoSitef
import com.sysdata.coadquirencia.NumeroLogicoAdquirente
import com.sysdata.coadquirencia.SegmentoAtuacaoFranquia
import com.sysdata.coadquirencia.Status
import com.sysdata.coadquirencia.StatusEnvio
import com.sysdata.coadquirencia.TipoEquipamento
import com.sysdata.ecarteira.PlanoRevendedora

/*@Description: */
class InitialDataMigrationService {

    def processar(){
        createAllPlanoRevendedora()
        createAllGrupoSitef()
        createAllNumeroLogico()
    }


    /* [INICIO] Importacao do Plano Revendedora */
    /* Como os valores de plano são estaticos, nao é necessario importar da base */
    def createAllPlanoRevendedora(){
        if(!PlanoRevendedora.findByNomeDoPlano("Plano 2 dias")) {
            createPlanoRevendedora("Plano 2 dias", true, 2)
            log.debug("Plano Revendedora 2 dias criado")
        }

        if(!PlanoRevendedora.findByNomeDoPlano("Plano 30 dias")) {
            createPlanoRevendedora("Plano 30 dias", true, 30)
            log.debug("Plano Revendedora 30 dias criado")
        }

    }

    def createPlanoRevendedora(String nome, Boolean ativo, Integer diasPrimeiraParcela){
        PlanoRevendedora plano = new PlanoRevendedora()
        plano.nomeDoPlano = nome
        plano.ativo = ativo
        plano.diasPrimeiraParcela = diasPrimeiraParcela
        plano.save(flush: true)
    }
    /* [FIM] Importacao do Plano Revendedora */

    /* [INICIO] Importacao Grupo Sitef*/

    def createAllGrupoSitef(){
        PlanoRevendedora plano2dias = PlanoRevendedora.findByNomeDoPlano("Plano 2 dias")
        PlanoRevendedora plano30dias = PlanoRevendedora.findByNomeDoPlano("Plano 30 dias")
        if(!GrupoSitef.findByPlanoRevendedora(plano30dias)) {
            createGrupoSitef("ME0001", plano30dias)
            createGrupoSitef("ME0002", plano30dias)
            createGrupoSitef("ME0003", plano30dias)
            createGrupoSitef("ME0004", plano30dias)
            log.debug("Grupos de 30 dias criados")
        }

        if(!GrupoSitef.findByPlanoRevendedora(plano2dias)) {
            createGrupoSitef("ME0005", plano2dias)
            createGrupoSitef("ME0006", plano2dias)
            createGrupoSitef("ME0007", plano2dias)
            createGrupoSitef("ME0008", plano2dias)
            log.debug("Grupos de 2 dias criados")
        }
    }

    def createGrupoSitef(String nome, PlanoRevendedora plano){
        GrupoSitef grupoSitef = new GrupoSitef()
        grupoSitef.nome = nome
        grupoSitef.tipoEquipamento = TipoEquipamento.MTEF
        grupoSitef.segmentoAtuacao = SegmentoAtuacaoFranquia.ATACADO
        grupoSitef.empresa = Empresa.findByCodigo("1001")
        grupoSitef.planoRevendedora = plano
        grupoSitef.save(flush: true)
    }

    /* [FIM] Importacao Grupo Sitef*/

    /* [INICIO] Importacao Numeros Logicos*/

    def createAllNumeroLogico(){
        if(!NumeroLogicoAdquirente.findByTipoEquipamento(TipoEquipamento.MTEF)) {
            createNumeroLogico("930744078140943", "930744078140943", PlanoRevendedora.findByNomeDoPlano("Plano 30 dias"), GrupoSitef.findByNome("ME0001"), "78140943")
            createNumeroLogico("930745078140943", "930745078140943", PlanoRevendedora.findByNomeDoPlano("Plano 30 dias"), GrupoSitef.findByNome("ME0002"), "78140943")
            createNumeroLogico("930746078140943", "930746078140943", PlanoRevendedora.findByNomeDoPlano("Plano 30 dias"), GrupoSitef.findByNome("ME0003"), "78140943")
            createNumeroLogico("930748078140943", "930748078140943", PlanoRevendedora.findByNomeDoPlano("Plano 30 dias"), GrupoSitef.findByNome("ME0004"), "78140943")
            createNumeroLogico("930758077462963", "930758077462963", PlanoRevendedora.findByNomeDoPlano("Plano 2 dias"), GrupoSitef.findByNome("ME0005"), "77462963")
            createNumeroLogico("930757077462963", "930757077462963", PlanoRevendedora.findByNomeDoPlano("Plano 2 dias"), GrupoSitef.findByNome("ME0006"), "77462963")
            createNumeroLogico("930753077462963", "930753077462963", PlanoRevendedora.findByNomeDoPlano("Plano 2 dias"), GrupoSitef.findByNome("ME0007"), "77462963")
            createNumeroLogico("930752077462963", "930752077462963", PlanoRevendedora.findByNomeDoPlano("Plano 2 dias"), GrupoSitef.findByNome("ME0008"), "77462963")
        }
    }

    def createNumeroLogico(String numeroLogico,
                           String estabelecimento,
                           PlanoRevendedora plano,
                           GrupoSitef grupo,
                           String numeroPV){
        NumeroLogicoAdquirente numeroLogicoAdquirente = new NumeroLogicoAdquirente()
        numeroLogicoAdquirente.numeroLogico = numeroLogico
        numeroLogicoAdquirente.estabelecimento = estabelecimento
        numeroLogicoAdquirente.tipoEquipamento = TipoEquipamento.MTEF
        numeroLogicoAdquirente.segmentoAtuacao = SegmentoAtuacaoFranquia.ATACADO
        numeroLogicoAdquirente.planoRevendedora = plano
        numeroLogicoAdquirente.status = Status.ATIVO
        numeroLogicoAdquirente.statusEnvio = StatusEnvio.ENVIADO
        numeroLogicoAdquirente.grupo = grupo
        numeroLogicoAdquirente.empresa = Empresa.findByCodigo("1001")
        numeroLogicoAdquirente.grupoRede = "79006868"
        numeroLogicoAdquirente.numeroPV = numeroPV
        numeroLogicoAdquirente.codigoTO = "29807"
        numeroLogicoAdquirente.adquirente = Adquirente.get(4)
        numeroLogicoAdquirente.save(flush: true)
    }

    /* [FIM] Importacao Numeros Logicos*/


}
