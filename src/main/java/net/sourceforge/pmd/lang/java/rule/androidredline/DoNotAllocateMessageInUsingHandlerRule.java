package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoNotAllocateMessageInUsingHandlerRule
  extends AbstractJavaRule
{
  private List<String> handlerVars = new ArrayList();
  private Map<String, Node> messageVars = new HashMap();
  
  public Object visit(ASTClassOrInterfaceDeclaration node, Object data)
  {
    List<ASTFieldDeclaration> fields = node.findDescendantsOfType(ASTFieldDeclaration.class);
    List<ASTLocalVariableDeclaration> locals = node.findDescendantsOfType(ASTLocalVariableDeclaration.class);
    for (ASTFieldDeclaration field : fields)
    {
      ASTType type = (ASTType)field.getFirstChildOfType(ASTType.class);
      if (type != null)
      {
        String image = type.getTypeImage();
        if ((image != null) && (("Handler".equals(image)) || ("android.os.Handler".equals(image)))) {
          this.handlerVars.add(field.getVariableName());
        }
      }
    }
    for (ASTLocalVariableDeclaration var : locals)
    {
      ASTType type = (ASTType)var.getFirstChildOfType(ASTType.class);
      if (type != null)
      {
        String image = type.getTypeImage();
        if ((image != null) && (("Handler".equals(image)) || ("android.os.Handler".equals(image)))) {
          this.handlerVars.add(var.getVariableName());
        }
      }
    }
    List<ASTAllocationExpression> allocations = node.findDescendantsOfType(ASTAllocationExpression.class);
    for (ASTAllocationExpression alloc : allocations)
    {
      ASTClassOrInterfaceType type = (ASTClassOrInterfaceType)alloc.getFirstChildOfType(ASTClassOrInterfaceType.class);
      if (type != null)
      {
        String image = type.getImage();
        if (("Message".equals(image)) || ("android.os.Message".equals(image)))
        {
          Node var = alloc.getNthParent(5);
          if ((var != null) && ((var instanceof ASTVariableDeclarator)))
          {
            ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)var.getFirstChildOfType(ASTVariableDeclaratorId.class);
            
            this.messageVars.put(id.getImage(), alloc);
          }
        }
      }
    }
    return super.visit(node, data);
  }
  
  public Object visit(ASTName node, Object data)
  {
    String image = node.getImage();
    if ((image == null) || (!image.contains("."))) {
      return super.visit(node, data);
    }
    String[] method = image.split("\\.");
    if ((method == null) || (method.length != 2)) {
      return super.visit(node, data);
    }
    if (("sendToTarget".equals(method[1])) && 
      (this.messageVars.containsKey(method[0]))) {
      addViolation(data, node);
    }
    if ((this.handlerVars.contains(method[0])) && (isSendMessage(method[1]))) {
      checkArguments(node, data);
    }
    return super.visit(node, data);
  }
  
  private void checkArguments(ASTName node, Object data)
  {
    Node p = node.getNthParent(2);
    if ((p != null) && ((p instanceof ASTPrimaryExpression)))
    {
      ASTArguments args = (ASTArguments)p.getFirstDescendantOfType(ASTArguments.class);
      List<ASTPrimaryPrefix> prefixs = args.findDescendantsOfType(ASTPrimaryPrefix.class);
      for (ASTPrimaryPrefix prefix : prefixs)
      {
        ASTName name = (ASTName)prefix.getFirstChildOfType(ASTName.class);
        if ((name != null) && (this.messageVars.containsKey(name.getImage()))) {
          addViolation(data, node);
        }
      }
    }
  }
  
  private boolean isSendMessage(String str)
  {
    return (("sendMessage".equals(str) | "sendMessageAtFrontOfQueue".equals(str))) || ("sendMessageAtTime".equals(str)) || ("sendMessageDelayed".equals(str));
  }
}
