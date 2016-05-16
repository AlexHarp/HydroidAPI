package au.gov.ga.hydroid.dto;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by u24529 on 15/04/2016.
 */
public class MenuDTO implements Comparable {

   private String nodeURI;
   private String nodeLabel;
   private SortedSet<MenuDTO> children;
   private String nodeType;

   public String getNodeURI() {
      return nodeURI;
   }

   public void setNodeURI(String nodeURI) {
      this.nodeURI = nodeURI;
   }

   public String getNodeLabel() {
      return nodeLabel;
   }

   public void setNodeLabel(String nodeLabel) {
      this.nodeLabel = nodeLabel;
   }

   public SortedSet<MenuDTO> getChildren() {
      return children;
   }

   public String getNodeType() {
      return nodeType;
   }

   public void setNodeType(String nodeType) {
      this.nodeType = nodeType;
   }

   public MenuDTO() {
      children = new TreeSet<>();
   }


   @Override
   public int compareTo(Object o) {
      MenuDTO menuDTO = (MenuDTO) o;
      return getNodeLabel().compareTo(menuDTO.getNodeLabel());
   }
}