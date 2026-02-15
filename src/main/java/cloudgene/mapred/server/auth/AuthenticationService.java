package cloudgene.mapred.server.auth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.RandomStringUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.ApiToken;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.responses.ValidatedApiTokenResponse;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class AuthenticationService {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

	private static final String MESSAGE_VALID_API_TOKEN = "API Token was created by %s and is valid.";

	private static final String MESSAGE_INVALID_API_TOKEN = "Invalid API Token.";

	@Inject
	protected Application application;

	@Inject
	protected JwtTokenGenerator generator;

	@Inject
	@Nullable
	protected JwtTokenValidator validator;

	public static String ATTRIBUTE_TOKEN_TYPE = "token_type";

	public static String ATTRIBUTE_API_HASH = "api_hash";

	public User getUserByAuthentication(Authentication authentication) {
		return getUserByAuthentication(authentication, AuthenticationType.ACCESS_TOKEN);
	}

	public User getUserByAuthentication(Authentication authentication, AuthenticationType authenticationType) {

		User user = null;
		if (authentication != null) {
			UserDao userDao = new UserDao(application.getDatabase());
			user = userDao.findByUsername(authentication.getName());
			Map<String, Object> attributes = authentication.getAttributes();

			if (attributes.containsKey(ATTRIBUTE_TOKEN_TYPE)) {

				String tokenType = attributes.get(ATTRIBUTE_TOKEN_TYPE).toString();

				if (tokenType.equalsIgnoreCase(AuthenticationType.API_TOKEN.toString())) {

					if (authenticationType == AuthenticationType.API_TOKEN
							|| authenticationType == AuthenticationType.ALL_TOKENS) {
						if (user.getApiToken().equals(attributes.get(ATTRIBUTE_API_HASH))) {
							user.setAccessedByApi(true);
							return user;
						}
					}

				} else if (tokenType.equalsIgnoreCase(AuthenticationType.ACCESS_TOKEN.toString())) {

					if (authenticationType == AuthenticationType.ACCESS_TOKEN
							|| authenticationType == AuthenticationType.ALL_TOKENS) {
						return user;
					}

				}

			} else {

				if (authenticationType == AuthenticationType.ACCESS_TOKEN
						|| authenticationType == AuthenticationType.ALL_TOKENS) {
					return user;
				}

			}

			throw new AuthorizationException(authentication);

		}

		throw new AuthenticationException();

	}

	public ApiToken createApiToken(User user, int lifetime) {

		String hash = RandomStringUtils.randomAlphanumeric(30);

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(ATTRIBUTE_TOKEN_TYPE, AuthenticationType.API_TOKEN.toString());
		attributes.put(ATTRIBUTE_API_HASH, hash);
		// addition attributes that are needed by imputationbot
		attributes.put("username", user.getUsername());
		attributes.put("name", user.getFullName());
		attributes.put("mail", user.getMail());
		attributes.put("api", true);

		Authentication authentication2 = Authentication.build(user.getUsername(), attributes);
		Optional<String> token = generator.generateToken(authentication2, lifetime);

		Date expiresOn = new Date(System.currentTimeMillis() + (lifetime * 1000L));

		return new ApiToken(token.get(), hash, expiresOn);

	}

	public Mono<ValidatedApiTokenResponse> validateApiToken(String token) {

		if (validator == null) {
			log.error("JWT Token Validator is null - cannot validate API tokens");
			return Mono.just(ValidatedApiTokenResponse.error("JWT Token Validator not available"));
		}

		log.debug("Validating API token");
		Publisher<Authentication> authentication = validator.validateToken(token, null);

		return Mono.<ValidatedApiTokenResponse>create(emitter -> {

			authentication.subscribe(new Subscriber<Authentication>() {

				private Subscription subscription;

				@Override
				public void onComplete() {
					// handle empty publisher. e.g. when token is invalid
					log.debug("Token validation completed with no authentication");
					emitter.success(ValidatedApiTokenResponse.error(MESSAGE_INVALID_API_TOKEN));
				}

				@Override
				public void onError(Throwable throwable) {					log.error("Token validation error: {}", throwable.getMessage(), throwable);
					emitter.error(throwable);
				}

				@Override
				public void onNext(Authentication authentication) {
					try {
						log.debug("Token validated, attempting to get user. Auth name: {}, attributes: {}", 
								authentication.getName(), authentication.getAttributes());
						User user = getUserByAuthentication(authentication, AuthenticationType.API_TOKEN);
						if (user == null) {
							log.debug("User not found or authorization failed");
							emitter.success(ValidatedApiTokenResponse.error(MESSAGE_INVALID_API_TOKEN));
						} else {
							log.debug("User found: {}", user.getUsername());
							ValidatedApiTokenResponse response = ValidatedApiTokenResponse
									.valid(MESSAGE_VALID_API_TOKEN, user);
							response.setExpire((Date) authentication.getAttributes().get("exp"));
							emitter.success(response);
						}
					} catch (Exception e) {
						log.error("Exception during token validation: {}", e.getMessage(), e);
						emitter.success(ValidatedApiTokenResponse.error(MESSAGE_INVALID_API_TOKEN));
					}
					subscription.request(1);
				}

				@Override
				public void onSubscribe(Subscription subscription) {
					this.subscription = subscription;
					subscription.request(1);
				}

			});
		}).single();

	}

}
