package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.jaxen.JaxenException;

import java.util.ArrayList;
import java.util.List;

public class LogOnOffRule
  extends AbstractJavaRule
{
  private List<String> BooleanStrings = new ArrayList();
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
      this.BooleanStrings.clear();
      this.astNamewithLog.clear();
    }
    return super.visit(compilationUnit, data);
  }
  
  private void checkLogRule(Node node, Object data)
    throws JaxenException
  {
    findBooleanStrings(node);
    pickUpLogMethods(node);
    if (!this.astNamewithLog.isEmpty())
    {
      List<ASTName> xpathLogNames = this.astNamewithLog;
      for (ASTName name : xpathLogNames)
      {
        ASTIfStatement ifStatement = (ASTIfStatement)name.getFirstParentOfType(ASTIfStatement.class);
        if (ifStatement != null)
        {
          ASTName astName = (ASTName)ifStatement.getFirstDescendantOfType(ASTName.class);
          if (astName != null)
          {
            String astNameString = astName.getImage();
            if ((this.BooleanStrings.size() != 0) && (this.BooleanStrings.contains(astNameString))) {
              addViolation(data, name);
            }
          }
          else
          {
            addViolation(data, name);
          }
        }
        else
        {
          addViolation(data, name);
        }
      }
    }
  }
  
  private void findBooleanStrings(Node node)
  {
    List<ASTBooleanLiteral> booleanLiterals = node.findDescendantsOfType(ASTBooleanLiteral.class);
    for (ASTBooleanLiteral booleanLiteral : booleanLiterals) {
      if ((!booleanLiteral.isTrue()) || (booleanLiteral.isTrue() == true))
      {
        ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator)booleanLiteral.getFirstParentOfType(ASTVariableDeclarator.class);
        ASTFieldDeclaration fieldDeclaration = (ASTFieldDeclaration)booleanLiteral.getFirstParentOfType(ASTFieldDeclaration.class);
        if ((variableDeclarator != null) && (fieldDeclaration == null) && 
          (variableDeclarator.jjtGetNumChildren() != 0) && ((variableDeclarator.jjtGetChild(0) instanceof ASTVariableDeclaratorId))) {
          this.BooleanStrings.add(variableDeclarator.jjtGetChild(0).getImage());
        }
      }
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
          if (imageString.toLowerCase().startsWith("log.")) {
            this.astNamewithLog.add(name);
          }
        }
      }
    }
  }
}
