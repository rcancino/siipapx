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
import sx.cxc.SolicitudDeDeposito
import sx.cxc.Cobro
import sx.cxc.CobroTransferencia
import sx.cxc.CobroDeposito
import sx.core.Cliente
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco


@Slf4j
@Transactional
class LxCobroService {

    FirebaseService firebaseService

    def dataSource

    static String TIME_FORMAT = 'dd/MM/yyyy HH:mm'

    def cobroCreate(){
        Sucursal sucursal = AppConfig.first().sucursal
        Firestore db = firebaseService.getFirestore()
        CollectionReference depositos  = db.collection("depositos");
        Query query = depositos.whereEqualTo('estado', 'AUTORIZADO').whereEqualTo('sucursal', sucursal.nombre).whereEqualTo('atendido', false)
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        querySnapshot.get().getDocuments().each{depositoSnapshot->
            Map<String,Object> deposito =  depositoSnapshot.getData()
            println deposito
            def solicitud = crearSolicitudDeposito(deposito)
            crearCobro(solicitud)
            /*
            Map<String, Object> update = new HashMap<>();
            update.put("atendido", true);
                    
            DocumentReference depositoRef = depositoSnapshot.getReference()
            ApiFuture<WriteResult> writeResult = depositoRef.set(update, SetOptions.merge()) 
            */ 
        } 
    }

    def cobroCreate(DocumentChange documentChange) {
 
        println "Generando el cobro desde el documento change"

        def suc = AppConfig.first().sucursal

        DocumentSnapshot depositoSnapshot =documentChange.getDocument()
        Map<String,Object> deposito = depositoSnapshot.getData() 

            depositoFbToDeposito(deposito, depositoSnapshot)


    }

    def depositoFbToDeposito(Map<String,Object> deposito, def depositoSnapshot){

        println "Dentro del fb"
        def solicitud = crearSolicitudDeposito(deposito)
        def cobro = crearCobro(solicitud)
        if(cobro){
            println "atendiendo"
            Map<String, Object> update = new HashMap<>();
            update.put("atendido", true);
                    
            DocumentReference depositoRef = depositoSnapshot.getReference()
            ApiFuture<WriteResult> writeResult = depositoRef.set(update, SetOptions.merge()) 
        }

    }
    
    def crearSolicitudDeposito(Map<String,Object> depositoFb){
        if(depositoFb){
            Cliente cliente = Cliente.get(depositoFb.cliente.id) 
            Banco banco = Banco.get(depositoFb.banco.id)
            CuentaDeBanco cuenta = CuentaDeBanco.get(depositoFb.cuenta.id)
            def suc = AppConfig.first().sucursal
            
            SolicitudDeDeposito sol = new SolicitudDeDeposito()    
            sol.sucursal =  suc
            sol.cliente = cliente
            sol.banco = banco
            sol.cuenta = cuenta
            sol.folio = depositoFb.folio
            sol.total = depositoFb.total
            // sol.fecha = depositoFb.fecha
            sol.fecha = Date.parse("yyyy-MM-dd'T'HH:mm:ss", depositoFb.fecha)
            //sol.fechaDeposito = depositoFb.fechaDeposito
            sol.fechaDeposito =  Date.parse("yyyy-MM-dd'T'HH:mm:ss", depositoFb.fechaDeposito)
            sol.referencia = depositoFb.referencia
            sol.comentario = depositoFb.estado
            sol.sw2 = depositoFb.id
            if(depositoFb.transferencia){
                sol.transferencia = depositoFb.total            
            }
            if(depositoFb.importes.cheque > 0){
                sol.cheque =  depositoFb.importes.cheque  
            }
            if(depositoFb.importes.efectivo > 0){
                sol.efectivo =  depositoFb.importes.efectivo  
            }
            if(depositoFb.importes.tarjeta > 0){
                sol.tarjeta =  depositoFb.importes.tarjeta 
            }
            
            sol.save failOnError:true, flush:true
            return sol
        }

    }

    def crearCobro(SolicitudDeDeposito solicitud){
        println "Generando el cobro"
        if(solicitud){
            Cobro cobro = new Cobro()
            cobro.cliente = solicitud.cliente
            cobro.sucursal = solicitud.sucursal
            cobro.tipo = 'CON'
            cobro.fecha = solicitud.fecha
            cobro.importe = solicitud.total
            cobro.referencia = solicitud.referencia
            if(solicitud.transferencia > 0){
                cobro.formaDePago = 'TRANSFERENCIA'
                CobroTransferencia transf = new CobroTransferencia()
                transf.bancoOrigen = solicitud.banco
                transf.cuentaDestino = solicitud.cuenta
                transf.folio = solicitud.folio
                transf.fechaDeposito = solicitud.fechaDeposito
                transf.cobro = cobro
                cobro.transferencia = transf
            }
            if(solicitud.cheque > 0 ){
                cobro.formaDePago = 'DEPOSITO_CHEQUE'
                CobroDeposito depo = new CobroDeposito()
                depo.bancoOrigen = solicitud.banco
                depo.cuentaDestino = solicitud.cuenta
                depo.folio = solicitud.folio
                depo.fechaDeposito = solicitud.fechaDeposito
                depo.totalCheque = solicitud.cheque
                depo.cobro = cobro
                cobro.deposito = depo
            }
            if(solicitud.efectivo > 0){
                cobro.formaDePago = 'DEPOSITO_EFECTIVO'
                CobroDeposito depo = new CobroDeposito()
                depo.bancoOrigen = solicitud.banco
                depo.cuentaDestino = solicitud.cuenta
                depo.folio = solicitud.folio
                depo.fechaDeposito = solicitud.fechaDeposito
                depo.totalEfectivo = solicitud.efectivo
                depo.cobro = cobro
                cobro.deposito = depo
            }
            
            cobro.save failOnError:true, flush:true
            solicitud.cobro = cobro
            solicitud.save failOnError:true, flush:true
        }
    }


        //@PostConstruct
    def listenerRegistration(){

        ListenerRegistration registration

    
        def sucursal = AppConfig.first().sucursal

        Firestore db = firebaseService.getFirestore()

		registration = db.collection("depositos").whereEqualTo('estado', 'AUTORIZADO').whereEqualTo('sucursal', sucursal.nombre).whereEqualTo('atendido', false)
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
							println('Se agrego un deposito: ')
                            cobroCreate(dc)
							break
						case 'MODIFIED':
                            println('Se modifico un deposito Existente')
                            // cobroCreate(dc)
							break
						case 'REMOVED':
							println('Se elimino un deposito')
							break
					}

				}
			}
		})
    } 

}
