package au.gov.ga.hydroid.service.impl;

import au.gov.ga.hydroid.service.UrlContentParser;
import au.gov.ga.hydroid.utils.HydroidException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Date;
import java.util.Properties;

/**
 * Created by u24529 on 31/03/2016.
 */
@Service("eCatParser")
public class ECatContentParser implements UrlContentParser {

   private static final String[] VALID_PARENTS = {"citation", "CI_Citation", "date", "CI_DATE", "citedResponsibleParty", "CI_ResponsibleParty"};
   private static final String[] VALID_NODES = {"abstract", "title", "date", "individualName"};
   private static final Properties LABELS = new Properties();

   static {
      LABELS.setProperty("dateStamp", "Creation-Date");
      LABELS.setProperty("individualName", "Author");
   }

   @Override
   public String parseUrl(String url, Metadata metadata) {
      parserXml(url, metadata);
      return metadata.get("abstract");
   }

   private String getLabel(String value) {
      String label = LABELS.getProperty(value);
      if (label == null) {
         label = value;
      }
      return label;
   }

   private String removeLineBreak(String value) {
      String tempValue = value.replace("\n", "");
      return StringUtils.trimLeadingWhitespace(tempValue).trim();
   }

   private void setMetadata(Metadata metadata, Node node) {
      String metadataName = getLabel(node.getNodeName());
      String metadataValue = metadata.get(metadataName);
      if (metadataValue == null) {
         metadataValue = removeLineBreak(node.getTextContent());
      } else {
         metadataValue = metadataValue + ", " + removeLineBreak(node.getTextContent());
      }
      metadata.set(metadataName, metadataValue);
   }

   private void getChildren(Node node, Metadata metadata) {
      Node localNode = node;
      NodeList children = localNode.getChildNodes();
      if (children != null) {
         for (int i = 0; i < children.getLength(); i++) {
            localNode = children.item(i);
            if (ArrayUtils.contains(VALID_NODES, localNode.getNodeName())) {
               setMetadata(metadata, localNode);
            }
            if (ArrayUtils.contains(VALID_PARENTS, localNode.getNodeName())) {
               getChildren(localNode, metadata);
            }
         }
      }
   }

   private void parserXml(String url, Metadata metadata)  {
      try {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(url);
         doc.getDocumentElement().normalize();
         NodeList nodeList = doc.getElementsByTagName("MD_DataIdentification");
         if (nodeList != null) {
            Node node = nodeList.item(0);
            getChildren(node, metadata);
         }
         nodeList = doc.getElementsByTagName("dateStamp");
         if (nodeList != null) {
            Node node = nodeList.item(0);
            Date dateStamp = DateUtils.parseDate(removeLineBreak(node.getTextContent()), new String[]{"yyyy-MM-dd"});
            metadata.set(getLabel(node.getNodeName()), DateUtils.formatDate(dateStamp, "yyyy-MM-dd'T'HH:mm:ss'Z'"));
         }
      } catch (Exception e) {
         throw new HydroidException(e);
      }
   }

}
