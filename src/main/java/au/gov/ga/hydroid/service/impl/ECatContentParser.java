package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.UrlContentParser;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.Date;

/**
 * Created by u24529 on 31/03/2016.
 */
@Service("eCatParser")
public class ECatContentParser implements UrlContentParser {

   private static final String DATE_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/dateStamp/Date";
   private static final String TITLE_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/identificationInfo/" +
         "MD_DataIdentification/citation/CI_Citation/title/CharacterString";
   private static final String AUTHOR_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/identificationInfo/" +
         "MD_DataIdentification/citation/CI_Citation/citedResponsibleParty/CI_ResponsibleParty/individualName/CharacterString";
   private static final String ABSTRACT_XPATH_EXPRESSION = "/GetRecordByIdResponse/MD_Metadata/identificationInfo/" +
         "MD_DataIdentification/abstract/CharacterString";

   @Override
   public String parseUrl(String url, Metadata metadata) {
      parserXml(url, metadata);
      return metadata.get("abstract");
   }

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

   private void parserXml(String url, Metadata metadata)  {
      try {
         String nodeValue;
         XPath xPath =  XPathFactory.newInstance().newXPath();
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document document = builder.parse(url);

         nodeValue = xPath.compile(DATE_XPATH_EXPRESSION).evaluate(document);
         if (nodeValue != null && !nodeValue.isEmpty()) {
            Date dateStamp = DateUtils.parseDate(nodeValue, new String[]{"yyyy-MM-dd"});
            setMetadata(metadata, "Creation-Date", DateUtils.formatDate(dateStamp, "yyyy-MM-dd'T'HH:mm:ss'Z'"));
         }

         nodeValue = xPath.compile(TITLE_XPATH_EXPRESSION).evaluate(document);
         setMetadata(metadata, "title", nodeValue);

         nodeValue = xPath.compile(ABSTRACT_XPATH_EXPRESSION).evaluate(document);
         setMetadata(metadata, "abstract", nodeValue);

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

}
