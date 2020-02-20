package sx.cloud

import javax.annotation.Nullable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.DocumentSnapshot


import com.google.cloud.firestore.FirestoreException


import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.*
import com.google.auth.oauth2.GoogleCredentials


import org.apache.commons.lang3.exception.ExceptionUtils

import grails.compiler.GrailsCompileStatic

@Slf4j
@CompileDynamic
class FirebaseService {

    
    private FirebaseApp app

	
    void initFirebase() {
    	// FileInputStream serviceAccount = new FileInputStream("/Users/rubencancino/Desktop/firebase/siipapx-436ce-firebase-adminsdk-ci4eg-779346f0c5.json");

		FirebaseOptions options = new FirebaseOptions.Builder()
  		.setCredentials(GoogleCredentials.getApplicationDefault())
  		.setDatabaseUrl("https://siipapx-436ce.firebaseio.com")
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

	
}


