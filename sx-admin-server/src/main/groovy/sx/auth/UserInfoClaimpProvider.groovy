package sx.security

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.security.core.userdetails.UserDetails 

import grails.plugin.springsecurity.rest.token.generation.jwt.CustomClaimProvider
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class UserInfoClaimpProvider  implements CustomClaimProvider{

    @Override
    void provideCustomClaims(JWTClaimsSet.Builder builder, UserDetails details, String principal, Integer expiration) {
        User.withNewSession{
            User user = User.where{username == details.username}.find()
            builder.claim('displayName', user.nombre)
            builder.claim('email', user.email)
        }
    }

}