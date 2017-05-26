package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class CheckOverrideOpenFile
  extends AbstractJavaRule
{
  private Boolean flagBoolean = Boolean.valueOf(false);
  
  public Object visit(ASTMethodDeclarator node, Object data)
  {
    if ((node != null) && (node.getImage().equalsIgnoreCase("openFile")))
    {
      checkOpenFile(node, data);
      return super.visit(node, data);
    }
    return super.visit(node, data);
  }
  
  private void checkOpenFile(Node node, Object data)
  {
    try
    {
      ASTClassOrInterfaceBodyDeclaration classOBD = (ASTClassOrInterfaceBodyDeclaration)node.getFirstParentOfType(ASTClassOrInterfaceBodyDeclaration.class);
      ASTName astName = (ASTName)classOBD.getFirstDescendantOfType(ASTName.class);
      if (astName != null)
      {
        String astnameString = astName.getImage();
        if (astnameString.equalsIgnoreCase("Override"))
        {
          List<ASTName> astNames = classOBD.findDescendantsOfType(ASTName.class);
          if (astNames.size() > 0)
          {
            for (ASTName astName2 : astNames) {
              if (astName2.getImage().contains(".getCanonicalPath")) {
                this.flagBoolean = Boolean.valueOf(true);
              }
            }
            if ((!this.flagBoolean.booleanValue()) && 
              (classOBD.getFirstDescendantOfType(ASTMethodDeclaration.class) != null)) {
              addViolation(data, (Node)classOBD.getFirstDescendantOfType(ASTMethodDeclaration.class));
            }
          }
        }
      }
    }
    catch (Exception e) {}finally
    {
      this.flagBoolean = Boolean.valueOf(false);
    }
  }
}
