package au.gov.ga.hydroid.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by u24529 on 17/02/2016.
 */
public class IOUtils {

   public static byte[] fromInputStreamToByteArray(InputStream input) throws IOException {
      byte[] output = null;
      ByteArrayOutputStream baos = null;
      try {
         baos = new ByteArrayOutputStream();
         int bytesRead = 0;
         byte[] buffer = new byte[4096];
         while ((bytesRead = input.read(buffer)) > 0) {
            baos.write(buffer, 0, bytesRead);
         }
         output = baos.toByteArray();
      } finally {
         baos.close();
      }
      return output;
   }

}
