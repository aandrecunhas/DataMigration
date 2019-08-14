package com.sysdata.ecarteira.data.migration

import com.sysdata.coadquirencia.Transacao

class MigrationService {
    def initialDataMigrationService
    //
    def dataMigrationRevendedoraService
    def dataMigrationDadosBancariosService
    def dataMigrationHistoricoPlanoRevendedoraService
    def dataMigrationMudancaPlanoService
    def dataMigrationRoteamentoNumeroLogicoService
    def dataMigrationRegraRoteamentoService
    def dataMigrationRemessaSolicitacaoService
    def dataMigrationSolicitacaoTrocaPlanoService
    //transacao
    def dataMigrationTransacaoService
    def dataMigrationTransacaoSitefService
    def dataMigrationArquivoPagamentoService
    def dataMigrationPagamentoService
    def dataMigrationLancamentoResumoVendaService
    def dataMigrationLancamentoAdquirenteService
    def dataMigrationParcelaTransacaoService

    def exportAll() {
        dataMigrationRevendedoraService.exportData()
        dataMigrationDadosBancariosService.exportData()
        dataMigrationHistoricoPlanoRevendedoraService.exportData()
        dataMigrationMudancaPlanoService.exportData()
        dataMigrationRoteamentoNumeroLogicoService.exportData()
        dataMigrationRegraRoteamentoService.exportData()
        dataMigrationRemessaSolicitacaoService.exportData()
        dataMigrationSolicitacaoTrocaPlanoService.exportData()
        //transacao
        dataMigrationTransacaoService.exportData()
        dataMigrationTransacaoSitefService.exportData()
        dataMigrationArquivoPagamentoService.exportData()
        dataMigrationPagamentoService.exportData()
        dataMigrationLancamentoResumoVendaService.exportData()
        dataMigrationLancamentoAdquirenteService.exportData()
        dataMigrationParcelaTransacaoService.exportData()
    }

    def importAll(){
/*
        importWithNewSession { initialDataMigrationService.processar() }
*/
        importWithNewSession { dataMigrationRevendedoraService.importData() }
        importWithNewSession { dataMigrationDadosBancariosService.importData() }
        importWithNewSession { dataMigrationHistoricoPlanoRevendedoraService.importData() }
        importWithNewSession { dataMigrationMudancaPlanoService.importData() }
        importWithNewSession { dataMigrationRoteamentoNumeroLogicoService.importData() }
        importWithNewSession { dataMigrationRegraRoteamentoService.importData() }
        importWithNewSession { dataMigrationRemessaSolicitacaoService.importData() }
        importWithNewSession { dataMigrationSolicitacaoTrocaPlanoService.importData() }
        //transacao
        importWithNewSession { dataMigrationTransacaoService.importData() }
        importWithNewSession { dataMigrationTransacaoSitefService.importData() }
        importWithNewSession { dataMigrationArquivoPagamentoService.importData() }
        importWithNewSession { dataMigrationPagamentoService.importData() }
        importWithNewSession { dataMigrationLancamentoResumoVendaService.importData() }
        importWithNewSession { dataMigrationLancamentoAdquirenteService.importData() }
        importWithNewSession { dataMigrationParcelaTransacaoService.importData() }
    }

    def importWithNewSession(Closure closure){
        Transacao.withNewSession {
            closure()
        }
    }
}
