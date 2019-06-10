package mvcp.adobe.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Object wrapper for a Response stored in Cache.
 * Contains the response and the cache creation date
 *
 */
public class CacheItem {
    private Response response;
    private String isoCreated;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public CacheItem(Response response, Date created) {
        this.response = response;
        this.isoCreated = (new SimpleDateFormat(DATE_FORMAT)).format(created);
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

    public Date getCreated() throws ParseException {
        return (new SimpleDateFormat(DATE_FORMAT)).parse(isoCreated);
    }
}
