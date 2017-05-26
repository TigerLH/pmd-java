package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class FragmentRule
  extends AbstractJavaRule
{
  private boolean override = false;
  
  public Object visit(ASTClassOrInterfaceDeclaration node, Object data)
  {
    if (node.isInterface()) {
      return data;
    }
    if ((node.jjtGetChild(0) != null) && ((node.jjtGetChild(0) instanceof ASTExtendsList)) && 
      (node.jjtGetChild(0).jjtGetChild(0) != null) && ((node.jjtGetChild(0).jjtGetChild(0) instanceof ASTClassOrInterfaceType)))
    {
      String aString = node.jjtGetChild(0).jjtGetChild(0).getImage();
      if (aString.equalsIgnoreCase("PreferenceActivity"))
      {
        checkFragmentOverRide(node, data);
        return super.visit(node, data);
      }
    }
    return super.visit(node, data);
  }
  
  private void checkFragmentOverRide(Node node, Object data)
  {
    try
    {
      List<ASTMethodDeclarator> methodDeclarators = node.findDescendantsOfType(ASTMethodDeclarator.class);
      if (methodDeclarators.size() > 0) {
        for (ASTMethodDeclarator methodDeclarator : methodDeclarators) {
          if (methodDeclarator.getImage().equalsIgnoreCase("isValidFragment"))
          {
            ASTClassOrInterfaceBodyDeclaration aBodyDeclaration = (ASTClassOrInterfaceBodyDeclaration)methodDeclarator.getFirstParentOfType(ASTClassOrInterfaceBodyDeclaration.class);
            if ((aBodyDeclaration.jjtGetChild(0) != null) && ((aBodyDeclaration.jjtGetChild(0) instanceof ASTAnnotation)) && 
              (aBodyDeclaration.jjtGetChild(0).jjtGetChild(0) != null) && ((aBodyDeclaration.jjtGetChild(0).jjtGetChild(0) instanceof ASTMarkerAnnotation)) && 
              (aBodyDeclaration.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0) != null) && ((aBodyDeclaration.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0) instanceof ASTName)))
            {
              String nameString = aBodyDeclaration.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).getImage();
              if (nameString.equalsIgnoreCase("Override"))
              {
                this.override = true;
                break;
              }
            }
          }
        }
      }
      if (!this.override) {
        addViolation(data, node.jjtGetChild(0).jjtGetChild(0));
      }
    }
    catch (Exception e) {}finally
    {
      this.override = false;
    }
  }
}
