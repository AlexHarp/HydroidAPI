package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.ImageService;
import au.gov.ga.hydroid.service.JenaService;
import org.apache.jena.rdf.model.Statement;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * Created by u24529 on 26/02/2016.
 */
@Service
public class ImageServiceImpl implements ImageService {

   @Autowired
   private JenaService jenaService;

   @Override
   public String extractRDFString(InputStream is) {
      String rdfString;

      try {
         // Convert image content to String and extract RDF data
         byte[] bytes = IOUtils.toByteArray(is);
         String imageString = new String(bytes);
         rdfString = imageString.substring(imageString.indexOf("<rdf:RDF"));
         rdfString = rdfString.substring(0, rdfString.indexOf("</x:xmpmeta>"));
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }

      return rdfString;
   }


   @Override
   public String getImageMetadata(InputStream is) {
      StringBuilder metadata = new StringBuilder();

      try {
         // Convert image content to String and extract RDF data
         String rdfInput = extractRDFString(is);

         // Parse RDF into a list of Statements
         List<Statement> triples = jenaService.parseRdf(rdfInput, "");

         // Collect unique object values and add them to the result
         if (triples != null) {
            String objectValue;
            for (Statement statement : triples) {
               if (statement.getObject().isLiteral()
                     && metadata.indexOf(statement.getObject().asLiteral().getString()) < 0) {
                  objectValue = statement.getObject().asLiteral().getString();
                  metadata.append(objectValue).append("\n");
               }
            }
         }
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }

      return metadata.toString();
   }

}
