package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentRowMapper;
import au.gov.ga.hydroid.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by u24529 on 4/02/2016.
 */
@Service
public class DocumentServiceImpl implements DocumentService {

   @Autowired
   private JdbcTemplate jdbcTemplate;

   @Override
   public List<Document> findAll() {
      return jdbcTemplate.query("SELECT * FROM documents", new DocumentRowMapper());
   }

   @Override
   public Document findByUrn(String urn) {
      try {
         return (Document) jdbcTemplate.queryForObject("SELECT * FROM documents where urn = ?",
               new String[]{urn}, new DocumentRowMapper());
      } catch (IncorrectResultSizeDataAccessException e) {
         // document was not found
         return null;
      }
   }

   @Override
   @Transactional
   public void create(Document document) {
      String sql = new StringBuilder("insert into documents (origin, urn, title, type, content, status, ")
            .append("status_reason, process_date) values (?, ?, ?, ?, ?, ?, ?, timezone('UTC', now()))").toString();
      jdbcTemplate.update(sql, document.getOrigin(), document.getUrn(), document.getTitle(), document.getType().name(),
            document.getContent(), document.getStatus().name(), document.getStatusReason());
   }

   @Override
   public void deleteByUrn(String urn) {
      jdbcTemplate.update("delete from documents where urn = ?", urn);
   }

   @Override
   public void update(Document document) {
      String sql = "update documents set urn = ?, status = ?, status_reason = ?, process_date = timezone('UTC', now()) where id = ?";
      jdbcTemplate.update(sql, document.getUrn(), document.getStatus().name(), document.getStatusReason(), document.getId());
   }

   @Override
   public void clearAll() {
      jdbcTemplate.update("delete from documents");
   }

}
