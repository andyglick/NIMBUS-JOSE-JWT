package com.nimbusds.jose.crypto;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;


/**
 * The base abstract class for RSA encrypters and decrypters of
 * {@link com.nimbusds.jose.JWEObject JWE objects}.
 *
 * <p>Supports the following JSON Web Algorithms (JWAs):
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.JWEAlgorithm#RSA1_5}
 *     <li>{@link com.nimbusds.jose.JWEAlgorithm#RSA_OAEP}
 * </ul>
 *
 * <p>Supports the following encryption methods:
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128CBC_HS256}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A192CBC_HS384}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256CBC_HS512}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128GCM}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A192GCM}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256GCM}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128CBC_HS256_DEPRECATED}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256CBC_HS512_DEPRECATED}
 * </ul>
 * 
 * @author David Ortiz
 * @author Vladimir Dzhuvinov
 * @version $version$ (2014-05-23)
 */
abstract class RSACryptoProvider extends BaseJWEProvider {


	/**
	 * The supported JWE algorithms.
	 */
	public static final Set<JWEAlgorithm> SUPPORTED_ALGORITHMS;


	/**
	 * The supported encryption methods.
	 */
	public static final Set<EncryptionMethod> SUPPORTED_ENCRYPTION_METHODS;


	/**
	 * Initialises the supported algorithms and encryption methods.
	 */
	static {

		Set<JWEAlgorithm> algs = new HashSet<JWEAlgorithm>();
		algs.add(JWEAlgorithm.RSA1_5);
		algs.add(JWEAlgorithm.RSA_OAEP);
		SUPPORTED_ALGORITHMS = Collections.unmodifiableSet(algs);

		Set<EncryptionMethod> methods = new HashSet<EncryptionMethod>();
		methods.add(EncryptionMethod.A128CBC_HS256);
		methods.add(EncryptionMethod.A192CBC_HS384);
		methods.add(EncryptionMethod.A256CBC_HS512);
		methods.add(EncryptionMethod.A128GCM);
		methods.add(EncryptionMethod.A192GCM);
		methods.add(EncryptionMethod.A256GCM);
		methods.add(EncryptionMethod.A128CBC_HS256_DEPRECATED);
		methods.add(EncryptionMethod.A256CBC_HS512_DEPRECATED);
		SUPPORTED_ENCRYPTION_METHODS = Collections.unmodifiableSet(methods);
	}


	/**
	 * Creates a new RSA encryption / decryption provider.
	 */
	protected RSACryptoProvider() {

		super(SUPPORTED_ALGORITHMS, SUPPORTED_ENCRYPTION_METHODS);
	}
}
