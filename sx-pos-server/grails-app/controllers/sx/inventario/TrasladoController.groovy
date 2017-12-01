package sx.inventario


import grails.rest.*
import grails.converters.*


class TrasladoController extends RestfulController {
    static responseFormats = ['json', 'xml']
    TrasladoController() {
        super(Traslado)
    }

   @Override
    def index(Integer max){

        params.max = Math.min(max ?: 10, 100)
        def query  = Traslado.where {}.find()

        respond query.list(params)

    }

    @Override
    def show(){
        Traslado traslado=Traslado.get(params.id)
        respond traslado
    }






}
