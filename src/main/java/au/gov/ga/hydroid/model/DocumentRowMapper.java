package au.gov.ga.hydroid.model;

import au.gov.ga.hydroid.utils.HydroidException;
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
    */
   @Override
   public Object mapRow(ResultSet resultSet, int rowNum) {
      Document document = new Document();
      try {
         document.setId(resultSet.getLong("id"));
         document.setOrigin(resultSet.getString("origin"));
         document.setUrn(resultSet.getString("urn"));
         document.setTitle(resultSet.getString("title"));
         document.setType(DocumentType.valueOf(resultSet.getString("type")));
         document.setStatus(EnhancementStatus.valueOf(resultSet.getString("status")));
         document.setStatusReason(resultSet.getString("status_reason"));
         document.setProcessDate(resultSet.getTimestamp("process_date"));
         document.setParserName(resultSet.getString("parser_name"));
      } catch (SQLException e) {
         throw new HydroidException(e);
      }
      return document;
   }

}
