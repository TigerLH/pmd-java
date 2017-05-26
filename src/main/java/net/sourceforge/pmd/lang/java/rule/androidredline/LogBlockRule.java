package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.jaxen.JaxenException;

import java.util.*;

public class LogBlockRule
  extends AbstractJavaRule
{
  private static Set<String> SensitiveStrings = new HashSet();
  private List<ASTName> astNamewithLog = new ArrayList();
  private List<ASTName> SASTNames = new ArrayList();
  private List<ASTVariableDeclaratorId> SensitiveVariables = new ArrayList();
  private List<String> BooleanStrings = new ArrayList();
  
  static
  {
    SensitiveStrings.add("classname");
    SensitiveStrings.add("pid");
    SensitiveStrings.add("uid");
    SensitiveStrings.add("imei");
    SensitiveStrings.add("getLocalClassName");
    
    SensitiveStrings.add("getPackagePath");
    SensitiveStrings.add("android.os.Process.myPid");
    SensitiveStrings.add("android.os.Process.myUid");
    SensitiveStrings.add("android.os.Process.getUidForName");
  }
  
  public Object visit(ASTCompilationUnit node, Object data)
  {
    checkLogRule(node, data);
    return super.visit(node, data);
  }
  
  private void checkLogRule(Node node, Object data)
  {
    String xpathBoolean = ".//FieldDeclaration/VariableDeclarator/VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Literal/BooleanLiteral[@True='true']";

    pickUpLogMethods(node);
    if (!this.astNamewithLog.isEmpty()) {
      try
      {
        List<ASTBooleanLiteral> xpathBooleanStringNames = (List<ASTBooleanLiteral>)node.findChildNodesWithXPath(xpathBoolean);
        if (xpathBooleanStringNames.size() > 0) {
          for (ASTBooleanLiteral booleanLiteral : xpathBooleanStringNames)
          {
            ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator)booleanLiteral.getFirstParentOfType(ASTVariableDeclarator.class);
            ASTVariableDeclaratorId variableDeclaratorId = (ASTVariableDeclaratorId)variableDeclarator.getFirstChildOfType(ASTVariableDeclaratorId.class);
            this.BooleanStrings.add(variableDeclaratorId.getImage());
          }
        }
        List<ASTName> xpathLogNames = this.astNamewithLog;
        if (xpathLogNames.size() > 0) {
          ASTIfStatement ifStatement;
          for (ASTName name : xpathLogNames)
          {
            String imageString = name.getImage();
            if ((imageString != null) && (imageString.contains("Log.")))
            {
              ifStatement = (ASTIfStatement)name.getFirstParentOfType(ASTIfStatement.class);
              ASTBlockStatement blockStatement = (ASTBlockStatement)name.getFirstParentOfType(ASTBlockStatement.class);
              List<ASTName> names2 = blockStatement.findDescendantsOfType(ASTName.class);
              if (names2.size() > 0) {
                for (ASTName name2 : names2) {
                  if (name2 != null)
                  {
                    String imageString2 = name2.getImage();
                    
                    boolean sflag = CheckIsSensitiveString(imageString2);
                    if (!sflag) {
                      this.SASTNames.add(name2);
                    }
                    if (sflag) {
                      if (ifStatement != null)
                      {
                        ASTExpression astExpression = (ASTExpression)ifStatement.getFirstDescendantOfType(ASTExpression.class);
                        ASTName astName = (ASTName)astExpression.getFirstDescendantOfType(ASTName.class);
                        if (astName != null)
                        {
                          String astNameString = astName.getImage();
                          if ((this.BooleanStrings.size() > 0) && (this.BooleanStrings.contains(astNameString))) {
                            addViolation(data, name2);
                          }
                        }
                      }
                      else
                      {
                        addViolation(data, name2);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        List<ASTVariableDeclaratorId> variableDeclaratorIds = node.findDescendantsOfType(ASTVariableDeclaratorId.class);
        if (variableDeclaratorIds.size() > 0)
        {
          Iterator i$;
          ASTVariableDeclaratorId SensitiveVariable;
          for (ASTVariableDeclaratorId variableDeclaratorId : variableDeclaratorIds) {
            if ((variableDeclaratorId.jjtGetParent() instanceof ASTVariableDeclarator))
            {
              ASTName astName = (ASTName)((ASTVariableDeclarator)variableDeclaratorId.getFirstParentOfType(ASTVariableDeclarator.class)).getFirstDescendantOfType(ASTName.class);
              if ((astName != null) && 
                (CheckIsSensitiveString(astName.getImage()))) {
                this.SensitiveVariables.add(variableDeclaratorId);
              }
            }
          }
          if (this.SensitiveVariables.size() > 0) {
            for (i$ = this.SensitiveVariables.iterator(); i$.hasNext();)
            {
               SensitiveVariable = (ASTVariableDeclaratorId)i$.next();
              for (ASTName SecondastName : this.SASTNames)
              {
                String astNameimage = SecondastName.getImage();
                if ((!hasNullInitializer(SensitiveVariable)) && (astNameimage != null) && (SensitiveVariable.getImage().equalsIgnoreCase(astNameimage)))
                {
                  ASTIfStatement ifStatement = (ASTIfStatement)SecondastName.getFirstParentOfType(ASTIfStatement.class);
                  if (ifStatement != null)
                  {
                    ASTExpression astExpression = (ASTExpression)ifStatement.getFirstDescendantOfType(ASTExpression.class);
                    ASTName astName3 = (ASTName)astExpression.getFirstDescendantOfType(ASTName.class);
                    if (astName3 != null)
                    {
                      String astNameString = astName3.getImage();
                      if ((this.BooleanStrings.size() > 0) && (this.BooleanStrings.contains(astNameString))) {
                        addViolation(data, SecondastName);
                      }
                    }
                  }
                  else
                  {
                    addViolation(data, SecondastName);
                  }
                }
              }
            }
          }
        }
      }
      catch (JaxenException e)
      {
        e.printStackTrace();
      }
      finally
      {
        this.astNamewithLog.clear();
        this.SASTNames.clear();
        this.BooleanStrings.clear();
        this.SensitiveVariables.clear();
      }
    }
  }
  
  private boolean CheckIsSensitiveString(String imageString2)
  {
    for (String SensitiveString : SensitiveStrings)
    {
      if (imageString2.equalsIgnoreCase(SensitiveString)) {
        return true;
      }
      if ((imageString2 != null) && (imageString2.contains(".")))
      {
        String[] partStrings = imageString2.split("\\.");
        int LastIndex = partStrings.length - 1;
        if ((partStrings[LastIndex].equals("length")) || (partStrings[LastIndex].equals("size"))) {
          return false;
        }
        for (int i = 0; i < partStrings.length; i++)
        {
          String partString = partStrings[i];
          if (partString.equalsIgnoreCase(SensitiveString)) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private boolean hasNullInitializer(ASTVariableDeclaratorId var)
  {
    ASTVariableInitializer init = (ASTVariableInitializer)var.getFirstDescendantOfType(ASTVariableInitializer.class);
    if (init != null) {
      try
      {
        List<?> nulls = init.findChildNodesWithXPath("Expression/PrimaryExpression/PrimaryPrefix/Literal/NullLiteral");
        return !nulls.isEmpty();
      }
      catch (JaxenException e)
      {
        return false;
      }
    }
    return false;
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
