package cloudgene.mapred.server.auth;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.security.token.jwt.encryption.EncryptionConfiguration;
import io.micronaut.security.token.jwt.signature.SignatureConfiguration;
import io.micronaut.security.token.jwt.validator.GenericJwtClaimsValidator;
import io.micronaut.security.token.jwt.validator.JwtAuthenticationFactory;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import jakarta.inject.Singleton;

import java.util.Collection;

@Factory
public class JwtConfiguration {

    @Singleton
    @Requires(beans = {SignatureConfiguration.class, JwtAuthenticationFactory.class})
    @SuppressWarnings("deprecation")
    public JwtTokenValidator jwtTokenValidator(
            Collection<SignatureConfiguration> signatures,
            Collection<EncryptionConfiguration> encryptions,
            Collection<GenericJwtClaimsValidator> genericJwtClaimsValidators,
            JwtAuthenticationFactory jwtAuthenticationFactory) {
        return new JwtTokenValidator(signatures, encryptions, genericJwtClaimsValidators, jwtAuthenticationFactory);
    }
}
