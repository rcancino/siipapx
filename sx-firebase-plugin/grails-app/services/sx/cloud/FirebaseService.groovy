package sx.cloud

import javax.annotation.Nullable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Value

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.FirestoreException
import com.google.api.core.ApiFuture
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.*
import com.google.auth.oauth2.GoogleCredentials


import org.apache.commons.lang3.exception.ExceptionUtils

import grails.compiler.GrailsCompileStatic

@Slf4j
// @CompileDynamic
class FirebaseService {

    
    private FirebaseApp app

    @Value('${siipapx.firebase.url}')
    String firebaseUrl

    @Value('${siipapx.firebase.bucket}')
    String firebaseBucket

    @Value('${siipapx.firebase.projectId}')
    String projectId

	
    void initFirebase() {
    	// FileInputStream serviceAccount = new FileInputStream("/Users/rubencancino/Desktop/firebase/siipapx-436ce-firebase-adminsdk-ci4eg-779346f0c5.json");

		FirebaseOptions options = new FirebaseOptions.Builder()
  		.setCredentials(GoogleCredentials.getApplicationDefault())
  		.setDatabaseUrl(this.firebaseUrl)
        .setStorageBucket(this.firebaseBucket)
  		.build();

		app = FirebaseApp.initializeApp(options);
		log.info('Firebase APP: ', app)

    }
   

    Firestore getFirestore() {
        if(!this.app) {
            initFirebase()
        }
        return FirestoreClient.getFirestore()
    }


    FirebaseApp getFirebaseApp() {
        return this.app
    }

    void updateCollection(String collection, String id, Map changes) {
        try {
            ApiFuture<WriteResult> result = getFirestore()
            .collection(collection)
            .document(id)
            .set(changes, SetOptions.merge())
            def updateTime = result.get().getUpdateTime().toDate().format('dd/MM/yyyy')
            log.debug('{}/{} UPDATED at : {}', collection, id, updateTime)
        }
        catch(Exception ex) {
            def msg = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error actualizando {} DocId: {} , Msg: {}', collection, id, msg)
        }
    }

    void updateDocument(String docPath,  Map changes) {
        try {
            ApiFuture<WriteResult> result = getFirestore()
            .document(docPath)
            .set(changes, SetOptions.merge())
            def updateTime = result.get()
                .getUpdateTime()
                .toDate()
                .format('dd/MM/yyyy')
            log.debug('{}/{} UPDATED at : {}', collection, id, updateTime)
        }
        catch(Exception ex) {
            def msg = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error actualizando: {} DocId: {} , Msg: {}', docPath, msg)
        }
    }

	
}


