package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBooleanLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.util.redline.util.Q;

public class WebViewRule
  extends AbstractJavaRule
{
  private boolean flag1 = false;
  private boolean flag2 = false;
  private boolean flag3 = false;
  
  public Object visit(ASTClassOrInterfaceType node, Object data)
  {
    this.flag3 = CheckCommonWebView(node, data);
    return super.visit(node, data);
  }
  
  public Object visit(ASTPrimarySuffix node, Object data)
  {
    if (!this.flag3) {
      CheckWebViewRule(node, data);
    }
    return super.visit(node, data);
  }
  
  private boolean CheckCommonWebView(ASTClassOrInterfaceType node, Object data)
  {
    String webViewString = node.getImage();
    if (webViewString.equalsIgnoreCase("CommonWebView")) {
      return true;
    }
    return false;
  }
  
  private void CheckWebViewRule(ASTPrimarySuffix node, Object data)
  {
    if ((node.jjtGetNumChildren() < 1) && (!Q.isEmptyString(node.getImage())))
    {
      String imageString = node.getImage();
      if ((imageString.equalsIgnoreCase("setJavaScriptEnabled")) && (node.jjtGetParent().hasDescendantOfType(ASTBooleanLiteral.class)))
      {
        ASTBooleanLiteral booleanLiteral1 = (ASTBooleanLiteral)node.jjtGetParent().getFirstDescendantOfType(ASTBooleanLiteral.class);
        if (booleanLiteral1.isTrue()) {
          this.flag1 = true;
        }
      }
      if ((imageString.equalsIgnoreCase("setAllowFileAccess")) && (node.jjtGetParent().hasDescendantOfType(ASTBooleanLiteral.class)))
      {
        ASTBooleanLiteral booleanLiteral2 = (ASTBooleanLiteral)node.jjtGetParent().getFirstDescendantOfType(ASTBooleanLiteral.class);
        if (booleanLiteral2.isTrue()) {
          this.flag2 = true;
        }
      }
      if ((this.flag1) && (this.flag2))
      {
        this.flag1 = false;
        this.flag2 = false;
        this.flag3 = false;
        addViolation(data, (Node)node.getFirstParentOfType(ASTBlock.class));
      }
    }
  }
}
