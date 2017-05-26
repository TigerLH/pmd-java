package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoNotAllocateObjectInonDrawRule
  extends AbstractJavaRule
{
  private static Set<String> doNotAllocationClazzName = new HashSet();
  
  static
  {
    doNotAllocationClazzName.add("Paint");
    doNotAllocationClazzName.add("Rect");
    doNotAllocationClazzName.add("RectF");
  }
  
  public Object visit(ASTMethodDeclaration node, Object data)
  {
    if (isOnDrawMethod(node))
    {
      List<ASTAllocationExpression> allocationExpressions = node.findDescendantsOfType(ASTAllocationExpression.class);
      for (ASTAllocationExpression express : allocationExpressions)
      {
        ASTClassOrInterfaceType classOrInterfaceType = (ASTClassOrInterfaceType)express.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
        if (doNotAllocationClazzName.contains(classOrInterfaceType.getImage())) {
          addViolation(data, express);
        }
      }
    }
    return super.visit(node, data);
  }
  
  private boolean isOnDrawMethod(ASTMethodDeclaration node)
  {
    ASTResultType resultType = node.getResultType();
    if (!node.isVoid()) {
      return false;
    }
    ASTMethodDeclarator methodDeclarator = (ASTMethodDeclarator)node.getFirstDescendantOfType(ASTMethodDeclarator.class);
    if (!"onDraw".equals(methodDeclarator.getImage())) {
      return false;
    }
    ASTFormalParameters formalParameters = (ASTFormalParameters)methodDeclarator.getFirstDescendantOfType(ASTFormalParameters.class);
    if (methodDeclarator.getParameterCount() != 1) {
      return false;
    }
    if ((formalParameters != null) && (formalParameters.getParameterCount() == 1))
    {
      String img = ((ASTClassOrInterfaceType)((ASTFormalParameter)formalParameters.getFirstDescendantOfType(ASTFormalParameter.class)).getFirstDescendantOfType(ASTClassOrInterfaceType.class)).getImage();
      if (!"Canvas".equals(img)) {
        return false;
      }
    }
    else
    {
      return false;
    }
    return true;
  }
}
