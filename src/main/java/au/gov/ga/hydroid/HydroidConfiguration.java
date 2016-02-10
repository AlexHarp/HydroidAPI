package au.gov.ga.hydroid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by u24529 on 9/02/2016.
 */
@Configuration
@PropertySource(value = "classpath:application.properties")
public class HydroidConfiguration {

   @Value("${proxy.host}")
   private String proxyHost;

   @Value("${proxy.port}")
   private String proxyPort;

   @Value("${stanbol.chain}")
   private String stanbolChain;

   @Value("${stanbol.url}")
   private String stanbolUrl;

   @Value("${solr.collection}")
   private String solrCollection;

   @Value("${solr.url}")
   private String solrUrl;

   @Value("${s3.bucket}")
   private String s3Bucket;

   @Value("${s3.rdf.folder}")
   private String s3RDFFolder;

   public String getProxyHost() {
      return proxyHost;
   }

   public String getProxyPort() {
      return proxyPort;
   }

   public String getStanbolChain() {
      return stanbolChain;
   }

   public String getStanbolUrl() {
      return stanbolUrl;
   }

   public String getSolrCollection() {
      return solrCollection;
   }

   public String getSolrUrl() {
      return solrUrl;
   }

   public String getS3Bucket() {
      return s3Bucket;
   }

   public String getS3RDFFolder() {
      return s3RDFFolder;
   }

}
