package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class StringRule
  extends AbstractJavaRule
{
  String xpathLocalString = ".//BlockStatement/LocalVariableDeclaration/VariableDeclarator/VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Literal";
  String xpathGlobalString = ".//ClassOrInterfaceBodyDeclaration//FieldDeclaration/VariableDeclarator/VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Literal";
  String xpathGlobalDefaultString = ".//ClassOrInterfaceBodyDeclaration//FieldDeclaration/VariableDeclarator";
  String xpathTypeString = ".//ClassOrInterfaceBodyDeclaration//FieldDeclaration/Type/ReferenceType/ClassOrInterfaceType";
  private boolean flag = false;
  
  public Object visit(ASTCompilationUnit node, Object data)
  {
    checkLogRule(node, data);
    return super.visit(node, data);
  }
  
  private void checkLogRule(ASTCompilationUnit node, Object data) {}
}
