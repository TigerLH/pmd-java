package net.sourceforge.pmd.util.redline.util;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.symboltable.ClassScope;

import java.util.ArrayList;
import java.util.List;

public class UIThreadCheckUtils
{
  private static final int METHOD_TYPE_NOTUI = 1;
  private static final int METHOD_TYPE_MAYUI = 2;
  private static final int METHOD_TYPE_UI = 3;
  private static final int METHOD_TYPE_NOTCARE = 4;
  private static List<ClassInfo> methodsOnUI = new ArrayList();
  private static List<ClassInfo> actionOnUI = new ArrayList();
  private static List<String> actionNeedAgain = new ArrayList();
  private static List<String> methodsNotOnUI = new ArrayList();
  
  static
  {
    methodsOnUI.add(new ClassInfo("Activity", "onCreate|onStart|onRestart|onResume|onPause|onStop|onDestroy"));
    
    methodsOnUI.add(new ClassInfo("Service", "onCreate|onStart|onStartCommand|onBind|onRebind|onUnbind|onDestroy"));
    
    methodsOnUI.add(new ClassInfo("BroadcastReceiver", "onReceive"));
    methodsOnUI.add(new ClassInfo("Provider", "onCreate|query|getType|insert|delete|update"));
    
    methodsOnUI.add(new ClassInfo("Handler", "handleMessage|dispatchMessage"));
    
    methodsOnUI.add(new ClassInfo("AsyncTask", "onPreExecute|onProgressUpdate|onPostExecute"));
    
    actionOnUI.add(new ClassInfo("Activity", "runOnUiThread"));
    actionOnUI.add(new ClassInfo("View", "post|postDelayed|postOnAnimation|postOnAnimationDelayed"));
    
    actionNeedAgain.add("run");
    
    methodsNotOnUI.add("call");
    methodsNotOnUI.add("onHandleIntent");
    methodsNotOnUI.add("doInBackground");
  }
  
  public static boolean isOnUiThread(ASTBlockStatement blockStatement)
  {
    if (blockStatement == null) {
      return false;
    }
    ASTMethodDeclaration methodDeclaration = (ASTMethodDeclaration)blockStatement.getFirstParentOfType(ASTMethodDeclaration.class);
    if (methodDeclaration == null) {
      return false;
    }
    String[] minfo = getMethodInfo(methodDeclaration);
    boolean result = false;
    switch (checkAndSelect(minfo))
    {
    case 2: 
      ASTClassOrInterfaceBodyDeclaration clsOrInterBodyDeclar = (ASTClassOrInterfaceBodyDeclaration)methodDeclaration.getFirstParentOfType(ASTClassOrInterfaceBodyDeclaration.class);
      
      ASTArguments arguments = (ASTArguments)clsOrInterBodyDeclar.getFirstParentOfType(ASTArguments.class);
      if ((result = isNotNull(arguments)))
      {
        ASTPrimaryExpression express = (ASTPrimaryExpression)arguments.getFirstParentOfType(ASTPrimaryExpression.class);
        if ((result = isNotNull(express)))
        {
          ASTName name = (ASTName)((ASTPrimaryPrefix)express.getFirstChildOfType(ASTPrimaryPrefix.class)).getFirstChildOfType(ASTName.class);
          if ((result = isNotNull(name)))
          {
            String image = name.getImage();
            ClassScope clzScope = (ClassScope)name.getScope().getEnclosingScope(ClassScope.class);
            if (("runOnUiThread".equals(image)) && (clzScope.getClassName().endsWith("Activity")))
            {
              result = true;
              break;
            }
            if ((result = isNotNull(image)))
            {
              String[] md = image.split("\\.");
              if ((md == null) || (md.length < 2))
              {
                result = false;
                break;
              }
              String varType = getVarNode(clsOrInterBodyDeclar, md[0]);
              if (checkMd(new String[] { varType, md[1] })) {
                result = true;
              } else {
                result = false;
              }
            }
          }
          ASTPrimaryPrefix prefix = (ASTPrimaryPrefix)express.getFirstChildOfType(ASTPrimaryPrefix.class);
          if ((prefix != null) && ((prefix.usesThisModifier()) || (prefix.usesSuperModifier())))
          {
            ASTPrimarySuffix suffix = (ASTPrimarySuffix)express.getFirstChildOfType(ASTPrimarySuffix.class);
            if ((suffix != null) && ("runOnUiThread".equals(suffix.getImage())))
            {
              result = true;
              break;
            }
          }
        }
      }
      break;
    case 1: 
      result = false;
      break;
    case 3: 
      result = true;
    }
    return result;
  }
  
  private static String getVarNode(ASTClassOrInterfaceBodyDeclaration clsOrInterBodyDeclar, String declaratorId)
  {
    ASTClassOrInterfaceBody cls = (ASTClassOrInterfaceBody)((ASTClassOrInterfaceBody)clsOrInterBodyDeclar.getFirstParentOfType(ASTClassOrInterfaceBody.class)).getFirstParentOfType(ASTClassOrInterfaceBody.class);
    if ((cls == null) || (declaratorId == null)) {
      return "";
    }
    List<ASTFieldDeclaration> fields = cls.findDescendantsOfType(ASTFieldDeclaration.class);
    List<ASTLocalVariableDeclaration> vars = cls.findDescendantsOfType(ASTLocalVariableDeclaration.class);
    for (ASTFieldDeclaration field : fields)
    {
      ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)field.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
      if (declaratorId.equals(id.getImage()))
      {
        ASTType type = (ASTType)field.getFirstChildOfType(ASTType.class);
        return type.getTypeImage();
      }
    }
    for (ASTLocalVariableDeclaration var : vars)
    {
      ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)var.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
      if (declaratorId.equals(id.getImage()))
      {
        ASTType type = (ASTType)var.getFirstChildOfType(ASTType.class);
        return type.getTypeImage();
      }
    }
    return null;
  }
  
  private static boolean checkMd(String... md)
  {
    String cls = md[0];
    String m = md[1];
    for (ClassInfo ci : actionOnUI)
    {
      String methods = ci.getMethod();
      if ((cls != null) && (methods != null) && (methods.contains(m)) && (cls.endsWith(ci.getCls()))) {
        return true;
      }
    }
    return false;
  }
  
  private static int checkAndSelect(String[] minfo)
  {
    String cls = minfo[0];
    String method = minfo[1];
    if (methodsNotOnUI.contains(method)) {
      return 1;
    }
    for (ClassInfo ci : methodsOnUI) {
      if ((cls != null) && (cls.endsWith(ci.getCls())) && (ci.getMethod().contains(method))) {
        return 3;
      }
    }
    if (actionNeedAgain.contains(method)) {
      return 2;
    }
    return 4;
  }
  
  private static String[] getMethodInfo(ASTMethodDeclaration method)
  {
    String[] r = new String[2];
    ASTClassOrInterfaceBodyDeclaration body = (ASTClassOrInterfaceBodyDeclaration)method.getFirstParentOfType(ASTClassOrInterfaceBodyDeclaration.class);
    if (body.isAnonymousInnerClass())
    {
      ASTClassOrInterfaceType type = (ASTClassOrInterfaceType)((ASTAllocationExpression)body.getFirstParentOfType(ASTAllocationExpression.class)).getFirstChildOfType(ASTClassOrInterfaceType.class);
      r[0] = type.getImage();
    }
    else if (body.isEnumChild())
    {
      ASTEnumDeclaration type = (ASTEnumDeclaration)body.getFirstParentOfType(ASTEnumDeclaration.class);
      r[0] = type.getImage();
    }
    else
    {
      ClassScope methScope = (ClassScope)method.getScope().getEnclosingScope(ClassScope.class);
      r[0] = methScope.getClassName();
    }
    r[1] = method.getMethodName();
    
    return r;
  }
  
  private static boolean isNotNull(Object o)
  {
    return o != null;
  }
  
  static class ClassInfo
  {
    String cls;
    String method;
    
    public ClassInfo(String cls, String method)
    {
      this.cls = cls;
      this.method = method;
    }
    
    public String getCls()
    {
      return this.cls;
    }
    
    public void setCls(String cls)
    {
      this.cls = cls;
    }
    
    public String getMethod()
    {
      return this.method;
    }
    
    public void setMethod(String method)
    {
      this.method = method;
    }
  }
}
