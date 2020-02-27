package sx.cloud


import groovy.util.logging.Slf4j

import org.springframework.scheduling.annotation.Scheduled

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import grails.compiler.GrailsCompileStatic
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.model.PersistentEntity
import grails.util.Environment

import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.api.core.ApiFuture


import org.apache.commons.lang3.exception.ExceptionUtils

import sx.core.Existencia

@Slf4j
// @GrailsCompileStatic
// @Transactional
class LxExistenciaService {

	FirebaseService firebaseService

	String getId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof Existencia ) {
            return ((Existencia) event.entityObject).id
        }
        null
    }

    Existencia getExistencia(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof Existencia ) {
            return (Existencia) event.entityObject
        }
        null
    }


    @Subscriber
    void afterUpdate(PostUpdateEvent event) {	
        Existencia exis = getExistencia(event)
        if ( exis ) {
            log.info('{} {} Exis: {} ({})', event.eventType.name(), event.entity.name, exis.clave, exis.sucursalNombre)
            String id = exis.producto.id
            Map data = [
                almacen: exis.sucursalNombre,
                cantidad: exis.cantidad as Long,
                recorte: exis.recorte as Long,
                recorteComentario: exis.recorteComentario
            ]
            updateFirebase(exis)
        }
    }

   	void updateFirebase(Existencia exis) {
        try {
            
            Map data = [
                almacen: exis.sucursalNombre,
                cantidad: exis.cantidad as Long,
                recorte: exis.recorte as Long,
                recorteComentario: exis.recorteComentario
            ]

        	String id = exis.producto.id
        	String collection = 'exis'
        	DocumentReference docRef =  firebaseService.getFirestore().document("${collection}/${id}")
        	DocumentSnapshot snapShot = docRef.get().get()

        	ApiFuture<WriteResult> result = null

        	if (!snapShot.exists()) {
                    
                Map<String,Object> exist = [
                    id: exis.producto.id,
                    clave: exis.producto.clave, 
                    descripcion: exis.producto.descripcion,
                    producto: exis.producto.id,
                    ejercicio: exis.anio as Integer,
                    mes: exis.mes as Integer
                    ]
                docRef.set(exist)

                result = docRef
                    .collection('almacenes')
                    .document(data.almacen)
                    .set(data)
            } else {
         
                result = docRef
                    .collection('almacenes')
                    .document(data.almacen)
                    .set(data)
                // return null
            }
        	
            def updateTime = result.get()
                .getUpdateTime()
                .toDate()
                .format('dd/MM/yyyy')
            log.info('{}/{}/almacenes/{} ({}) UPDATED at : {}', collection, id, data.almacen, exis.clave, updateTime)
        }

        catch(Exception ex) {
            def msg = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error actualizando firebase , Msg: {}', msg)
        }
    }


}
