package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class SharedPerferenceDataRule
  extends AbstractJavaRule
{
  long start = System.currentTimeMillis();
  
  public Object visit(ASTPrimaryPrefix node, Object data)
  {
    if ((node.jjtGetNumChildren() == 0) || (!(node.jjtGetChild(0) instanceof ASTName))) {
      return super.visit(node, data);
    }
    String image = ((ASTName)node.jjtGetChild(0)).getImage();
    if ((image.endsWith(".putInt")) || (image.endsWith(".putString")))
    {
      checkInputKey((ASTName)node.jjtGetChild(0), data);
      return super.visit(node, data);
    }
    return super.visit(node, data);
  }
  
  private void checkInputKey(Node node, Object data)
  {
    try
    {
      ASTPrimaryExpression primaryExpression1 = (ASTPrimaryExpression)node.getFirstParentOfType(ASTPrimaryExpression.class);
      ASTLiteral keyname = (ASTLiteral)((ASTExpression)primaryExpression1.getFirstDescendantOfType(ASTExpression.class)).getFirstDescendantOfType(ASTLiteral.class);
      if (keyname != null)
      {
        String keynameString = keyname.getImage().toUpperCase();
        if (((keynameString.contains("PHONENUMBER")) || (keynameString.contains("SERVER_IP_ADDRESS"))) && 
          (checkSharedPreferencesDeclaration(keyname, data))) {
          addViolation(data, node);
        }
      }
    }
    catch (Exception e) {}
  }
  
  private boolean checkSharedPreferencesDeclaration(Node node, Object data)
  {
    boolean flag = false;
    List<ASTClassOrInterfaceType> classOrInterfaceTypes = ((ASTCompilationUnit)node.getFirstParentOfType(ASTCompilationUnit.class)).findDescendantsOfType(ASTClassOrInterfaceType.class);
    for (ASTClassOrInterfaceType classOrInterfaceType : classOrInterfaceTypes) {
      if ((classOrInterfaceType != null) && ("SharedPreferences".equals(classOrInterfaceType.getImage()))) {
        flag = true;
      }
    }
    return flag;
  }
}
