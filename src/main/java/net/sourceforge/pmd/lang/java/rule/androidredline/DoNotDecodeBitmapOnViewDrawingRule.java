package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.ArrayList;
import java.util.List;

public class DoNotDecodeBitmapOnViewDrawingRule
  extends AbstractJavaRule
{
  static List<String> clsNameList = new ArrayList();
  static List<String> simpleClsNameList = new ArrayList();
  
  public Object visit(ASTClassOrInterfaceDeclaration node, Object data)
  {
    String image = node.getImage();
    if ((image != null) && (!image.endsWith("View"))) {
      return data;
    }
    return super.visit(node, data);
  }
  
  public Object visit(ASTMethodDeclaration node, Object data)
  {
    ASTMethodDeclarator declarator = (ASTMethodDeclarator)node.getFirstChildOfType(ASTMethodDeclarator.class);
    boolean isOnDraw = isOnDrawMethod(declarator);
    if (isOnDraw)
    {
      List<ASTName> names = node.findDescendantsOfType(ASTName.class);
      for (ASTName name : names)
      {
        String image = name.getImage();
        if ((image != null) && ((image.startsWith("BitmapFactory.decode")) || (image.startsWith("android.graphics.BitmapFactory.decode")))) {
          addViolation(data, name);
        }
      }
    }
    return super.visit(node, data);
  }
  
  private boolean isOnDrawMethod(ASTMethodDeclarator node)
  {
    if (!node.hasImageEqualTo("onDraw")) {
      return false;
    }
    int iFormalParams = 0;
    String paramName = null;
    for (int ix = 0; ix < node.jjtGetNumChildren(); ix++)
    {
      Node sn = node.jjtGetChild(ix);
      if ((sn instanceof ASTFormalParameters))
      {
        List<ASTFormalParameter> allParams = ((ASTFormalParameters)sn).findChildrenOfType(ASTFormalParameter.class);
        for (ASTFormalParameter formalParam : allParams)
        {
          iFormalParams++;
          ASTClassOrInterfaceType param = (ASTClassOrInterfaceType)formalParam.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
          if (param != null) {
            paramName = param.getImage();
          }
        }
      }
    }
    if ((iFormalParams == 1) && (("Canvas".equals(paramName)) || ("android.graphics.Canvas".equals(paramName)))) {
      return true;
    }
    return false;
  }
}
