package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.DataObjectSummary;
import au.gov.ga.hydroid.service.S3Client;
import au.gov.ga.hydroid.service.impl.DataObjectSummaryImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("fileSystemClientImpl")
public class FileSystemClientImpl implements S3Client {

   public FileSystemClientImpl() {
      this.basePath = FileSystems.getDefault().getPath(System.getProperties().getProperty("s3.use.file.system.path"));
   }

   public FileSystemClientImpl(Path basePath) {
      this.basePath = basePath;
   }

   private Path basePath;

   @Override
   public String getAccountOwner() {
      return null;
   }

   private File _getFile(String bucketName,String key) {
      Path p = FileSystems.getDefault().getPath(basePath.toString(),bucketName,key);
      return new File(p.toString());
   }

   private void ensureDirectoriesExist(String bucketName, String key) {
      File file = _getFile(bucketName,key);
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
         result = FileUtils.openInputStream(_getFile(bucketName,key));
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
         is = FileUtils.openInputStream(_getFile(bucketName,key));
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
         Files.write(_getFile(bucketName,key).toPath(), Collections.singletonList(content), Charset.forName("UTF-8"));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void storeFile(String bucketName, String key, InputStream content, String contentType) {
      try {
         ensureDirectoriesExist(bucketName,key);
         Files.write(_getFile(bucketName,key).toPath(), IOUtils.toByteArray(content));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void deleteFile(String bucketName, String key) {
      try {
         Path file = _getFile(bucketName,key).toPath();
         if(Files.exists(file)) {
            Files.delete(file);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public List<DataObjectSummary> listObjects(String bucketName, String key) {
      List<DataObjectSummary> result = new ArrayList<>();
      File fileRoot =_getFile(bucketName,key);
      if(fileRoot.listFiles() == null) {
         return result;
      }
      for(File file : fileRoot.listFiles()) {
         String addKey = file.getPath().toString().replace(this.basePath.toAbsolutePath().toString() + File.separator,"").replaceFirst(bucketName,"").replaceAll("\\\\","/");
         result.add(new DataObjectSummaryImpl(bucketName,addKey));
      }
      return result;
   }

   @Override
   public void copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
      try {
         ensureDirectoriesExist(destinationBucketName,destinationKey);
         Files.copy(_getFile(sourceBucketName,sourceKey).toPath(),_getFile(destinationBucketName,destinationKey).toPath());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
