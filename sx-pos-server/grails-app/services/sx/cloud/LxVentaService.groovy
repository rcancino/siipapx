package sx.cloud

import javax.annotation.Nullable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import javax.annotation.PostConstruct

import com.google.firebase.cloud.*
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Query
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.DocumentSnapshot

import com.google.cloud.firestore.ListenerRegistration 
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.EventListener;
import grails.gorm.transactions.Transactional



import sx.core.AppConfig
import sx.core.Sucursal
import sx.core.Cliente
import sx.core.Vendedor
import sx.core.Direccion
import sx.core.InstruccionCorte
import sx.core.Venta
import sx.core.VentaDet
import sx.core.Producto
import sx.logistica.CondicionDeEnvio



@Slf4j
@Transactional
class LxVentaService {

    FirebaseService firebaseService

    def dataSource

    static String TIME_FORMAT = 'dd/MM/yyyy HH:mm'
    

    def ventaCreate(){
       
        def suc = AppConfig.first().sucursal

        Firestore db = firebaseService.getFirestore()

        CollectionReference pedidos  = db.collection("pedidos");

        Query query = pedidos.whereEqualTo('status', 'CERRADO').whereEqualTo('sucursal', suc.nombre)
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        querySnapshot.get().getDocuments().each{pedidoSnapshot->

            Map<String,Object> pedido =  pedidoSnapshot.getData() 

            pedidoToVenta(pedido, suc, pedidoSnapshot)
          
        } 
        
    }

    def ventaCreate(DocumentChange documentChange) {
 
        def suc = AppConfig.first().sucursal

        DocumentSnapshot pedidoSnapshot =documentChange.getDocument()
        Map<String,Object> pedido = pedidoSnapshot.getData() 
        
        if(pedido.sucursal == suc.nombre && pedido.status == 'CERRADO'){

            pedidoToVenta(pedido, suc, pedidoSnapshot)
        
        }

    }

    def pedidoToVenta(Map<String,Object> pedido, Sucursal suc, def pedidoSnapshot) {

        // Creacion de la Venta
        Venta venta = crearVenta(pedido, suc)
        
        //Validar si tiene partidas para crear VentaDet
        if(pedido.partidas){
            pedido.partidas.each {partida ->

                // Crear Venta Det
                def ventaDet = crearVentaDet(partida, suc)
                
                // Validar si tiene corte para crear InstruccionDeCorte
                if(partida.corte){
                    
                    // Crear Intstruccion de corte
                    InstruccionCorte corte = creaInstruccionCorte(partida)
                    ventaDet.corte = corte
                    corte.ventaDet = ventaDet
                }
                
                venta.addToPartidas(ventaDet)
            }
        }
        
        
        // Validar si tiene envio para crear CondicionDeEnvio
        if(pedido.envio){
            def condicion = crearCondicionEnvio(pedido.envio)
            venta.envio = condicion
            condicion.venta = venta
        }
        
        venta.save failOnError:true,flush:true

        Map<String, Object> update = new HashMap<>();
        update.put("status", "FACTURABLE");
                    
        DocumentReference pedidoRef = pedidoSnapshot.getReference()
        ApiFuture<WriteResult> writeResult = pedidoRef.set(update, SetOptions.merge())  

    }

    


    def crearVenta(Map<String,Object> pedido, def sucursalLocal){

                   
     
       // def cliente = Cliente.findByNombre(pedido.nombre)
        def cliente = Cliente.get(pedido.cliente)
        def vendedor = Vendedor.findByNombres('CASA')
        def venta = new Venta()
try{


        venta.cargosPorManiobra = pedido.cargosPorManiobra
        venta.cfdiMail = pedido.cfdiMail
        venta.cliente = cliente
        venta.comentario = pedido.comentario
        venta.comisionTarjeta = pedido.comisionTarjeta
        venta.comisionTarjetaImporte = pedido.comisionTarjetaImporte
        venta.comprador = pedido.comprador
        venta.corteImporte = pedido.corteImporte
        venta.createUser = pedido.createUser
        venta.createUser = pedido.updateUser
        venta.descuento = pedido.descuento
        venta.descuentoOriginal = pedido.descuento
        venta.descuentoImporte = pedido.descuentoImporte
        venta.descuentoOriginal = pedido.descuento
        venta.documento = pedido.folio
        venta.fecha = pedido.fecha
        venta.formaDePago = pedido.formaDePago
        venta.importe = pedido.importe
        venta.impuesto = pedido.impuesto
        venta.kilos = pedido.kilos
        venta.moneda = Currency.getInstance(pedido.moneda)
        venta.tipo = pedido.tipo
        venta.nombre = pedido.nombre
        venta.subtotal = pedido.subtotal
        venta.total = pedido.total
        venta.tipoDeCambio = pedido.tipoDeCambio
        venta.usoDeCfdi = pedido.usoDeCfdi
        venta.sucursal  = sucursalLocal
        venta.documento = pedido.folio
        venta.sw2 = pedido.id
        venta.vendedor = vendedor
        venta.atencion='TELEFONICA'
        venta.callcenter = true
}catch(Exception e){
    e.printStackTrace()
}      
        return venta
    }



    def crearVentaDet(Map<String,Object> partida, def sucursalLocal ){
    
        Producto producto = Producto.get(partida.producto)

        def ventaDet = new VentaDet()

        ventaDet.sucursal = sucursalLocal
        ventaDet.total = partida.total
        ventaDet.cantidad = partida.cantidad
        ventaDet.precio = partida.precio
        ventaDet.precioOriginal = partida.precioOriginal
        ventaDet.kilos = partida.kilos
        ventaDet.descuentoOriginal = partida.descuentoOriginal
        ventaDet.importeCortes = partida.importeCortes
        ventaDet.descuentoImporte = partida.descuentoImporte
        ventaDet.nacional = partida.nacional
        ventaDet.precioLista = partida.precioLista
        ventaDet.comentario = partida.comentario
        ventaDet.producto  = producto
        ventaDet.impuesto = partida.impuesto
        ventaDet.subtotal = partida.subtotal
        ventaDet.impuestoTasa =  partida.impuestoTasa
        ventaDet.importe = partida.importe
        ventaDet.sw2 = partida.id
        
        return ventaDet
    }

    def creaInstruccionCorte(Map<String,Object> partida){
        
        InstruccionCorte corte = new InstruccionCorte()
        corte.cantidad = partida.corte.cantidad
        corte.precio = partida.corte.precio
        corte.instruccion = partida.corte.instruccion
        corte.refinado = partida.corte.refinado
        return corte
    }

    def crearCondicionEnvio(Map<String,Object> envio){
        
        def direccion = new Direccion()
        
        direccion.calle = envio.calle
        direccion.numeroInterior = envio.direccion.numeroInterior
        direccion.numeroExterior = envio.direccion.numeroExterior
        direccion.colonia = envio.direccion.colonia
        direccion.municipio = envio.direccion.municipio
        direccion.codigoPostal = envio.direccion.codigoPostal
        direccion.estado = envio.direccion.estado
        //direccion.pais='MEXICO'
        //direccion.latitud =  0
        //direccion.longitud = 0
        
        def condicion = new CondicionDeEnvio()
        
        condicion.direccion = direccion
        condicion.comentario = envio.comentario
        if(envio.fechaDeEntrega){
            condicion.fechaDeEntrega = envio.fechaDeEntrega.toDate()
        }
        condicion.condiciones = "Tipo: "+envio.tipo+" Contacto: "+envio.contacto+" Tel:"+envio.telefono+" Horario: "+envio.horario
        
        return condicion 
    
    }

    //@PostConstruct
    def listenerRegistration(){

        ListenerRegistration registration

    
        def suc = AppConfig.first().sucursal

        Firestore db = firebaseService.getFirestore()

		registration = db.collection("pedidos").whereEqualTo('sucursal', suc.nombre)
                .whereEqualTo('status', 'CERRADO')
				.addSnapshotListener(new EventListener<QuerySnapshot>() {
			@Override
			void onEvent(
					@Nullable QuerySnapshot snapshots,
					@Nullable FirestoreException ex) {
				if(ex) {
                    println "Error"
					String msg = ExceptionUtils.getRootCauseMessage(ex)
					println("Error: {}", msg, ex)
				}
				snapshots.getDocumentChanges().each { DocumentChange dc ->

					switch (dc.type) {
						case 'ADDED':
							println('Se agrego un pedido: ')
                            ventaCreate(dc)
							break
						case 'MODIFIED':
                            println('Se modifico un pedido Existente')
                            ventaCreate(dc)
							break
						case 'REMOVED':
							println('Se elimino un pedido')
							break
					}

				}
			}
		})
    }   

    def desRegistrarListener(){
        registration.remove()
    }

}
