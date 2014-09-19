package net.wholook.wmessage.api;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wholook on 14. 9. 17..
 */
public class WholookURLWithParams {
    public String url;
    public List<NameValuePair> nameValuePairs;

    public WholookURLWithParams()
    {
        nameValuePairs = new ArrayList<NameValuePair>();
    }
}
