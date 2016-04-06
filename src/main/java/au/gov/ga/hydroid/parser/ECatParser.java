package au.gov.ga.hydroid.parser;

import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by u24529 on 6/04/2016.
 */
@Component("eCatParser")
public class ECatParser extends AbstractParser {

   private static final Set<MediaType> SUPPORTED_TYPES =
         Collections.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(
               MediaType.application("xml"),
               MediaType.image("svg+xml"))));

   private static final String DATE_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/dateStamp/Date";

   private static final String TITLE_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/identificationInfo/" +
         "MD_DataIdentification/citation/CI_Citation/title/CharacterString";

   private static final String AUTHOR_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/identificationInfo/" +
         "MD_DataIdentification/citation/CI_Citation/citedResponsibleParty/CI_ResponsibleParty/individualName/CharacterString";

   private static final String ABSTRACT_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/identificationInfo/" +
         "MD_DataIdentification/abstract/CharacterString";

   private void setMetadata(Metadata metadata, String name, String value) {
      if (value == null || value.isEmpty()) {
         return;
      }
      String metadataValue = metadata.get(name);
      if (metadataValue == null) {
         metadataValue = value;
      } else {
         metadataValue = metadataValue + "; " + value;
      }
      metadata.set(name, metadataValue);
   }

   private void parserXml(InputStream inputStream, Metadata metadata)  {
      try {
         String nodeValue;
         XPath xPath =  XPathFactory.newInstance().newXPath();
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document document = builder.parse(inputStream);

         setMetadata(metadata, Metadata.CONTENT_TYPE, "application/xml");

         nodeValue = xPath.compile(DATE_XPATH_EXPRESSION).evaluate(document);
         if (nodeValue != null && !nodeValue.isEmpty()) {
            Date dateStamp = DateUtils.parseDate(nodeValue, new String[]{"yyyy-MM-dd"});
            setMetadata(metadata, "Creation-Date", DateUtils.formatDate(dateStamp, "yyyy-MM-dd'T'HH:mm:ss'Z'"));
         }

         nodeValue = xPath.compile(TITLE_XPATH_EXPRESSION).evaluate(document);
         setMetadata(metadata, "title", nodeValue);

         nodeValue = xPath.compile(ABSTRACT_XPATH_EXPRESSION).evaluate(document);
         setMetadata(metadata, "content", nodeValue);

         NodeList nodeList = (NodeList) xPath.compile(AUTHOR_XPATH_EXPRESSION).evaluate(document, XPathConstants.NODESET);
         if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
               nodeValue = nodeList.item(i).getTextContent();
               setMetadata(metadata, "Author", nodeValue);
            }
         }

      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

   @Override
   public Set<MediaType> getSupportedTypes(ParseContext context) {
      return SUPPORTED_TYPES;
   }

   @Override
   public void parse(InputStream inputStream, ContentHandler contentHandler, Metadata metadata, ParseContext parseContext)
         throws IOException, SAXException, TikaException {
      parserXml(inputStream, metadata);
      String content = metadata.get("content");
      contentHandler.characters(content.toCharArray(), 0, content.length());
   }

}
