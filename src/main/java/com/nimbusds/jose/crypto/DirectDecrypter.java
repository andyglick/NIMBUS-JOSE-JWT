package com.nimbusds.jose.crypto;


import java.io.UnsupportedEncodingException;

import javax.crypto.SecretKey;

import org.bouncycastle.util.Arrays;

import com.nimbusds.jose.DefaultJWEHeaderFilter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEHeaderFilter;
import com.nimbusds.jose.ReadOnlyJWEHeader;
import com.nimbusds.jose.util.Base64URL;


/**
 * Direct decrypter of {@link com.nimbusds.jose.JWEObject JWE objects} with a
 * shared symmetric key. This class is thread-safe.
 *
 * <p>Supports the following JWE algorithms:
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.JWEAlgorithm#DIR}
 * </ul>
 *
 * <p>Supports the following encryption methods:
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128CBC_HS256}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256CBC_HS512}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128GCM}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256GCM}
 * </ul>
 *
 * <p>Accepts all {@link com.nimbusds.jose.JWEHeader#getReservedParameterNames
 * reserved JWE header parameters}. Modify the {@link #getJWEHeaderFilter
 * header filter} properties to restrict the acceptable JWE algorithms, 
 * encryption methods and header parameters, or to allow custom JWE header 
 * parameters.
 * 
 * @author Vladimir Dzhuvinov
 * @version $version$ (2013-04-16)
 *
 */
public class DirectDecrypter extends DirectCryptoProvider implements JWEDecrypter {


	/**
	 * The JWE header filter.
	 */
	private final DefaultJWEHeaderFilter headerFilter;


	/**
	 * Creates a new direct decrypter.
	 *
	 * @param key The shared symmetric key. Must be 128 bits (16 bytes),
	 *            256 bits (32 bytes) or 512 bits (64 bytes) long. Must not 
	 *            be {@code null}.
	 *
	 * @throws JOSEException If the key length is unexpected.
	 */
	public DirectDecrypter(final byte[] key)
		throws JOSEException {

		super(key);

		headerFilter = new DefaultJWEHeaderFilter(supportedAlgorithms(), supportedEncryptionMethods());
	}


	@Override
	public JWEHeaderFilter getJWEHeaderFilter() {

		return headerFilter;
	}


	@Override
	public byte[] decrypt(final ReadOnlyJWEHeader readOnlyJWEHeader,
		              final Base64URL encryptedKey,
		              final Base64URL iv,
		              final Base64URL cipherText,
		              final Base64URL integrityValue) 
		throws JOSEException {

		// Validate required JWE parts
		if (encryptedKey != null) {

			throw new JOSEException("Unexpected encrypted key, must be omitted");
		}	

		if (iv == null) {

			throw new JOSEException("The initialization vector (IV) must not be null");
		}

		if (integrityValue == null) {

			throw new JOSEException("The integrity value must not be null");
		}
		

		JWEAlgorithm alg = readOnlyJWEHeader.getAlgorithm();

		if (! alg.equals(JWEAlgorithm.DIR)) {

			throw new JOSEException("Unsupported algorithm, must be \"dir\"");
		}

	    	EncryptionMethod enc = readOnlyJWEHeader.getEncryptionMethod();

	    	byte[] plainText;

	    	if (enc.equals(EncryptionMethod.A128CBC_HS256) || enc.equals(EncryptionMethod.A256CBC_HS512)    ) {

	    		byte[] epu = null;

			if (readOnlyJWEHeader.getEncryptionPartyUInfo() != null) {

				epu = readOnlyJWEHeader.getEncryptionPartyUInfo().decode();
			}

			byte[] epv = null;
			
			if (readOnlyJWEHeader.getEncryptionPartyVInfo() != null) {

				epv = readOnlyJWEHeader.getEncryptionPartyVInfo().decode();
			}

	    		SecretKey cek = ConcatKDF.generateCEK(cmk, enc, epu, epv);

			plainText = AESCBC.decrypt(cek, iv.decode(), cipherText.decode());

			SecretKey cik = ConcatKDF.generateCIK(cmk, enc, epu, epv);

			String macInput = readOnlyJWEHeader.toBase64URL().toString() + "." +
			                  /* encryptedKey omitted */ "." +
			                  iv.toString() + "." +
			                  cipherText.toString();

			byte[] mac = HMAC.compute(cik, macInput.getBytes());

			if (! Arrays.constantTimeAreEqual(integrityValue.decode(), mac)) {

				throw new JOSEException("HMAC integrity check failed");
			}

	    	} else if (enc.equals(EncryptionMethod.A128GCM) || enc.equals(EncryptionMethod.A256GCM)    ) {

	    		// Compose the additional authenticated data
			String authDataString = readOnlyJWEHeader.toBase64URL().toString() + "." +
						/* encryptedKey omitted */ "." +
						iv.toString();

			byte[] authData = null;

			try {
				authData = authDataString.getBytes("UTF-8");

			} catch (UnsupportedEncodingException e) {

				throw new JOSEException(e.getMessage(), e);
			}

			plainText = AESGCM.decrypt(cmk, iv.decode(), cipherText.decode(), authData, integrityValue.decode());

	    	} else {

	    		throw new JOSEException("Unsupported encryption method, must be A128CBC_HS256, A256CBC_HS512, A128GCM or A128GCM");
	    	}


	    	// Apply decompression if requested
	    	return DeflateHelper.applyDecompression(readOnlyJWEHeader, plainText);
	}
}

