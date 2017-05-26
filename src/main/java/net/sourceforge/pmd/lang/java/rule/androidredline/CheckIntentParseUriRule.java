package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class CheckIntentParseUriRule
  extends AbstractJavaRule
{
  private Boolean flag1 = Boolean.valueOf(false);
  private Boolean flag2 = Boolean.valueOf(false);
  private Boolean flag3 = Boolean.valueOf(false);
  private String vString = "";
  
  public Object visit(ASTPrimaryPrefix node, Object data)
  {
    CheckFourConditions(node, data);
    return super.visit(node, data);
  }
  
  private void CheckFourConditions(ASTPrimaryPrefix node, Object data)
  {
    try
    {
      if ((node.jjtGetNumChildren() > 0) && ((node.jjtGetChild(0) instanceof ASTName)))
      {
        String astString = node.jjtGetChild(0).getImage();
        if (astString.equalsIgnoreCase("Intent.parseUri"))
        {
          ASTVariableDeclarator vD = (ASTVariableDeclarator)node.getFirstParentOfType(ASTVariableDeclarator.class);
          if ((vD != null) && (vD.jjtGetNumChildren() > 0) && ((vD.jjtGetChild(0) instanceof ASTVariableDeclaratorId)))
          {
            this.vString = vD.jjtGetChild(0).getImage();
            checkOtherThree(this.vString, node, data);
          }
        }
      }
    }
    catch (Exception e) {}
  }
  
  private void checkOtherThree(String vString2, ASTPrimaryPrefix node, Object data)
  {
    try
    {
      ASTClassOrInterfaceBodyDeclaration COIBD = (ASTClassOrInterfaceBodyDeclaration)node.getFirstParentOfType(ASTClassOrInterfaceBodyDeclaration.class);
      if (COIBD != null)
      {
        List<ASTPrimaryPrefix> Prefixs = COIBD.findDescendantsOfType(ASTPrimaryPrefix.class);
        if (Prefixs.size() > 3) {
          for (ASTPrimaryPrefix Prefix : Prefixs) {
            if ((Prefix.jjtGetNumChildren() > 0) && ((Prefix.jjtGetChild(0) instanceof ASTName)))
            {
              String astString1 = Prefix.jjtGetChild(0).getImage();
              if (astString1.equalsIgnoreCase(this.vString + ".addCategory")) {
                if (((Prefix.jjtGetParent() instanceof ASTPrimaryExpression)) && 
                  (Prefix.jjtGetParent().hasDescendantOfType(ASTLiteral.class)))
                {
                  List<ASTLiteral> Literals = Prefix.jjtGetParent().findDescendantsOfType(ASTLiteral.class);
                  if (Literals.size() > 0) {
                    for (ASTLiteral Literal : Literals) {
                      if (Literal.getImage().equalsIgnoreCase("\"android.intent.category.BROWSABLE\""))
                      {
                        this.flag1 = Boolean.valueOf(true);
                        break;
                      }
                    }
                  }
                }
              }
              if (astString1.equalsIgnoreCase(this.vString + ".setComponent")) {
                if (((Prefix.jjtGetParent() instanceof ASTPrimaryExpression)) && 
                  (Prefix.jjtGetParent().hasDescendantOfType(ASTNullLiteral.class))) {
                  this.flag2 = Boolean.valueOf(true);
                }
              }
              if (astString1.equalsIgnoreCase(this.vString + ".setSelector")) {
                if (((Prefix.jjtGetParent() instanceof ASTPrimaryExpression)) && 
                  (Prefix.jjtGetParent().hasDescendantOfType(ASTNullLiteral.class))) {
                  this.flag3 = Boolean.valueOf(true);
                }
              }
            }
          }
        }
        if ((!this.flag1.booleanValue()) || (!this.flag2.booleanValue()) || (!this.flag3.booleanValue())) {
          addViolation(data, node);
        }
      }
    }
    catch (Exception e) {}finally
    {
      this.flag1 = Boolean.valueOf(false);
      this.flag2 = Boolean.valueOf(false);
      this.flag3 = Boolean.valueOf(false);
    }
  }
}
