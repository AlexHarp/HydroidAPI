package au.gov.ga.hydroid.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by u24529 on 4/02/2016
 * Class maps the Document objects to the Documents table
 */
public class DocumentRowMapper implements RowMapper {

   /**
    * Returns a Document object which has been mapped from a row in the Documents table
    * @param resultSet result set
    * @param rowNum row number
    * @return the mapped Document object
    * @throws SQLException
    */
   public Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {
      Document document = new Document();
      document.setId(resultSet.getLong("id"));
      document.setOrigin(resultSet.getString("origin"));
      document.setUrn(resultSet.getString("urn"));
      document.setTitle(resultSet.getString("title"));
      document.setType(DocumentType.valueOf(resultSet.getString("type")));
      document.setContent(resultSet.getBytes("content"));
      document.setStatus(EnhancementStatus.valueOf(resultSet.getString("status")));
      document.setStatusReason(resultSet.getString("status_reason"));
      document.setProcessDate(resultSet.getTimestamp("process_date"));
      return document;
   }

}
