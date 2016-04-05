package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.UrlContentParser;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Created by u24529 on 05/04/2016.
 */
@Service("tikaParser")
public class TikaContentParser implements UrlContentParser {

   @Override
   public String parseUrl(String url, Metadata metadata) {
      InputStream inputStream = IOUtils.getUrlContent(url);
      IOUtils.parseFile(inputStream, metadata);
      return metadata.get("description");
   }

}
