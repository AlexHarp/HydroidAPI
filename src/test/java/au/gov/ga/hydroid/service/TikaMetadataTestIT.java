package au.gov.ga.hydroid.service;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.parser.ECatParser;
import au.gov.ga.hydroid.utils.HydroidException;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

/**
 * Created by u24529 on 31/03/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(HydroidApplication.class)
@IntegrationTest
public class TikaMetadataTestIT {

   @Test
   public void testPDFMetadata() {
      String origin = "/testfiles/36_4_1175-1197_Buss_and_Clote.pdf";
      Metadata metadata = new Metadata();
      IOUtils.parseStream(this.getClass().getResourceAsStream(origin), metadata);
      Assert.assertTrue(metadata.size() > 0);
      Assert.assertEquals("Title", "Solving the Fisher-Wright and Coalescence Problems with a Discrete Markov Chain Analysis", metadata.get("title"));
      Assert.assertNotNull("Creation-Date", DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
   }

   @Test
   public void testWordMetadata() {
      String origin = "/testfiles/whale.docx";
      Metadata metadata = new Metadata();
      IOUtils.parseStream(this.getClass().getResourceAsStream(origin), metadata);
      Assert.assertTrue(metadata.size() > 0);
      Assert.assertEquals("Title", "Whale text from Wikipedia", metadata.get("title"));
      Assert.assertEquals("Author", "Carneiro Elton", metadata.get("Author"));
      Assert.assertNotNull("Creation-Date", DateUtils.parseDate(metadata.get("Creation-Date"), new String[] {"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
   }

   @Test
   public void testTikaDefaultParser(){
      try {
         String url = "http://www.ga.gov.au/metadata-gateway/metadata/record/gcat_a05f7892-8d5d-7506-e044-00144fdd4fa6";
         Metadata metadata = new Metadata();
         InputStream inputStream = IOUtils.getUrlContent(url);
         IOUtils.parseStream(inputStream, metadata);
         Assert.assertEquals("Title",
               "Geoscience Australia Metadata for Hydrogeology Map of Australia (G.Jacobson and JE.Lau Hydrogeology Map)",
               metadata.get("title"));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testECatParser(){
      try {
         String url = "http://www.ga.gov.au/metadata-gateway/metadata/record/gcat_a05f7892-8d5d-7506-e044-00144fdd4fa6/xml";
         Metadata metadata = new Metadata();
         InputStream inputStream = IOUtils.getUrlContent(url);
         Parser eCatParser = new ECatParser();
         IOUtils.parseStream(inputStream, metadata, eCatParser);
         Assert.assertEquals("Title", "Hydrogeology Map of Australia (G.Jacobson and JE.Lau Hydrogeology Map)", metadata.get("title"));
         Assert.assertEquals("Author", "Brodie, R.S.; Kilgour, B.; Jacobson, G.; Lau, J.E.", metadata.get("Author"));
         Assert.assertNotNull("Creation-Date", DateUtils.parseDate(metadata.get("Creation-Date"), new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"}));
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}
