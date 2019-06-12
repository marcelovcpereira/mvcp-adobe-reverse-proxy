package mvcp.adobe.components;

import com.google.gson.Gson;
import mvcp.adobe.abstractions.ISerializer;
import mvcp.adobe.entities.CacheItem;
import mvcp.adobe.entities.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Utility class for serialization/deserialization of objects.
 * Applies MD5 hashing for "hash" methods.
 * For "serialize/deserialize" methods, encodes in JSON.
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-10
 */
@Component
public class MD5Serializer implements ISerializer {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(MD5Serializer.class);

    public MD5Serializer(){}

    /**
     * Encode a Request into a hash string
     *
     * @param request Request to be serialized
     * @return String A hash representation of the request
     */
    @Override
    public String hashRequest(Request request) {
        String s = new Gson().toJson(request);
        Request clone = new Gson().fromJson(s, Request.class);
        HashMap<String,String> headers = new HashMap<>();
        headers.put("host", request.getHostHeader());
        clone.setHeaders(headers);
        s = new Gson().toJson(clone);
        return getMd5(s);
    }


    /**
     * Deserializes a CacheItem from json string
     *
     * @param hash Serialized cached item
     * @return CacheItem Deserialized object
     */
    @Override
    public CacheItem deserializeCacheItem(String hash) {
        return new Gson().fromJson(hash, CacheItem.class);
    }

    /**
     * Generates a json string of a CacheItem for being stored in the Cache.
     *
     * @param item Item to be cached
     * @return String A hash representation of the object to be cached
     */
    @Override
    public String serializeCacheItem(CacheItem item) {
        return new Gson().toJson(item);
    }


    /**
     * Hashes a string into a MD5 hex string
     * Ref: https://www.geeksforgeeks.org/md5-hash-in-java/
     *
     * @param input String to be hashed
     * @return String A MD5 Hash representation of the string
     */
    public static String getMd5(String input) {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
