package au.gov.ga.hydroid.service;

import com.hp.hpl.jena.query.DatasetAccessorFactory;

import java.io.InputStream;

/**
 * Created by Layoric on 16/02/2016.
 */
public interface JenaService {
    void storeRdf(InputStream rdfInput,String baseRdfUrl);
}
