package au.gov.ga.hydroid.service;

import org.apache.tika.metadata.Metadata;

import java.io.InputStream;

/**
 * Created by u24529 on 31/03/2016.
 */
public interface UrlContentParser {

   String parseUrl(String url, Metadata metadata);

}
