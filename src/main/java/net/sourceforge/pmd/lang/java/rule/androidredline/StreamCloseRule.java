package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.BooleanProperty;
import net.sourceforge.pmd.lang.rule.properties.StringMultiProperty;
import org.jaxen.JaxenException;

import java.util.*;

public class StreamCloseRule
  extends AbstractJavaRule
{
  private Set<String> types = new HashSet();
  private Set<String> simpleTypes = new HashSet();
  private Set<String> closeTargets = new HashSet();
  private static final StringMultiProperty CLOSE_TARGETS_DESCRIPTOR = new StringMultiProperty("closeTargets", "Methods which may close this resource", new String[0], 1.0F, ',');
  private static final StringMultiProperty TYPES_DESCRIPTOR = new StringMultiProperty("types", "Affected types", new String[] { "java.io.FileReader", "java.io.FileWriter", "java.io.BufferedReader", "java.io.BufferedWriter", "java.io.InputStream", "java.io.OutputStream", "java.io.Reader", "java.io.Writer", "java.io.Closeable" }, 2.0F, ',');
  private static final BooleanProperty USE_CLOSE_AS_DEFAULT_TARGET = new BooleanProperty("closeAsDefaultTarget", "Consider 'close' as a target by default", Boolean.valueOf(true), 3.0F);
  
  public StreamCloseRule()
  {
    definePropertyDescriptor(CLOSE_TARGETS_DESCRIPTOR);
    definePropertyDescriptor(TYPES_DESCRIPTOR);
    definePropertyDescriptor(USE_CLOSE_AS_DEFAULT_TARGET);
  }
  
  public Object visit(ASTCompilationUnit node, Object data)
  {
    if ((this.closeTargets.isEmpty()) && (getProperty(CLOSE_TARGETS_DESCRIPTOR) != null)) {
      this.closeTargets.addAll(new ArrayList(Arrays.asList((Object[])getProperty(CLOSE_TARGETS_DESCRIPTOR))));
    }
    if ((((Boolean)getProperty(USE_CLOSE_AS_DEFAULT_TARGET)).booleanValue()) && (!this.closeTargets.contains("close"))) {
      this.closeTargets.add("close");
    }
    if ((this.types.isEmpty()) && (getProperty(TYPES_DESCRIPTOR) != null)) {
      this.types.addAll(new ArrayList(Arrays.asList((Object[])getProperty(TYPES_DESCRIPTOR))));
    }
    if ((this.simpleTypes.isEmpty()) && (getProperty(TYPES_DESCRIPTOR) != null)) {
      for (String type : (String[])getProperty(TYPES_DESCRIPTOR)) {
        this.simpleTypes.add(toSimpleType(type));
      }
    }
    return super.visit(node, data);
  }
  
  private static String toSimpleType(String fullyQualifiedClassName)
  {
    int lastIndexOf = fullyQualifiedClassName.lastIndexOf('.');
    if (lastIndexOf > -1) {
      return fullyQualifiedClassName.substring(lastIndexOf + 1);
    }
    return fullyQualifiedClassName;
  }
  
  public Object visit(ASTConstructorDeclaration node, Object data)
  {
    checkForResources(node, data);
    return data;
  }
  
  public Object visit(ASTMethodDeclaration node, Object data)
  {
    checkForResources(node, data);
    return data;
  }
  
  private void checkForResources(Node node, Object data)
  {
    List<ASTLocalVariableDeclaration> vars = node.findDescendantsOfType(ASTLocalVariableDeclaration.class);
    List<ASTVariableDeclaratorId> ids = new ArrayList();
    for (ASTLocalVariableDeclaration var : vars)
    {
      ASTType type = var.getTypeNode();
      if ((type.jjtGetChild(0) instanceof ASTReferenceType))
      {
        ASTReferenceType ref = (ASTReferenceType)type.jjtGetChild(0);
        if ((ref.jjtGetChild(0) instanceof ASTClassOrInterfaceType))
        {
          ASTClassOrInterfaceType clazz = (ASTClassOrInterfaceType)ref.jjtGetChild(0);
          if (((clazz.getType() != null) && (this.types.contains(clazz.getType().getName()))) || ((clazz.getType() == null) && (this.simpleTypes.contains(toSimpleType(clazz.getImage())))) || (this.types.contains(clazz.getImage())))
          {
            ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)var.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            ids.add(id);
          }
        }
      }
    }
    for (ASTVariableDeclaratorId x : ids) {
      ensureClosed((ASTLocalVariableDeclaration)x.jjtGetParent().jjtGetParent(), x, data);
    }
  }
  
  private boolean hasNullInitializer(ASTLocalVariableDeclaration var)
  {
    ASTVariableInitializer init = (ASTVariableInitializer)var.getFirstDescendantOfType(ASTVariableInitializer.class);
    if (init != null) {
      try
      {
        List<?> nulls = init.findChildNodesWithXPath("Expression/PrimaryExpression/PrimaryPrefix/Literal/NullLiteral");
        
        return !nulls.isEmpty();
      }
      catch (JaxenException e)
      {
        return false;
      }
    }
    return false;
  }
  
  private void ensureClosed(ASTLocalVariableDeclaration var, ASTVariableDeclaratorId id, Object data)
  {
    String variableToClose = id.getImage();
    ASTPrimaryExpression expr;
    Node n = var;
    while ((!(n instanceof ASTBlock)) && (!(n instanceof ASTConstructorDeclaration))) {
      n = n.jjtGetParent();
    }
    Node top = n;
    
    List<ASTTryStatement> tryblocks = top.findDescendantsOfType(ASTTryStatement.class);
    
    boolean closed = false;
    
    ASTBlockStatement parentBlock = (ASTBlockStatement)id.getFirstParentOfType(ASTBlockStatement.class);
    for (ASTTryStatement t : tryblocks) {
      if ((t.getBeginLine() > id.getBeginLine()) && (t.hasFinally()))
      {
        ASTBlock f = (ASTBlock)t.getFinally().jjtGetChild(0);
        List<ASTName> names = f.findDescendantsOfType(ASTName.class);
        for (ASTName oName : names)
        {
          String name = oName.getImage();
          if ((name != null) && (name.contains(".")))
          {
            String[] parts = name.split("\\.");
            if (parts.length >= 2)
            {
              String methodName = parts[(parts.length - 1)];
              
              String varName = parts[0];
              if ((varName.equals(variableToClose)) && (nullCheckIfCondition(f, oName, varName)))
              {
                closed = true;
                break;
              }
              if (this.closeTargets.contains(methodName))
              {
                ASTName vName = (ASTName)((ASTPrimarySuffix)((ASTPrimaryExpression)oName.getFirstParentOfType(ASTPrimaryExpression.class)).getFirstDescendantOfType(ASTPrimarySuffix.class)).getFirstDescendantOfType(ASTName.class);
                if ((vName != null) && (vName.getImage().equals(variableToClose)))
                {
                  closed = true;
                  break;
                }
              }
            }
          }
          else if (this.closeTargets.contains(name))
          {
            ASTName vName = (ASTName)((ASTPrimarySuffix)((ASTPrimaryExpression)oName.getFirstParentOfType(ASTPrimaryExpression.class)).getFirstDescendantOfType(ASTPrimarySuffix.class)).getFirstDescendantOfType(ASTName.class);
            if ((vName != null) && (vName.getImage().equals(variableToClose)))
            {
              closed = true;
              break;
            }
          }
        }
        if (closed) {
          break;
        }
        List<ASTStatementExpression> exprs = new ArrayList();
        f.findDescendantsOfType(ASTStatementExpression.class, exprs, true);
        for (ASTStatementExpression stmt : exprs)
        {
          expr = (ASTPrimaryExpression)stmt.getFirstChildOfType(ASTPrimaryExpression.class);
          if (expr != null)
          {
            ASTPrimaryPrefix prefix = (ASTPrimaryPrefix)expr.getFirstChildOfType(ASTPrimaryPrefix.class);
            ASTPrimarySuffix suffix = (ASTPrimarySuffix)expr.getFirstChildOfType(ASTPrimarySuffix.class);
            if ((prefix != null) && (suffix != null))
            {
              if (prefix.getImage() == null)
              {
                ASTName prefixName = (ASTName)prefix.getFirstChildOfType(ASTName.class);
                if ((prefixName != null) && (this.closeTargets.contains(prefixName.getImage())))
                {
                  closed = variableIsPassedToMethod(expr, variableToClose);
                  if (closed) {
                    break;
                  }
                }
              }
              else if (suffix.getImage() != null)
              {
                String prefixPlusSuffix = prefix.getImage() + "." + suffix.getImage();
                if (this.closeTargets.contains(prefixPlusSuffix))
                {
                  closed = variableIsPassedToMethod(expr, variableToClose);
                  if (closed) {
                    break;
                  }
                }
              }
              if (!closed)
              {
                List<ASTPrimarySuffix> suffixes = new ArrayList();
                expr.findDescendantsOfType(ASTPrimarySuffix.class, suffixes, true);
                for (ASTPrimarySuffix oSuffix : suffixes)
                {
                  String suff = oSuffix.getImage();
                  if (this.closeTargets.contains(suff))
                  {
                    closed = variableIsPassedToMethod(expr, variableToClose);
                    if (closed) {
                      break;
                    }
                  }
                }
              }
            }
          }
        }

        if (closed) {
          break;
        }
      }
    }
    if (!closed)
    {
      List<ASTReturnStatement> returns = new ArrayList();
      top.findDescendantsOfType(ASTReturnStatement.class, returns, true);
      for (ASTReturnStatement returnStatement : returns)
      {
        List<ASTName> names = returnStatement.findDescendantsOfType(ASTName.class);
        if (names.size() > 0) {
          for (ASTName name : names) {
            if ((name != null) && (name.getImage().equals(variableToClose)))
            {
              closed = true;
              break;
            }
          }
        }
      }
    }
    if (!closed)
    {
      ASTType type = (ASTType)var.getFirstChildOfType(ASTType.class);
      ASTReferenceType ref = (ASTReferenceType)type.jjtGetChild(0);
      ASTClassOrInterfaceType clazz = (ASTClassOrInterfaceType)ref.jjtGetChild(0);
      addViolation(data, (Node)id.getFirstParentOfType(ASTBlock.class), clazz.getImage());
    }
  }
  
  private boolean variableIsPassedToMethod(ASTPrimaryExpression expr, String variable)
  {
    List<ASTName> methodParams = new ArrayList();
    expr.findDescendantsOfType(ASTName.class, methodParams, true);
    for (ASTName pName : methodParams)
    {
      String paramName = pName.getImage();
      
      ASTArgumentList parentParam = (ASTArgumentList)pName.getFirstParentOfType(ASTArgumentList.class);
      if ((paramName.equals(variable)) && (parentParam != null)) {
        return true;
      }
    }
    return false;
  }
  
  private ASTIfStatement findIfStatement(ASTBlock enclosingBlock, Node node)
  {
    ASTIfStatement ifStatement = (ASTIfStatement)node.getFirstParentOfType(ASTIfStatement.class);
    List<ASTIfStatement> allIfStatements = enclosingBlock.findDescendantsOfType(ASTIfStatement.class);
    if ((ifStatement != null) && (allIfStatements.contains(ifStatement))) {
      return ifStatement;
    }
    return null;
  }
  
  private boolean nullCheckIfCondition(ASTBlock enclosingBlock, Node node, String varName)
  {
    ASTIfStatement ifStatement = findIfStatement(enclosingBlock, node);
    if (ifStatement != null) {
      try
      {
        List<?> nodes = ifStatement.findChildNodesWithXPath("Expression/EqualityExpression[@Image='!=']  [PrimaryExpression/PrimaryPrefix/Name[@Image='" + varName + "']]" + "  [PrimaryExpression/PrimaryPrefix/Literal/NullLiteral]");
        
        return !nodes.isEmpty();
      }
      catch (JaxenException e) {}
    }
    return true;
  }
}
