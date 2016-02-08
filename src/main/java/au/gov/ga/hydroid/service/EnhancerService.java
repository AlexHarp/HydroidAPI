package au.gov.ga.hydroid.service;

/**
 * Created by u24529 on 3/02/2016.
 */
public interface EnhancerService {

   public void enhance(String chainName, String title, String content, String solrCollection) throws Exception;

}
