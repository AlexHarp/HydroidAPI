package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.service.impl.DataObjectSummaryImpl;
import au.gov.ga.hydroid.service.impl.FileSystemClientImpl;
import au.gov.ga.hydroid.utils.IOUtils;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileSystemClientImplTest {

   // Get the temporary directory and print it.
   FileSystemClientImpl fsClient = new FileSystemClientImpl();

   @Test
   public void testGetAccountOwner() throws Exception {
      String accountOwner = fsClient.getAccountOwner();
      Assert.assertEquals(null, accountOwner);
   }

   @Test
   public void testGetFileAsByteArray() throws Exception {
      fsClient.storeFile("test", "test.txt", "Hello", "text/plain");
      byte[] bytes = fsClient.getFileAsByteArray("test", "test.txt");
      String str = new String(bytes, StandardCharsets.UTF_8);
      Assert.assertEquals("Hello", str.trim());
   }

   @Test
   public void testStoreFile() throws Exception {
      fsClient.storeFile("test", "test.txt", "Hello", "text/plain");
      InputStream is = fsClient.getFile("test", "test.txt");
      String result = IOUtils.parseStream(is);
      Assert.assertEquals("Hello", result.trim());
      is.close();
   }

   @Test
   public void testStoreFileFromStream() throws Exception {
      InputStream fileContent = new ByteArrayInputStream("Hello".getBytes());
      fsClient.storeFile("test", "test.txt", fileContent, "text/plain", 5);
      InputStream is = fsClient.getFile("test", "test.txt");
      String result = IOUtils.parseStream(is);
      Assert.assertEquals("Hello", result.trim());
      fileContent.close();
      is.close();
   }

   @Test
   public void testDeleteFile() throws Exception {
      fsClient.storeFile("test", "test.txt", "Hello", "text/plain");
      InputStream is = fsClient.getFile("test", "test.txt");
      String result = IOUtils.parseStream(is);
      Assert.assertEquals("Hello", result.trim());
      is.close();
      fsClient.deleteFile("test", "test.txt");
      Boolean exists = Files.exists(new File("test", "test.txt").toPath());
      Assert.assertEquals(false, exists);
   }

   @Test
   public void testListObjects() throws Exception {
      fsClient.storeFile("test", "foo/test.txt", "Hello", "text/plain");
      List<DataObjectSummary> dataObjs = fsClient.listObjects("test", "foo/");
      Assert.assertEquals(1, dataObjs.size());
      Assert.assertEquals("test", dataObjs.get(0).getBucketName());
      Assert.assertEquals("/foo/test.txt", dataObjs.get(0).getKey());
   }

   @Test
   public void testCopyObject() throws Exception {
      fsClient.deleteFile("test1", "test1.txt");
      fsClient.storeFile("test", "test.txt", "Hello", "text/plain");
      fsClient.copyObject("test", "test.txt", "test1", "test1.txt");
      InputStream is = fsClient.getFile("test1", "test1.txt");
      String result = IOUtils.parseStream(is);
      Assert.assertEquals("Hello", result.trim());
      is.close();
   }

   @Test
   public void testDataObjectSummary() {
      S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
      s3ObjectSummary.setBucketName("myBucket");
      s3ObjectSummary.setKey("myKey");
      DataObjectSummary dataObjectSummary = new DataObjectSummaryImpl(s3ObjectSummary);
      Assert.assertEquals("myBucket", dataObjectSummary.getBucketName());
      Assert.assertEquals("myKey", dataObjectSummary.getKey());
   }

   @Test
   public void testListObjectsAndGetFile() throws Exception {
      fsClient.storeFile("test", "foo/test.txt", "Hello", "text/plain");
      List<DataObjectSummary> dataObjs = fsClient.listObjects("test", "foo/");
      Assert.assertEquals(1, dataObjs.size());
      DataObjectSummary object = dataObjs.get(0);
      Assert.assertEquals("test", object.getBucketName());
      Assert.assertEquals("/foo/test.txt", object.getKey());
      Assert.assertNotNull(fsClient.getFile(object.getBucketName(), object.getKey()));
   }

   @Test
   public void testGetObjectMetadata() throws Exception {
      fsClient.storeFile("test", "test.txt", "Hello", "text/plain");
      ObjectMetadata objectMetadata = fsClient.getObjectMetadata("test", "test.txt");
      Assert.assertNotNull(objectMetadata);
      Assert.assertEquals(7, objectMetadata.getContentLength());
      Assert.assertEquals(7, objectMetadata.getInstanceLength());
   }

}