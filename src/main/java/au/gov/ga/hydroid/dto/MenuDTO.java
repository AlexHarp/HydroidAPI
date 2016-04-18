package au.gov.ga.hydroid.dto;

import java.util.List;

/**
 * Created by u24529 on 15/04/2016.
 */
public class MenuDTO {

    private String nodeURI;
    private String nodeLabel;
    private List<MenuDTO> children;

    public void setNodeURI(String _nodeURI) {
        nodeURI = _nodeURI;
    }

    public String getNodeURI() {
        return nodeURI;
    }

    public void setNodeLabel(String _nodeLabel) {
        nodeLabel = _nodeLabel;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setChildren(List<MenuDTO> _children) {
        children = _children;
    }

    public List<MenuDTO> getChildren() {
        return children;
    }

}