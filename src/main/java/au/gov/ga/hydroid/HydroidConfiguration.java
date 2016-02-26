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
   private int proxyPort;

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

   @Value("${s3.enhancer.input}")
   private String s3EnhancerInput;

   @Value("${s3.enhancer.output}")
   private String s3EnhancerOutput;

   @Value("${fuseki.url}")
   private String fusekiUrl;

   public String getProxyHost() {
      return proxyHost;
   }

   public int getProxyPort() {
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

   public String getS3EnhancerInput() {
      return s3EnhancerInput;
   }

   public String getS3EnhancerOutput() {
      return s3EnhancerOutput;
   }

   public String getFusekiUrl() { return fusekiUrl; }

}
