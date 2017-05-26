package net.sourceforge.pmd.util.redline.util;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedLineConfiguration
{
  private XmlParser xmlParser;
  private Map<String, Map<String, RedlineFilterRule>> filterMethodMap = new HashMap();
  
  public RedLineConfiguration(String configurationFile)
  {
    try
    {
      if ((configurationFile.startsWith("/")) || (configurationFile.contains(":"))) {
        this.xmlParser = new XmlParser(configurationFile, false);
      } else {
        this.xmlParser = new XmlParser(configurationFile, true);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public List<String> getFilterMethod(String ruleName, String action)
  {
    parse();
    if (this.filterMethodMap.containsKey(ruleName))
    {
      Map<String, RedlineFilterRule> actionMap = (Map)this.filterMethodMap.get(ruleName);
      if (actionMap.containsKey(action)) {
        return ((RedlineFilterRule)actionMap.get(action)).methods;
      }
    }
    return null;
  }
  
  public void parse()
  {
    if (this.xmlParser == null) {
      return;
    }
    Node pmdNode = this.xmlParser.getDescendantsChildrenNodeFromRoot("pmd");
    Node filterRules = XmlParser.getChildrenNode(pmdNode, "filterRules");
    List<Node> nodeList = XmlParser.getAllChildrenNodes(filterRules, "filterRule");
    for (Node filterNode : nodeList)
    {
      String ruleName = XmlParser.getNodeAttributeValue(filterNode, "ruleName");
      Map<String, RedlineFilterRule> actionMap;
      if (this.filterMethodMap.containsKey(ruleName))
      {
        actionMap = (Map)this.filterMethodMap.get(ruleName);
      }
      else
      {
        actionMap = new HashMap();
        this.filterMethodMap.put(ruleName, actionMap);
      }
      String action = XmlParser.getNodeAttributeValue(filterNode, "action");
      RedlineFilterRule filterRule;
      if (actionMap.containsKey(action))
      {
        filterRule = (RedlineFilterRule)actionMap.get(action);
      }
      else
      {
        filterRule = new RedlineFilterRule();
        filterRule.ruleName = ruleName;
        filterRule.action = action;
        actionMap.put(action, filterRule);
      }
      List<Node> mNodes = XmlParser.getAllChildrenNodes(filterNode, "method");
      for (Node mNode : mNodes)
      {
        String method = XmlParser.getNodeAttributeValue(mNode, "name");
        if (method != null) {
          filterRule.methods.add(method);
        }
      }
    }
    RedlineFilterRule filterRule;
  }
}
