package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.model.Document;
import au.gov.ga.hydroid.model.DocumentRowMapper;
import au.gov.ga.hydroid.model.EnhancementStatus;
import au.gov.ga.hydroid.service.DocumentService;
import au.gov.ga.hydroid.utils.HydroidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by u24529 on 4/02/2016.
 */
@Service
public class DocumentServiceImpl implements DocumentService {

   private static Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

   @Autowired
   private JdbcTemplate jdbcTemplate;

   private Date getUTCDateTime() {
      Calendar utcDateTime = Calendar.getInstance();
      ZonedDateTime localDateTime = ZonedDateTime.now();
      utcDateTime.add(Calendar.SECOND, localDateTime.getOffset().getTotalSeconds() * (-1));
      return utcDateTime.getTime();
   }

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
         logger.debug("findByUrn - IncorrectResultSizeDataAccessException: ", e);
         return null;
      }
   }

   @Override
   public Document findByOrigin(String origin) {
      try {
         return (Document) jdbcTemplate.queryForObject("SELECT * FROM documents where origin = ?",
               new String[]{origin}, new DocumentRowMapper());
      } catch (IncorrectResultSizeDataAccessException e) {
         logger.debug("findByOrigin - IncorrectResultSizeDataAccessException: ", e);
         return null;
      }
   }

   @Override
   public List<Document> findByStatus(EnhancementStatus status) {
      return jdbcTemplate.query("SELECT * FROM documents where status = ?", new String[]{status.name()}, new DocumentRowMapper());
   }

   @Override
   public void create(Document document) {
      try {
         String sql = "insert into documents (origin, urn, title, type, status, "
               + "status_reason, process_date, parser_name) values (?, ?, ?, ?, ?, ?, ?, ?)";
         jdbcTemplate.update(sql, document.getOrigin(), document.getUrn(), document.getTitle(), document.getType().name(),
               document.getStatus().name(), document.getStatusReason(), getUTCDateTime(), document.getParserName());
      } catch (DataAccessException e) {
         throw new HydroidException(e.getMostSpecificCause());
      }
   }

   @Override
   public void deleteByUrn(String urn) {
      jdbcTemplate.update("delete from documents where urn = ?", urn);
   }

   @Override
   public void update(Document document) {
      try {
         String sql = "update documents set title = ?, urn = ?, status = ?, status_reason = ?, process_date = ? where id = ?";
         jdbcTemplate.update(sql, document.getTitle(), document.getUrn(), document.getStatus().name(),
               document.getStatusReason(), getUTCDateTime(), document.getId());
      } catch (DataAccessException e) {
         throw new HydroidException(e.getMostSpecificCause());
      }
   }

   @Override
   public void clearAll() {
      jdbcTemplate.update("delete from documents");
   }

   @Override
   public void createImageMetadata(String origin, String metadata) {
      String sql = "insert into image_metadata (origin, metadata) values (?, ?)";
      jdbcTemplate.update(sql, origin, metadata);
   }

   @Override
   public String readImageMetadata(String origin) {
      try {
         return jdbcTemplate.queryForObject("SELECT metadata FROM image_metadata where origin = ?",
               new String[] {origin}, String.class);
      } catch (IncorrectResultSizeDataAccessException e) {
         logger.debug("readImageMetadata - IncorrectResultSizeDataAccessException: ", e);
         return null;
      }
   }

   @Override
   public void updateImageMetadata(String origin, String metadata) {
      String sql = "update image_metadata set metadata = ? where origin = ?";
      jdbcTemplate.update(sql, metadata, origin);
   }

}
