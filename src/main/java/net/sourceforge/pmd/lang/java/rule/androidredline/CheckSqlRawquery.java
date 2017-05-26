package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class CheckSqlRawquery
  extends AbstractJavaRule
{
  public Object visit(ASTPrimaryPrefix node, Object data)
  {
    if ((node.jjtGetNumChildren() == 0) || (!(node.jjtGetChild(0) instanceof ASTName))) {
      return super.visit(node, data);
    }
    String image = ((ASTName)node.jjtGetChild(0)).getImage();
    if (image.contains(".rawQuery"))
    {
      String[] parts = image.split("\\.");
      if ((parts.length == 2) && (parts[1].equals("rawQuery")))
      {
        String methodName = parts[0];
        if (methodName.equals("SQLiteDatabase"))
        {
          addViolation(data, (ASTName)node.jjtGetChild(0));
          return super.visit(node, data);
        }
        if (checkMethodName(methodName, node))
        {
          addViolation(data, (ASTName)node.jjtGetChild(0));
          return super.visit(node, data);
        }
      }
    }
    return super.visit(node, data);
  }
  
  private boolean checkMethodName(String methodName, Node node)
  {
    ASTCompilationUnit compilationUnit = (ASTCompilationUnit)node.getFirstParentOfType(ASTCompilationUnit.class);
    List<ASTVariableDeclaratorId> variableDeclaratorIds = compilationUnit.findDescendantsOfType(ASTVariableDeclaratorId.class);
    if (variableDeclaratorIds.size() > 0) {
      for (ASTVariableDeclaratorId variableDeclaratorId : variableDeclaratorIds) {
        if (variableDeclaratorId.getImage().equals(methodName))
        {
          ASTClassOrInterfaceType classOrInterfaceType = null;
          if ((variableDeclaratorId.jjtGetParent().jjtGetParent() instanceof ASTLocalVariableDeclaration)) {
            classOrInterfaceType = (ASTClassOrInterfaceType)((ASTLocalVariableDeclaration)variableDeclaratorId.getFirstParentOfType(ASTLocalVariableDeclaration.class)).getFirstDescendantOfType(ASTClassOrInterfaceType.class);
          }
          if ((variableDeclaratorId.jjtGetParent().jjtGetParent() instanceof ASTFieldDeclaration)) {
            classOrInterfaceType = (ASTClassOrInterfaceType)((ASTFieldDeclaration)variableDeclaratorId.getFirstParentOfType(ASTFieldDeclaration.class)).getFirstDescendantOfType(ASTClassOrInterfaceType.class);
          }
          if ((classOrInterfaceType != null) && (classOrInterfaceType.getImage().equals("SQLiteDatabase"))) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
