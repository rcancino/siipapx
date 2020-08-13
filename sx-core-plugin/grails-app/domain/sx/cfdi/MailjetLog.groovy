package sx.cfdi

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * 
 + 
 */
@ToString(includeNames=true,includePackage=false )
@EqualsAndHashCode(includeFields = true, includes = ['id', 'target', 'messageId'])
class MailjetLog {
    
    String target
    String nombre
    String messageHref
    String messageUUID 
    String messageID
    String status
    int statusCode
    String messageErrors

    List<String> cfdis

    Date dateCreated
    Date lastUpdated
    String createUser
    String updateUser

    static hasMany = [cfdis: String]

    static constraints = {
        createUser nullable: true
        updateUser nullable: true
        messageHref nullable: true
        messageUUID  nullable: true
        messageID nullable: true
        status nullable: true
        messageErrors nullable: true
    }

    static mapping = {
        // cfdi index: 'MAILJET_LOG_IDX1'
        // uuid index: 'MAILJET_LOG_IDX2'
        cfdis joinTable: [name: 'mailjetlog_cfdis',
                           key: 'log_id',
                           column: 'cfdi',
                           type: "text"]
    }

}

