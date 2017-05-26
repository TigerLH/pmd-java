package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class DeprecateSharedPreferencesRule
  extends AbstractJavaRule
{
  public Object visit(ASTFieldDeclaration node, Object data)
  {
    findSharedPreferencesDeclaration(node, data);
    return super.visit(node, data);
  }
  
  public Object visit(ASTLocalVariableDeclaration node, Object data)
  {
    findSharedPreferencesDeclaration(node, data);
    return super.visit(node, data);
  }
  
  public Object visit(ASTMethodDeclaration node, Object data)
  {
    List<ASTPrimaryExpression> expresses = node.findDescendantsOfType(ASTPrimaryExpression.class);
    for (ASTPrimaryExpression express : expresses)
    {
      List<ASTName> names = express.findDescendantsOfType(ASTName.class);
      for (ASTName name : names)
      {
        String image = name.getImage();
        if ((image == null) || (!image.endsWith(".getDefaultSharedPreferences"))) {}
      }
    }
    return super.visit(node, data);
  }
  
  private void findSharedPreferencesDeclaration(AbstractJavaAccessNode node, Object data)
  {
    ASTClassOrInterfaceType classOrInterfaceType = (ASTClassOrInterfaceType)node.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
    if ((classOrInterfaceType != null) && ("SharedPreferences".equals(classOrInterfaceType.getImage()))) {}
  }
}
