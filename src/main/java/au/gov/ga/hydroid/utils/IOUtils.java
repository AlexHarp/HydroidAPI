package au.gov.ga.hydroid.utils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by u24529 on 17/02/2016.
 */
public class IOUtils {

   public static byte[] fromInputStreamToByteArray(InputStream input) {
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
      } catch (IOException e) {
         throw new HydroidException(e);
      } finally {
         try {
            baos.close();
         } catch (IOException e) {
            throw new HydroidException(e);
         }
      }
      return output;
   }

   public static String parseFile(InputStream stream) {
      return parseFile(stream, null);
   }

   public static String parseFile(InputStream stream, Metadata metadata) {
      AutoDetectParser parser = new AutoDetectParser();
      BodyContentHandler handler = new BodyContentHandler(-1);
      try {
         if (metadata == null) {
            metadata = new Metadata();
         }
         parser.parse(stream, handler, metadata);
      } catch (Throwable e) {
         throw new HydroidException(e);
      }
      return handler.toString();
   }

   public static void sendResponseError(HttpServletResponse response, int errorCode) {
      try {
         response.sendError(errorCode);
      } catch (Throwable e) {
         throw new HydroidException(e);
      }
   }

}
