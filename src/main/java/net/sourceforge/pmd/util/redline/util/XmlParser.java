package net.sourceforge.pmd.util.redline.util;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmlParser
{
  private Document doc;
  private Node root;
  private NodeList rootNodes;
  private static final String INDENTATION = "|---";
  private String xmlFileName;
  
  public XmlParser(String xmlFileName, boolean isResourceFile)
    throws Exception
  {
    initXmlData(xmlFileName, isResourceFile);
  }
  
  private void initXmlData(String xmlFileName, boolean isResourceFile)
    throws Exception
  {
    this.xmlFileName = xmlFileName;
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    InputStream in = null;
    if (isResourceFile) {
      in = getClass().getClassLoader().getResourceAsStream(xmlFileName);
    } else {
      in = new FileInputStream(new File(xmlFileName));
    }
    this.doc = builder.parse(in);
    this.root = this.doc.getFirstChild();
    this.rootNodes = this.root.getChildNodes();
  }
  
  public void printXmlAllNode()
  {
    Q.println(this.doc.getNodeName());
    printChildrenNodes(this.doc.getFirstChild(), "|---");
  }
  
  public Document getDocument()
  {
    return this.doc;
  }
  
  public Node getRootNode()
  {
    return this.root;
  }
  
  public static void printChildrenNodes(Node parentNode, String indentation)
  {
    if (parentNode.getChildNodes().getLength() > 0)
    {
      NodeList nodes = parentNode.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
        Node n = nodes.item(i);
        if (n != null) {
          if (n.getNodeType() == 1)
          {
            Q.print(indentation + "<" + n.getNodeName());
            NamedNodeMap attrs = n.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
              Q.print(" " + attrs.getNamedItem(attrs.item(j).getNodeName()));
            }
            Q.println(">");
            printChildrenNodes(n, indentation + "|---");
            Q.println(indentation + "</" + n.getNodeName() + ">");
          }
          else if (n.getNodeType() == 3)
          {
            if (n.getTextContent().trim().length() > 0)
            {
              String subStr = n.getTextContent();
              if (subStr.length() > 10) {
                subStr = subStr.substring(0, 10) + "...";
              }
              Q.println(indentation + subStr);
            }
          }
          else if ((n.getNodeType() == 2) && 
            (n.getTextContent().trim().length() > 0))
          {
            Q.println(indentation + n.getTextContent());
          }
        }
      }
    }
  }
  
  public Node appendNodeToRoot(String tagName)
  {
    return appendNodeToRoot(tagName, null);
  }
  
  public Node appendNodeToRoot(String tagName, Map<String, String> attrMap)
  {
    return appendNode(this.root, tagName, attrMap);
  }
  
  public Node appendNode(Node pareNode, String tagName, Map<String, String> attrMap)
  {
    Element element = this.doc.createElement(tagName);
    if (attrMap != null)
    {
      Set<String> keySet = attrMap.keySet();
      for (String key : keySet) {
        element.setAttribute(key, (String)attrMap.get(key));
      }
    }
    pareNode.appendChild(element);
    return element;
  }
  
  public void appendNodeToRoot(Node node)
  {
    appendNode(this.root, node);
  }
  
  public void appendNodesToRoot(List<Node> nodes)
  {
    appendNodes(this.root, nodes);
  }
  
  public Node importNodeToNode(Node toNode, Node newNode, boolean deep)
  {
    Node node = this.doc.importNode(newNode, deep);
    toNode.appendChild(node);
    return toNode;
  }
  
  public Node importNode(Node newNode, boolean deep)
  {
    Node node = this.doc.importNode(newNode, deep);
    return node;
  }
  
  public Node importNodeToRoot(Node node, boolean deep)
  {
    return importNodeToNode(this.root, node, deep);
  }
  
  public static void appendNode(Node parentNode, Node node)
  {
    parentNode.appendChild(node);
  }
  
  public static void appendNodes(Node parentNode, List<Node> nodes)
  {
    for (Node node : nodes) {
      parentNode.appendChild(node);
    }
  }
  
  public List<Node> removeAllNodesFromRoot(String nodeName)
  {
    return removeAllChildrenNodes(this.root, nodeName);
  }
  
  public void removeNodeFromRoot(Node node)
  {
    this.root.removeChild(node);
  }
  
  public static void removeNodeFromParent(Node node)
  {
    node.getParentNode().removeChild(node);
  }
  
  public static List<Node> removeAllChildrenNodes(Node parentNode, String nodeName)
  {
    ArrayList<Node> nodesList = new ArrayList();
    NodeList nodes = parentNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Node n = nodes.item(i);
      if (n != null) {
        if (n.getNodeType() == 1) {
          if (n.getNodeName().equals(nodeName))
          {
            nodesList.add(n);
            parentNode.removeChild(n);
          }
          else
          {
            List<Node> list = removeAllChildrenNodes(n, nodeName);
            nodesList.addAll(list);
          }
        }
      }
    }
    return nodesList;
  }
  
  public ArrayList<Node> getAllNodesFromRoot(String nodeName)
  {
    return getAllChildrenNodes(this.root, nodeName);
  }
  
  public ArrayList<Node> getRootChildrenNodes(String findNodeName)
  {
    return getChildrenNodes(this.root, findNodeName);
  }
  
  public static ArrayList<Node> getChildrenNodes(Node parentNode, String findNodeName)
  {
    ArrayList<Node> nodesList = new ArrayList();
    NodeList nodes = parentNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Node n = nodes.item(i);
      if (n != null) {
        if ((n.getNodeType() == 1) && 
          (n.getNodeName().equals(findNodeName))) {
          nodesList.add(n);
        }
      }
    }
    return nodesList;
  }
  
  public Node getChildrenNodeOfRoot(String findNodeName)
  {
    return getChildrenNode(this.root, findNodeName);
  }
  
  public static Node getChildrenNode(Node parentNode, String findNodeName)
  {
    NodeList nodes = parentNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Node n = nodes.item(i);
      if (n != null) {
        if ((n.getNodeType() == 1) && 
          (n.getNodeName().equals(findNodeName))) {
          return n;
        }
      }
    }
    return null;
  }
  
  public Node getDescendantsChildrenNodeFromRoot(String findNodeName)
  {
    return getDescendantsChildrenNode(this.root, findNodeName);
  }
  
  public static Node getDescendantsChildrenNode(Node parentNode, String findNodeName)
  {
    NodeList nodes = parentNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Node n = nodes.item(i);
      if (n != null) {
        if (n.getNodeType() == 1)
        {
          if (n.getNodeName().equals(findNodeName)) {
            return n;
          }
          Node node = getDescendantsChildrenNode(n, findNodeName);
          if (node != null) {
            return node;
          }
        }
      }
    }
    return null;
  }
  
  public static String getNodeAttributeValue(Node node, String attributeName)
  {
    NamedNodeMap map = node.getAttributes();
    if (map != null)
    {
      Node attrNode = map.getNamedItem(attributeName);
      if (attrNode != null) {
        return attrNode.getNodeValue();
      }
    }
    return null;
  }
  
  public Element createNode(String nodeName, Map<String, String> attrMap)
  {
    Element node = this.doc.createElement(nodeName);
    if (attrMap != null)
    {
      Set<String> keySet = attrMap.keySet();
      for (String key : keySet) {
        node.setAttribute(key, (String)attrMap.get(key));
      }
    }
    return node;
  }
  
  public Element createNode(String nodeName)
  {
    return createNode(nodeName, null);
  }
  
  public Text createTextNode(String nodeName, String textContent)
  {
    Text text = this.doc.createTextNode(nodeName);
    text.setTextContent(textContent);
    return text;
  }
  
  public Node getDescendantsChildrenNodeByAttributeFromRoot(String nodeName, String attributeName, String attributeValue)
  {
    return getDescendantsChildrenNodeByAttribute(this.root, nodeName, attributeName, attributeValue);
  }
  
  public static Node getDescendantsChildrenNodeByAttribute(Node parentNode, String nodeName, String attributeName, String attributeValue)
  {
    NodeList nodes = parentNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Node n = nodes.item(i);
      if (n != null) {
        if (n.getNodeType() == 1)
        {
          if (n.getNodeName().equals(nodeName))
          {
            NamedNodeMap map = n.getAttributes();
            if (map != null)
            {
              Node attrNode = map.getNamedItem(attributeName);
              if ((attrNode != null) && (attrNode.getNodeValue().equalsIgnoreCase(attributeValue))) {
                return n;
              }
            }
          }
          Node node = getDescendantsChildrenNodeByAttribute(n, nodeName, attributeName, attributeValue);
          if (node != null) {
            return node;
          }
        }
      }
    }
    return null;
  }
  
  public static ArrayList<Node> getAllChildrenNodes(Node parentNode, String findNodeName)
  {
    ArrayList<Node> nodesList = new ArrayList();
    NodeList nodes = parentNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Node n = nodes.item(i);
      if (n != null) {
        if (n.getNodeType() == 1)
        {
          if (n.getNodeName().equals(findNodeName)) {
            nodesList.add(n);
          }
          ArrayList<Node> list = getAllChildrenNodes(n, findNodeName);
          nodesList.addAll(list);
        }
      }
    }
    return nodesList;
  }
  
  public void saveAs(String fileName)
  {
    TransformerFactory tf = TransformerFactory.newInstance();
    try
    {
      Transformer transformer = tf.newTransformer();
      DOMSource source = new DOMSource(this.doc);
      transformer.setOutputProperty("encoding", "UTF-8");
      transformer.setOutputProperty("indent", "yes");
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
      StreamResult result = new StreamResult(pw);
      transformer.transform(source, result);
    }
    catch (TransformerConfigurationException e)
    {
      System.out.println(e.getMessage());
    }
    catch (IllegalArgumentException e)
    {
      System.out.println(e.getMessage());
    }
    catch (FileNotFoundException e)
    {
      System.out.println(e.getMessage());
    }
    catch (TransformerException e)
    {
      System.out.println(e.getMessage());
    }
    catch (UnsupportedEncodingException e)
    {
      System.out.println(e.getMessage());
    }
  }
  
  public void save()
  {
    saveAs(this.xmlFileName);
  }
  
  public static boolean createXML(String fileName, String rootName)
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = dbf.newDocumentBuilder();
      Document document = builder.newDocument();
      document.setXmlVersion("1.0");
      Element root = document.createElement(rootName);
      document.appendChild(root);
      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();
      DOMSource source = new DOMSource(document);
      
      transformer.setOutputProperty("encoding", "UTF-8");
      transformer.setOutputProperty("indent", "yes");
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
      StreamResult result = new StreamResult(pw);
      transformer.transform(source, result);
    }
    catch (Exception e)
    {
      return false;
    }
    return true;
  }
  
  public static void main(String[] args) {}
}
