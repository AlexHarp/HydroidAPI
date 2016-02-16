package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.service.JenaService;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class JenaServiceImpl implements JenaService {
    @Autowired
    private HydroidConfiguration configuration;

    @Override
    public void storeRdf(InputStream rdfInput, String baseRdfUrl) {
        String serviceURI = configuration.getFusekiUrl();
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
        Model model = ModelFactory.createDefaultModel();
        model.read(rdfInput,baseRdfUrl);
        accessor.putModel(model);
    }
}
