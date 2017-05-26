package net.sourceforge.pmd.util.redline.util;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.jaxen.JaxenException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class RedUtil
{
  public static List<ASTLocalVariableDeclaration> findAllLocalVariableDeclarationByClassName(ASTMethodDeclaration methodNode, String fullClassName)
  {
    List<ASTLocalVariableDeclaration> list = new ArrayList();
    List<ASTLocalVariableDeclaration> vars = methodNode.findDescendantsOfType(ASTLocalVariableDeclaration.class);
    for (ASTLocalVariableDeclaration var : vars)
    {
      ASTType type = (ASTType)var.getFirstChildOfType(ASTType.class);
      String img = type.getTypeImage();
      if ((!Q.isEmptyString(img)) && (isEqualClassName(img, fullClassName))) {
        list.add(var);
      }
    }
    return list;
  }
  
  public static boolean isEqualClassName(String className, String fullClassName)
  {
    return (className.equals(fullClassName)) || (className.equals(toSimpleType(fullClassName)));
  }
  
  public static String toSimpleType(String fullyQualifiedClassName)
  {
    int lastIndexOf = fullyQualifiedClassName.lastIndexOf('.');
    if (lastIndexOf > -1) {
      return fullyQualifiedClassName.substring(lastIndexOf + 1);
    }
    return fullyQualifiedClassName;
  }
  
  public static ASTVariableDeclaratorId getVariableDeclaratorId(ASTClassOrInterfaceType astClassOrInterfaceType)
  {
    ASTVariableDeclarator declarator = (ASTVariableDeclarator)astClassOrInterfaceType.jjtGetParent().jjtGetParent().jjtGetParent().getFirstChildOfType(ASTVariableDeclarator.class);
    if (declarator != null)
    {
      ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)declarator.getFirstChildOfType(ASTVariableDeclaratorId.class);
      return id;
    }
    return null;
  }
  
  public static String getVariableName(ASTLocalVariableDeclaration localVariableDeclaration)
  {
    ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator)localVariableDeclaration.getFirstChildOfType(ASTVariableDeclarator.class);
    if (variableDeclarator != null)
    {
      ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)variableDeclarator.getFirstChildOfType(ASTVariableDeclaratorId.class);
      if (id != null) {
        return id.getImage();
      }
    }
    return null;
  }
  
  public static String getVariableName(ASTFieldDeclaration fieldDeclaration)
  {
    ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator)fieldDeclaration.getFirstChildOfType(ASTVariableDeclarator.class);
    if (variableDeclarator != null)
    {
      ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)variableDeclarator.getFirstChildOfType(ASTVariableDeclaratorId.class);
      if (id != null) {
        return id.getImage();
      }
    }
    return null;
  }
  
  public static boolean isVariablePassedToMethod(ASTPrimaryExpression expr, String variable)
  {
    List<ASTName> methodParams = new ArrayList();
    expr.findDescendantsOfType(ASTName.class, methodParams, true);
    for (ASTName pName : methodParams)
    {
      String paramName = pName.getImage();
      
      ASTArgumentList parentParam = (ASTArgumentList)pName.getFirstParentOfType(ASTArgumentList.class);
      if ((paramName.equals(variable)) && (parentParam != null)) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isVariablePassedToMethod(Node ancestorNode, Node variableNode, String variableName)
  {
    List<ASTPrimaryExpression> exprs = new ArrayList();
    ancestorNode.findDescendantsOfType(ASTPrimaryExpression.class, exprs, true);
    for (ASTPrimaryExpression expr : exprs) {
      if ((expr.getBeginLine() > variableNode.getBeginLine()) && 
        (isVariablePassedToMethod(expr, variableName))) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isVariablePassedToMethod(Node ancestorNode, Node variableNode, String variableName, String methodName)
  {
    List<ASTPrimaryExpression> exprs = new ArrayList();
    ancestorNode.findDescendantsOfType(ASTPrimaryExpression.class, exprs, true);
    for (ASTPrimaryExpression expr : exprs) {
      if ((expr.getBeginLine() > variableNode.getBeginLine()) && 
        (isVariablePassedToMethod(expr, variableName)))
      {
        ASTPrimaryPrefix prefix = (ASTPrimaryPrefix)expr.getFirstChildOfType(ASTPrimaryPrefix.class);
        if (prefix != null)
        {
          ASTName name = (ASTName)prefix.getFirstChildOfType(ASTName.class);
          if ((name != null) && 
            (name.getImage() != null) && 
            (name.getImage().contains(methodName))) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  public static boolean isVariablePassedToMethods(Node ancestorNode, Node variableNode, String variableName, List<String> methodNames)
  {
    ASTName name;
    if ((methodNames == null) || (methodNames.size() == 0)) {
      return false;
    }
    List<ASTPrimaryExpression> exprs = new ArrayList();
    ancestorNode.findDescendantsOfType(ASTPrimaryExpression.class, exprs, true);
    for (ASTPrimaryExpression expr : exprs) {
      if ((expr.getBeginLine() > variableNode.getBeginLine()) && 
        (isVariablePassedToMethod(expr, variableName)))
      {
        ASTPrimaryPrefix prefix = (ASTPrimaryPrefix)expr.getFirstChildOfType(ASTPrimaryPrefix.class);
        if (prefix != null)
        {
           name = (ASTName)prefix.getFirstChildOfType(ASTName.class);
          if ((name != null) && 
            (name.getImage() != null)) {
            for (String methodName : methodNames) {
              if (name.getImage().contains(methodName)) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  public static boolean isVariableReturnByMethod(Node methodNode, String variableName)
  {
    List<ASTReturnStatement> returns = new ArrayList();
    methodNode.findDescendantsOfType(ASTReturnStatement.class, returns, true);
    for (ASTReturnStatement returnStatement : returns)
    {
      ASTName name = (ASTName)returnStatement.getFirstDescendantOfType(ASTName.class);
      if ((name != null) && (name.getImage().equals(variableName))) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isMethodParameterContainType(Node methodNode, String typeName)
  {
    String xpathString = ".//FormalParameters/FormalParameter/Type/ReferenceType/ClassOrInterfaceType[@Image='" + typeName + "']";
    if (methodNode.hasDescendantMatchingXPath(xpathString)) {
      return true;
    }
    return false;
  }
  
  public static void ensureMethodParameterCallMethodInMethod(Node methodNode, String typeName, String callMethod, AbstractJavaRule rule, Object data)
  {
    String xpathString = ".//FormalParameters/FormalParameter/Type/ReferenceType/ClassOrInterfaceType[@Image='" + typeName + "']";
    if (methodNode.hasDescendantMatchingXPath(xpathString)) {
      try
      {
        List<ASTClassOrInterfaceType> types = (List<ASTClassOrInterfaceType>)methodNode.findChildNodesWithXPath(xpathString);
        for (ASTClassOrInterfaceType type : types)
        {
          ASTFormalParameter formal = (ASTFormalParameter)type.jjtGetParent().jjtGetParent().jjtGetParent();
          ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)formal.getFirstChildOfType(ASTVariableDeclaratorId.class);
          String variableName = id.getImage();
          if (!formal.isVarargs())
          {
            String callXpath = ".//PrimaryExpression/PrimaryPrefix/Name[@Image='" + variableName + "." + callMethod + "']";
            if ((!methodNode.hasDescendantMatchingXPath(callXpath)) && 
              (!isVariableReturnByMethod(methodNode, id.getImage())) && 
              (!isVariablePassedToMethod(methodNode, id, id.getImage()))) {
              rule.addViolation(data, id, id.getImage());
            }
          }
          else
          {
            List<ASTForStatement> forStatements = methodNode.findDescendantsOfType(ASTForStatement.class);
            boolean isCalled = false;
            for (ASTForStatement forStatement : forStatements)
            {
              if ((forStatement.hasDescendantMatchingXPath(".//Expression/PrimaryExpression/PrimaryPrefix/Name[@Image='" + variableName + "']")) && 
                (forStatement.hasDescendantMatchingXPath(".//LocalVariableDeclaration/Type/ReferenceType/ClassOrInterfaceType[@Image='" + typeName + "']")))
              {
                String varPathString = ".//LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId";
                if (forStatement.hasDescendantMatchingXPath(varPathString))
                {
                  List<Node> ids = forStatement.findChildNodesWithXPath(varPathString);
                  for (Node varid : ids)
                  {
                    String varName = varid.getImage();
                    String closeXpath = ".//PrimaryExpression/PrimaryPrefix/Name[@Image='" + varName + "." + callMethod + "']";
                    if (forStatement.hasDescendantMatchingXPath(closeXpath))
                    {
                      isCalled = true;
                      break;
                    }
                    if (isVariablePassedToMethod(methodNode, varid, varid.getImage()))
                    {
                      isCalled = true;
                      break;
                    }
                  }
                }
              }
              if (isCalled) {
                break;
              }
            }
            if (!isCalled) {
              rule.addViolation(data, id, id.getImage());
            }
          }
        }
      }
      catch (JaxenException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public static int countFileLinesNumber(String filePath)
  {
    File file = new File(filePath);
    long fileLength = file.length();
    LineNumberReader rf = null;
    try
    {
      rf = new LineNumberReader(new FileReader(file));
      if (rf != null)
      {
        int lines = 0;
        rf.skip(fileLength);
        lines = rf.getLineNumber();
        rf.close();
        return lines;
      }
    }
    catch (IOException e)
    {
      if (rf != null) {
        try
        {
          rf.close();
        }
        catch (IOException ee) {}
      }
    }
    return 0;
  }
}
