package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringMultiProperty;
import net.sourceforge.pmd.util.redline.util.RedUtil;

import java.util.*;

public class BitmapRecycleRule
  extends AbstractJavaRule
{
  private Set<String> types = new HashSet();
  private Set<String> simpleTypes = new HashSet();
  private static final StringMultiProperty TYPES_DESCRIPTOR = new StringMultiProperty("types", "Affected types", new String[] { "android.graphics.Bitmap" }, 2.0F, ',');
  
  public BitmapRecycleRule()
  {
    definePropertyDescriptor(TYPES_DESCRIPTOR);
  }
  
  public Object visit(ASTCompilationUnit cu, Object data)
  {
    if ((this.types.isEmpty()) && (getProperty(TYPES_DESCRIPTOR) != null)) {
      this.types.addAll(new ArrayList(Arrays.asList((Object[])getProperty(TYPES_DESCRIPTOR))));
    }
    if ((this.simpleTypes.isEmpty()) && (getProperty(TYPES_DESCRIPTOR) != null)) {
      for (String type : (String[])getProperty(TYPES_DESCRIPTOR)) {
        this.simpleTypes.add(RedUtil.toSimpleType(type));
      }
    }
    return super.visit(cu, data);
  }
  
  public void start(RuleContext ctx) {}
  
  public Object visit(ASTConstructorDeclaration node, Object data)
  {
    checkForBitmap(node, data);
    return data;
  }
  
  public Object visit(ASTMethodDeclaration node, Object data)
  {
    checkForBitmap(node, data);
    return data;
  }
  
  private void checkForBitmap(Node node, Object data)
  {
    List<ASTLocalVariableDeclaration> vars = node.findDescendantsOfType(ASTLocalVariableDeclaration.class);
    
    List<ASTVariableDeclaratorId> ids = new ArrayList();
    for (ASTLocalVariableDeclaration var : vars)
    {
      ASTClassOrInterfaceType classOrInterfaceType = (ASTClassOrInterfaceType)var.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
      if (classOrInterfaceType != null)
      {
        Class<?> type = classOrInterfaceType.getType();
        if (type != null)
        {
          String typeName = type.getName();
          if ((this.types.contains(typeName)) || (this.simpleTypes.contains(typeName)))
          {
            ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)var.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            
            ids.add(id);
          }
        }
        else
        {
          String typeName = classOrInterfaceType.getImage();
          if ((this.types.contains(typeName)) || (this.simpleTypes.contains(RedUtil.toSimpleType(typeName))))
          {
            ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)var.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            
            ids.add(id);
          }
        }
      }
    }
    for (ASTVariableDeclaratorId x : ids) {
      ensureRecycle(node, (ASTLocalVariableDeclaration)x.jjtGetParent().jjtGetParent(), x, data);
    }
  }
  
  private void ensureRecycle(Node node, ASTLocalVariableDeclaration var, ASTVariableDeclaratorId id, Object data)
  {
    String variableToClose = id.getImage();
    String closeXpath = ".//PrimaryExpression/PrimaryPrefix/Name[ends-with(@Image,'" + variableToClose + ".recycle')]";
    
    boolean isRecycle = false;
    if (!isRecycle)
    {
      List<ASTReturnStatement> returns = new ArrayList();
      node.findDescendantsOfType(ASTReturnStatement.class, returns, true);
      for (ASTReturnStatement returnStatement : returns)
      {
        ASTName name = (ASTName)returnStatement.getFirstDescendantOfType(ASTName.class);
        if ((name != null) && (name.getImage().equals(variableToClose)))
        {
          isRecycle = true;
          break;
        }
      }
    }
    if ((isRecycle) || (
    
      (!isRecycle) && (!node.hasDescendantMatchingXPath(closeXpath)))) {
      addViolation(data, id, id.getImage());
    }
  }
}
