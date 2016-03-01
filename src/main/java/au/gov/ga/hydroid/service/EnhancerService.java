package au.gov.ga.hydroid.service;

/**
 * Created by u24529 on 3/02/2016.
 */
public interface EnhancerService {

   public boolean enhance(String title, String content, String docType, String origin);

   public void enhanceDocuments();
   public void enhanceDatasets();
   public void enhanceModels();
   public void enhanceImages();

}
