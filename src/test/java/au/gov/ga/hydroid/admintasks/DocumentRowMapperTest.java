package au.gov.ga.hydroid.admintasks;

import au.gov.ga.hydroid.HydroidApplication;
import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentRowMapper;
import au.gov.ga.hydroid.utils.HydroidException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;

/**
 * Created by u24529 on 7/04/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HydroidApplication.class)
public class DocumentRowMapperTest {

   @Mock
   private ResultSet resultSet;

   @Before
   public void setup() {
      MockitoAnnotations.initMocks(this);
      try {
         Mockito.when(resultSet.getLong("id")).thenReturn(Long.valueOf(1));
         Mockito.when(resultSet.getString("title")).thenReturn("The title");
         Mockito.when(resultSet.getString("type")).thenReturn("DOCUMENT");
         Mockito.when(resultSet.getString("status")).thenReturn("SUCCESS");
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Test
   public void testValidDocument() {
      Document document = (Document) new DocumentRowMapper().mapRow(resultSet, 0);
      Assert.assertNotNull(document);
      Assert.assertEquals("The title", document.getTitle());
   }

   @Test
   public void testInvalidDocument() {
      try {
         Mockito.when(resultSet.getString("type")).thenReturn("WRONG_TYPE");
         new DocumentRowMapper().mapRow(resultSet, 0);
      } catch (Exception e) {
         Assert.assertNotNull(e);
      }
   }

}
