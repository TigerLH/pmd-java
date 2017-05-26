package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckonCreateTryCatchRule
  extends AbstractJavaRule
{
  long start = System.currentTimeMillis();
  private static Set<String> OnCreareStrings = new HashSet();
  
  static
  {
    OnCreareStrings.add("getBooleanArrayExtra");
    OnCreareStrings.add("getBooleanExtra");
    OnCreareStrings.add("getBundleExtra");
    OnCreareStrings.add("getBundleExtra");
    OnCreareStrings.add("getByteArrayExtra");
    OnCreareStrings.add("getByteExtra");
    OnCreareStrings.add("getCharArrayExtra");
    OnCreareStrings.add("getCharExtra");
    OnCreareStrings.add("getCharSequenceArrayExtra");
    OnCreareStrings.add("getCharSequenceArrayListExtra");
    OnCreareStrings.add("getCharSequenceExtra");
    OnCreareStrings.add("getClipData");
    OnCreareStrings.add("getData");
    OnCreareStrings.add("getDataString");
    OnCreareStrings.add("getDoubleArrayExtra");
    OnCreareStrings.add("getDoubleExtra");
    OnCreareStrings.add("getExtras");
    OnCreareStrings.add("getFloatArrayExtra\t");
    OnCreareStrings.add("getFloatExtra");
    OnCreareStrings.add("getIntArrayExtra");
    OnCreareStrings.add("getIntegerArrayListExtra");
    OnCreareStrings.add("getIntExtra");
    OnCreareStrings.add("getLongArrayExtra");
    OnCreareStrings.add("getLongExtra");
    OnCreareStrings.add("getPackage");
    OnCreareStrings.add("getParcelableArrayExtra");
    OnCreareStrings.add("getParcelableArrayListExtra");
    OnCreareStrings.add("getParcelableExtra");
    OnCreareStrings.add("getSerializableExtra");
    OnCreareStrings.add("getShortArrayExtra");
    OnCreareStrings.add("getShortExtra");
    OnCreareStrings.add("getStringArrayExtra");
    OnCreareStrings.add("getStringArrayListExtra");
    OnCreareStrings.add("getStringExtra");
  }
  
  public Object visit(ASTMethodDeclarator node, Object data)
  {
    if ((node != null) && (node.getImage().equalsIgnoreCase("onCreate")))
    {
      checkTryCatchDeclaration(node, data);
      return super.visit(node, data);
    }
    return super.visit(node, data);
  }
  
  private void checkTryCatchDeclaration(Node node, Object data)
  {
    ASTMethodDeclaration methodDeclaration;
    String varString;
    try
    {
      methodDeclaration = (ASTMethodDeclaration)node.getFirstParentOfType(ASTMethodDeclaration.class);
      List<ASTClassOrInterfaceType> classOrInterfaceTypes = methodDeclaration.findDescendantsOfType(ASTClassOrInterfaceType.class);
      if (classOrInterfaceTypes.size() > 0) {
        for (ASTClassOrInterfaceType classOrInterfaceType : classOrInterfaceTypes) {
          if ((classOrInterfaceType != null) && (classOrInterfaceType.getImage().equalsIgnoreCase("Intent")))
          {
            ASTVariableDeclaratorId variableDeclaratorId = (ASTVariableDeclaratorId)((ASTLocalVariableDeclaration)classOrInterfaceType.getFirstParentOfType(ASTLocalVariableDeclaration.class)).getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            if (variableDeclaratorId != null)
            {
              varString = variableDeclaratorId.getImage();
              
              List<ASTName> astNames = methodDeclaration.findDescendantsOfType(ASTName.class);
              if (astNames.size() > 0) {
                for (ASTName astName : astNames)
                {
                  String astNameString = astName.getImage();
                  if (astNameString.contains(varString + "."))
                  {
                    astNameString = astNameString.replace(varString + ".", "");
                    if (OnCreareStrings.contains(astNameString))
                    {
                      ASTTryStatement tryStatement = (ASTTryStatement)astName.getFirstParentOfType(ASTTryStatement.class);
                      if (tryStatement == null) {
                        addViolation(data, astName);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    catch (Exception e) {}
  }
}
