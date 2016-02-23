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
      return jdbcTemplate.query("SELECT * FROM hydroid.documents", new DocumentRowMapper());
   }

   @Override
   public Document findByUrn(String urn) {
      try {
         return (Document) jdbcTemplate.queryForObject("SELECT * FROM hydroid.documents where urn = ?",
               new String[]{urn}, new DocumentRowMapper());
      } catch (IncorrectResultSizeDataAccessException e) {
         // document was not found
         return null;
      }
   }

   @Override
   @Transactional
   public void create(Document document) {
      jdbcTemplate.update("insert into documents (urn, title, type, content) values (?, ?, ?, ?)",
            document.getUrn(), document.getTitle(), document.getType().name(), document.getContent());
   }

   @Override
   public void deleteByUrn(String urn) {
      jdbcTemplate.update("delete from documents where urn = ?", urn);
   }

   @Override
   public void update(Document document) {
      jdbcTemplate.update("update documents set urn = ? where id = ?", document.getUrn(), document.getId());
   }

}
