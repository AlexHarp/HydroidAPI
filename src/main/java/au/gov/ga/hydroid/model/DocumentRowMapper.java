package au.gov.ga.hydroid.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by u24529 on 4/02/2016.
 */
public class DocumentRowMapper implements RowMapper {

   @Override
   public Object mapRow(ResultSet resultSet, int i) throws SQLException {
      Document document = new Document();
      document.setId(resultSet.getLong("id"));
      document.setUrn(resultSet.getString("urn"));
      document.setContent(resultSet.getBytes("content"));
      return document;
   }

}
