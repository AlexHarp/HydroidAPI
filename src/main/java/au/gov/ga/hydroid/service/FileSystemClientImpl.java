package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.service.impl.DataObjectSummaryImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSystemClientImpl implements S3Client {
   @Override
   public String getAccountOwner() {
      return null;
   }

   private void ensureDirectoriesExist(String bucketName, String key) {
      File file = new File(bucketName + "/" + key);
      Path parentDir = file.toPath().getParent();
      if (!Files.exists(parentDir)) {
         try {
            Files.createDirectories(parentDir);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   @Override
   public InputStream getFile(String bucketName, String key) {
      InputStream result = null;
      try {
         result = FileUtils.openInputStream(new File(bucketName + "/" + key));
      } catch (IOException e) {
         e.printStackTrace();
         try {
            result.close();
         } catch (IOException e1) {
            e1.printStackTrace();
         }
      }
      return result;
   }

   @Override
   public byte[] getFileAsByteArray(String bucketName, String key) {
      byte[] result = null;
      InputStream is = null;
      try {
         is = FileUtils.openInputStream(new File(bucketName + "/" + key));
         result = IOUtils.toByteArray(is);
      } catch (IOException e) {
         e.printStackTrace();
      }
      finally {
         if(is != null)
            try {
               is.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
      }
      return result;
   }

   @Override
   public void storeFile(String bucketName, String key, String content, String contentType) {
      try {
         ensureDirectoriesExist(bucketName,key);
         Files.write(new File(bucketName + "/" + key).toPath(), Collections.singletonList(content), Charset.forName("UTF-8"));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void storeFile(String bucketName, String key, InputStream content, String contentType) {
      try {
         ensureDirectoriesExist(bucketName,key);
         Files.write(new File(bucketName + "/" + key).toPath(), IOUtils.toByteArray(content));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void deleteFile(String bucketName, String key) {
      try {
         Files.delete(new File(bucketName + "/" + key).toPath());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public List<DataObjectSummary> listObjects(String bucketName, String key) {
      List<DataObjectSummary> result = new ArrayList<>();
      File fileRoot = new File(bucketName + "/" + key);
      if(fileRoot.listFiles() == null) {
         return result;
      }
      for(File file : fileRoot.listFiles()) {
        String addKey = file.getPath().replaceFirst(bucketName,"").replaceAll("\\\\","/");
         result.add(new DataObjectSummaryImpl(bucketName,addKey));
      }
      return result;
   }

   @Override
   public void copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
      try {
         ensureDirectoriesExist(destinationBucketName,destinationKey);
         Files.copy(new File(sourceBucketName + "/" + sourceKey).toPath(),new File(destinationBucketName + "/" + destinationKey).toPath());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
