package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.ArrayList;
import java.util.List;

public class CheckFinishInAsyncTaskUpdateUIRule
  extends AbstractJavaRule
{
  long start = System.currentTimeMillis();
  private List<String> viewVars = new ArrayList();
  private List<String> activityVars = new ArrayList();
  
  public Object visit(ASTCompilationUnit node, Object data)
  {
    List<ASTFieldDeclaration> fields = node.findDescendantsOfType(ASTFieldDeclaration.class);
    
    List<ASTLocalVariableDeclaration> locals = node.findDescendantsOfType(ASTLocalVariableDeclaration.class);
    for (ASTFieldDeclaration field : fields)
    {
      ASTType type = (ASTType)field.getFirstChildOfType(ASTType.class);
      if (type != null)
      {
        String image = type.getTypeImage();
        if (image != null) {
          if (image.endsWith("View")) {
            this.viewVars.add(field.getVariableName());
          } else if (image.endsWith("Activity")) {
            this.activityVars.add(field.getVariableName());
          }
        }
      }
    }
    for (ASTLocalVariableDeclaration var : locals)
    {
      ASTType type = (ASTType)var.getFirstChildOfType(ASTType.class);
      if (type != null)
      {
        String image = type.getTypeImage();
        if (image != null) {
          if (image.endsWith("View")) {
            this.viewVars.add(var.getVariableName());
          } else if (image.endsWith("Activity")) {
            this.activityVars.add(var.getVariableName());
          }
        }
      }
    }
    return super.visit(node, data);
  }
  
  public Object visit(ASTName node, Object data)
  {
    ASTMethodDeclaration methodDeclar = (ASTMethodDeclaration)node.getFirstParentOfType(ASTMethodDeclaration.class);
    if ((methodDeclar == null) || (!"onPostExecute".equals(methodDeclar.getMethodName()))) {
      return super.visit(node, data);
    }
    String image = node.getImage();
    if ((image == null) || (!image.contains("."))) {
      return super.visit(node, data);
    }
    String[] method = image.split("\\.");
    if ((method == null) || (method.length != 2)) {
      return super.visit(node, data);
    }
    if (this.viewVars.contains(method[0]))
    {
      Node n = node.getNthParent(5);
      if ((n != null) && ((n instanceof ASTBlockStatement)))
      {
        ASTBlockStatement block = (ASTBlockStatement)n;
        Node n2 = block.getNthParent(3);
        if ((n2 != null) && ((n2 instanceof ASTIfStatement)))
        {
          ASTIfStatement ifStatement = (ASTIfStatement)n2;
          ASTPrimaryExpression exp = (ASTPrimaryExpression)ifStatement.getFirstDescendantOfType(ASTPrimaryExpression.class);
          
          ASTName prefix = (ASTName)((ASTPrimaryPrefix)exp.getFirstChildOfType(ASTPrimaryPrefix.class)).getFirstChildOfType(ASTName.class);
          
          boolean isCheck = false;
          if ((prefix != null) && (prefix.getImage() != null) && (prefix.getImage().contains("isFinishing"))) {
            isCheck = true;
          }
          List<ASTPrimarySuffix> suffixs = ((ASTPrimaryExpression)ifStatement.getFirstDescendantOfType(ASTPrimaryExpression.class)).findDescendantsOfType(ASTPrimarySuffix.class);
          for (ASTPrimarySuffix suffix : suffixs)
          {
            String img = suffix.getImage();
            if ("isFinishing".equals(img))
            {
              isCheck = true;
              break;
            }
          }
          if (!isCheck) {
            addViolation(data, node);
          }
        }
        else
        {
          addViolation(data, node);
        }
      }
    }
    return super.visit(node, data);
  }
}
