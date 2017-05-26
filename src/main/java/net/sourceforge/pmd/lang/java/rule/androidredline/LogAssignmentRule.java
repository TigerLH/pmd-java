package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.jaxen.JaxenException;

import java.util.ArrayList;
import java.util.List;

public class LogAssignmentRule
  extends AbstractJavaRule
{
  private List<ASTName> astNamewithLog = new ArrayList();
  
  public Object visit(ASTCompilationUnit compilationUnit, Object data)
  {
    try
    {
      checkLogRule(compilationUnit, data);
    }
    catch (JaxenException e)
    {
      e.printStackTrace();
    }
    finally
    {
      this.astNamewithLog.clear();
    }
    return super.visit(compilationUnit, data);
  }
  
  private void checkLogRule(Node node, Object data)
    throws JaxenException
  {
    pickUpLogMethods(node);
    if (!this.astNamewithLog.isEmpty()) {
      try
      {
        List<ASTName> xpathLogNames = this.astNamewithLog;
        for (ASTName name : xpathLogNames)
        {
          String imageString = name.getImage();
          if ((imageString != null) && (imageString.contains("Log."))) {
            FindAssignment(name, data);
          }
        }
      }
      catch (Exception e) {}
    }
  }
  
  private void FindAssignment(ASTName name, Object data)
  {
    ASTPrimaryExpression primaryExpression = (ASTPrimaryExpression)name.getFirstParentOfType(ASTPrimaryExpression.class);
    if (primaryExpression.findDescendantsOfType(ASTAssignmentOperator.class).size() > 0) {
      addViolation(data, name);
    }
    if (primaryExpression.findDescendantsOfType(ASTPostfixExpression.class).size() > 0) {
      addViolation(data, name);
    }
  }
  
  private void pickUpLogMethods(Node node)
  {
    List<ASTStatementExpression> pexs = node.findDescendantsOfType(ASTStatementExpression.class);
    for (ASTStatementExpression ast : pexs)
    {
      ASTPrimaryPrefix primaryPrefix = (ASTPrimaryPrefix)ast.jjtGetChild(0).getFirstDescendantOfType(ASTPrimaryPrefix.class);
      if (primaryPrefix != null)
      {
        ASTName name = (ASTName)primaryPrefix.getFirstChildOfType(ASTName.class);
        if (name != null)
        {
          String imageString = name.getImage();
          if (imageString.startsWith("Log.")) {
            this.astNamewithLog.add(name);
          }
        }
      }
    }
  }
}
