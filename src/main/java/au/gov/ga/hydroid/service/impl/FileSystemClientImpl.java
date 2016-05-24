package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.dto.FileMetadata;
import au.gov.ga.hydroid.service.DataObjectSummary;
import au.gov.ga.hydroid.service.S3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service("s3FileSystem")
public class FileSystemClientImpl implements S3Client {

   private static final Logger logger = LoggerFactory.getLogger(FileSystemClientImpl.class);

   private Path basePath;

   public FileSystemClientImpl() {
      String defaultPath = System.getProperty("java.io.tmpdir");
      String customPath = System.getProperty("s3.use.file.system.path", defaultPath);
      this.basePath = FileSystems.getDefault().getPath(customPath).toAbsolutePath();
   }

   @Override
   public String getAccountOwner() {
      return null;
   }

   private String sanitizedKey(String key) {
      return key.replaceAll(":", "-");
   }

   private File doGetFile(String bucketName, String key) {
      key = sanitizedKey(key);
      return basePath.getFileSystem().getPath(basePath.toString(), bucketName, key).toFile();
   }

   private void ensureDirectoriesExist(String bucketName, String key) {
      File file = doGetFile(bucketName, key);
      Path parentDir = file.toPath().getParent();
      if (!Files.exists(parentDir)) {
         try {
            Files.createDirectories(parentDir);
         } catch (IOException e) {
            logger.debug("ensureDirectoriesExist - IOException: ", e);
         }
      }
   }

   @Override
   public InputStream getFile(String bucketName, String key) {
      InputStream result = null;
      Path fileToGet = doGetFile(bucketName, key).toPath().toAbsolutePath();
      logger.debug("getFile - Trying to get file: " + fileToGet.toString());
      logger.debug("getFile - File exists: " + Files.exists(fileToGet));
      try {
         if (Files.exists(fileToGet)) {
            result = FileUtils.openInputStream(fileToGet.toFile());
         }
      } catch (IOException e) {
         logger.debug("getFile - IOException: ", e);
      }
      return result;
   }

   @Override
   public byte[] getFileAsByteArray(String bucketName, String key) {
      byte[] result = null;
      Path fileToGet = doGetFile(bucketName, key).toPath().toAbsolutePath();
      logger.debug("getFileAsByteArray - Trying to get file: " + fileToGet.toString());
      logger.debug("getFileAsByteArray - File exists: " + Files.exists(fileToGet));
      if (Files.exists(fileToGet)) {
         try (InputStream is = FileUtils.openInputStream(fileToGet.toFile())) {
            result = IOUtils.toByteArray(is);
         } catch (IOException e) {
            logger.debug("getFileAsByteArray - IOException: ", e);
         }
      }
      return result;
   }

   @Override
   public void storeFile(String bucketName, String key, String content, String contentType) {
      try {
         ensureDirectoriesExist(bucketName, key);
         Files.write(doGetFile(bucketName, key).toPath(), content.getBytes());
      } catch (IOException e) {
         logger.debug("storeFile - IOException: ", e);
      }
   }

   @Override
   public void storeFile(String bucketName, String key, InputStream content, String contentType, long contentLength) {
      try {
         ensureDirectoriesExist(bucketName, key);
         Files.write(doGetFile(bucketName, key).toPath(), IOUtils.toByteArray(content));
      } catch (IOException e) {
         logger.debug("storeFile - IOException: ", e);
      }
   }

   @Override
   public void deleteFile(String bucketName, String key) {
      try {
         Path file = doGetFile(bucketName, key).toPath();
         if (Files.exists(file)) {
            Files.delete(file);
         }
      } catch (IOException e) {
         logger.debug("deleteFile - IOException: ", e);
      }
   }

   @Override
   public List<DataObjectSummary> listObjects(String bucketName, String key) {
      List<DataObjectSummary> result = new ArrayList<>();
      File fileRoot = doGetFile(bucketName, key);
      logger.debug("listObjects - Listing files in: " + fileRoot.getAbsolutePath());
      if (fileRoot.listFiles() == null) {
         return result;
      }
      for (File file : fileRoot.listFiles()) {
         String addKey = file.getPath().replace(this.basePath.toAbsolutePath().toString() + File.separator + bucketName, "").replaceAll("\\\\", "/");
         result.add(new DataObjectSummaryImpl(bucketName, addKey));
      }
      return result;
   }

   @Override
   public void copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
      try {
         ensureDirectoriesExist(destinationBucketName, destinationKey);
         Files.copy(doGetFile(sourceBucketName, sourceKey).toPath(), doGetFile(destinationBucketName, destinationKey).toPath());
      } catch (IOException e) {
         logger.debug("copyObject - IOException: ", e);
      }
   }

   @Override
   public ObjectMetadata getObjectMetadata(String bucketName, String key) {
      ObjectMetadata objectMetadata = new FileMetadata();
      Path file = doGetFile(bucketName, key).toPath();
      if (Files.exists(file)) {
         long length = file.toFile().length();
         objectMetadata.setContentLength(length);
      }
      return objectMetadata;
   }

}
