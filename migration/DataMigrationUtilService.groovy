package com.sysdata.ecarteira.data.migration

import com.sysdata.ecarteira.RevendedoraCarteira
import com.sysdata.util.Util

import java.text.SimpleDateFormat

class DataMigrationUtilService {

    def batchProcessingService

    File generateFile(String name, String conteudo, String path = "/tmp"){
        File file = new File(path+"//"+name)
        file << conteudo
    }

    String generateHeader(List<String> fields){
        def content = ""
        fields.each { field ->
            content += field+";"
        }
        content += "\n"
    }

    Map joinListIntoMap(List keys, List values){

        Map mapa = [:]

        keys.eachWithIndex{ def entry, int i ->
            def value = values[i]
            if(value == 'null') value = null
            mapa["${entry}"] = value
        }

        mapa
    }

    List<Long> getIDS(Class className, Closure criteria = null, Integer max = null){
        def actualCriteria = {
            if(max) maxResults(max)
            projections {
                property("id")
            }
        }

        if(criteria)
            actualCriteria = criteria << actualCriteria

        className.createCriteria().list(actualCriteria)
    }

    String generateDetail(Object object, List<String> fields, Map formatters = [:]){
        def detail = ""
        fields.each { field ->
            def value = Util.getPropriedade(object, field)
            def formatter = formatters[field]
            if(formatter)
                value = formatter.call(value)
            detail += value.toString() + ";"
        }
        detail += "\n"

        detail
    }

    String getContentRevendedora(List<Long> ids, List<String> fields, Map formatters = [:], Class className){
        def content = generateHeader(fields)

        ids.each { id ->
            Object obj = className.get(id)
            content += generateDetail(obj, fields, formatters)
            log.debug("Exportando #${id}")
        }

        return content
    }

    void exportData(Class className, Closure criteria, List<String> fields, Map formatters = [:], Integer max = null) {
        log.debug("Exportacao de ${className} iniciado")
        def ids = getIDS(className, criteria, max)
        def conteudo = getContentRevendedora(ids, fields, formatters, className)
        generateFile("${className}.csv", conteudo)
        log.debug("Exportacao de ${className} finalizado")

    }

    def importData(Class className, Closure closure){
        String path = "/tmp"
        String nome = "${className}.csv"

        log.debug("Iniciando importacao de ${className}")
        File file = new File(path+"/"+nome)
        def count = 0
        file.eachLine { line ->
            if(count > 0) {
                closure(line)
            }
            count++
            if(count % 20 == 0){
                batchProcessingService.gormClean()
            }
        }

        log.debug("Finalizando importacao de ${className}")
    }

    SimpleDateFormat getSimpleDateFormatDefault(){
        new java.text.SimpleDateFormat('dd/MM/yyyy HH:mm')
    }

    String formatDateDefault(Date date){
        getSimpleDateFormatDefault().format(date)
    }
}
