package mvcp.adobe.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Object wrapper for a Response stored in Cache.
 * Contains the response and the cache creation date (in ISO-8601 format)
 *
 * @author      Marcelo Pereira
 * @version     1.0.0
 * @since       2019-06-10
 */
public class CacheItem {
    public static final Logger logger = (Logger) LoggerFactory.getLogger(CacheItem.class);
    private Response response;
    private String isoCreated;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public CacheItem () {}

    public CacheItem(Response response) {
        this.response = response;
        this.isoCreated = (new SimpleDateFormat(DATE_FORMAT)).format(new Date());
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getIsoCreated() {
        return isoCreated;
    }

    public void setIsoCreated(String created) {
        this.isoCreated = created;
    }

    /**
     * Transforms the ISO 8601 date representation into a Java Date object
     *
     * @return Date Date object converted from a ISO 8601 string
     * @throws ParseException Thrown in case the string is invalid
     */
    public Date getCreated() throws ParseException {
        Date date =(new SimpleDateFormat(DATE_FORMAT)).parse(isoCreated);
        return date;
    }
}
