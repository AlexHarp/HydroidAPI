package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.dto.ImageMetadata;

import java.io.InputStream;

/**
 * Created by u24529 on 25/02/2016.
 */
public interface ImageService {

   public ImageMetadata getImageMetadata(InputStream is);
   public ImageMetadata describeImage(InputStream is);

}
