package net.sourceforge.pmd.lang.java.rule.androidredline;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.util.redline.util.UIThreadCheckUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoNotDoTimeCostOpeInMainThreadRule
  extends AbstractJavaRule
{
  private Set<String> mTypes = new HashSet();
  private final String SHAREDPREFERENCES = "android.content.SharedPreferences";
  private final String SHAREDPREFERENCES_SHORT = "SharedPreferences";
  private final String EDIT = "android.content.SharedPreferences.Editor";
  private final String EDIT_SHORT = "SharedPreferences.Editor";
  private final String FILE_INPUT_STREAM = "java.io.FileInputStream";
  private final String FILE_OUTPUT_STREAM = "java.io.FileOutputStream";
  private final String BUFFERED_INPUT_STREAM = "java.io.BufferedInputStream";
  private final String BUFFERED_OUTPUT_STREAM = "java.io.BufferedOutputStream";
  private final String INPUT_STREAM_READER = "java.io.InputStreamReader";
  private final String OUTPUT_STREAM_WRITER = "java.io.OutputStreamWriter";
  private final String BUFFERED_READER = "java.io.BufferedReader";
  private final String BUFFERED_WRITER = "java.io.BufferedWriter";
  private final String SQLITEDATABASE = "SQLiteDatabase";
  private final String ZIP_OUTPUT_STREAM = "java.util.zip.ZipOutputStream";
  private final String ZIP_INPUT_STREAM = "java.util.zip.ZipInputStream";
  private final String CONTENT_PROVIDER = "android.content.ContentResolver";
  private final String CONTENT_PROVIDER_SHORT = "ContentResolver";
  private List<String> mUsefulFunSet = new ArrayList();
  
  private void appendSharedPreferenceFun(String name)
  {
    this.mUsefulFunSet.add(name + ".getString");
    this.mUsefulFunSet.add(name + ".contains");
    this.mUsefulFunSet.add(name + ".edit");
    this.mUsefulFunSet.add(name + ".getAll");
    this.mUsefulFunSet.add(name + ".getBoolean");
    this.mUsefulFunSet.add(name + ".getInt");
    this.mUsefulFunSet.add(name + ".getLong");
    this.mUsefulFunSet.add(name + ".getString");
    this.mUsefulFunSet.add(name + ".getFloat");
    this.mUsefulFunSet.add(name + ".registerOnSharedPreferenceChangeListener");
    this.mUsefulFunSet.add(name + ".unregisterOnSharedPreferenceChangeListener");
  }
  
  private void appendEditFun(String name)
  {
    this.mUsefulFunSet.add(name + ".apply");
    this.mUsefulFunSet.add(name + ".clear");
    this.mUsefulFunSet.add(name + ".commit");
    this.mUsefulFunSet.add(name + ".putBoolean");
    this.mUsefulFunSet.add(name + ".putStringSet");
    this.mUsefulFunSet.add(name + ".putInt");
    this.mUsefulFunSet.add(name + ".putLong");
    this.mUsefulFunSet.add(name + ".putString");
    this.mUsefulFunSet.add(name + ".putFloat");
    this.mUsefulFunSet.add(name + ".remove");
  }
  
  private void appendFileFun(String name)
  {
    this.mUsefulFunSet.add(name + ".read");
    this.mUsefulFunSet.add(name + ".readLine");
    this.mUsefulFunSet.add(name + ".write");
    this.mUsefulFunSet.add(name + ".newLine");
    this.mUsefulFunSet.add(name + ".close");
  }
  
  private void appendSQLiteDatabaseFun(String name)
  {
    this.mUsefulFunSet.add(name + ".openOrCreateDatabase");
    this.mUsefulFunSet.add(name + ".beginTransaction");
    this.mUsefulFunSet.add(name + ".close");
    this.mUsefulFunSet.add(name + ".create");
    this.mUsefulFunSet.add(name + ".delete");
    this.mUsefulFunSet.add(name + ".execSQL");
    this.mUsefulFunSet.add(name + ".update");
    this.mUsefulFunSet.add(name + ".rawQuery");
    this.mUsefulFunSet.add(name + ".query");
    this.mUsefulFunSet.add(name + ".openDatabase");
    this.mUsefulFunSet.add(name + ".endTransaction");
    this.mUsefulFunSet.add(name + ".insert");
    this.mUsefulFunSet.add(name + ".insertOrThrow");
    this.mUsefulFunSet.add(name + ".endTransaction");
    this.mUsefulFunSet.add(name + ".findEditTable");
    this.mUsefulFunSet.add(name + ".getAttachedDbs");
    this.mUsefulFunSet.add(name + ".getMaximumSize");
    this.mUsefulFunSet.add(name + ".getPath");
    this.mUsefulFunSet.add(name + ".getPageSize");
    this.mUsefulFunSet.add(name + ".inTransaction");
    this.mUsefulFunSet.add(name + ".replace");
    this.mUsefulFunSet.add(name + ".replaceOrThrow");
    this.mUsefulFunSet.add(name + ".yieldIfContended");
    this.mUsefulFunSet.add(name + ".setLocale");
    this.mUsefulFunSet.add(name + ".setMaxSqlCacheSize");
    this.mUsefulFunSet.add(name + ".setVersion");
    this.mUsefulFunSet.add(name + ".yieldIfContendedSafely");
  }
  
  private void appendZipFun(String name)
  {
    this.mUsefulFunSet.add(name + ".available");
    this.mUsefulFunSet.add(name + ".close");
    this.mUsefulFunSet.add(name + ".closeEntry");
    this.mUsefulFunSet.add(name + ".getNextEntry");
    this.mUsefulFunSet.add(name + ".read");
    this.mUsefulFunSet.add(name + ".finish");
    this.mUsefulFunSet.add(name + ".putNextEntry");
    this.mUsefulFunSet.add(name + ".setComment");
    this.mUsefulFunSet.add(name + ".setLevel");
    this.mUsefulFunSet.add(name + ".setMethod");
    this.mUsefulFunSet.add(name + ".write");
  }
  
  private void appendProviderFun(String name)
  {
    this.mUsefulFunSet.add(name + ".query");
    this.mUsefulFunSet.add(name + ".insert");
    this.mUsefulFunSet.add(name + ".update");
    this.mUsefulFunSet.add(name + ".delete");
  }
  
  public DoNotDoTimeCostOpeInMainThreadRule()
  {
    this.mTypes.add("android.content.SharedPreferences");
    this.mTypes.add("SharedPreferences");
    
    this.mTypes.add("android.content.SharedPreferences.Editor");
    this.mTypes.add("SharedPreferences.Editor");
    
    this.mTypes.add("java.io.BufferedInputStream");
    this.mTypes.add("java.io.BufferedOutputStream");
    this.mTypes.add("java.io.InputStreamReader");
    this.mTypes.add("java.io.OutputStreamWriter");
    this.mTypes.add("java.io.FileInputStream");
    this.mTypes.add("java.io.FileOutputStream");
    this.mTypes.add("java.io.BufferedReader");
    this.mTypes.add("java.io.BufferedWriter");
    this.mTypes.add("SQLiteDatabase");
    this.mTypes.add("java.util.zip.ZipInputStream");
    this.mTypes.add("java.util.zip.ZipOutputStream");
    
    this.mTypes.add("android.content.ContentResolver");
    this.mTypes.add("ContentResolver");
  }
  
  public Object visit(ASTBlockStatement node, Object data)
  {
    if (UIThreadCheckUtils.isOnUiThread(node)) {
      ensureTimeCostInMainThread(node, data);
    }
    return super.visit(node, data);
  }
  
  public Object visit(ASTPrimarySuffix node, Object data)
  {
    String image = node.getImage();
    ASTPrimaryExpression ex;
    if ((image != null) && ("getContentResolver".equals(image)))
    {
      ex = (ASTPrimaryExpression)node.getNthParent(1);
      if (ex != null)
      {
        List<ASTPrimarySuffix> suffixs = ex.findChildrenOfType(ASTPrimarySuffix.class);
        for (ASTPrimarySuffix suffix : suffixs)
        {
          String s = suffix.getImage();
          if ((s != null) && (isProviderMethod(s)))
          {
            addViolation(data, ex);
            break;
          }
        }
      }
    }
    return super.visit(node, data);
  }
  
  public Object visit(ASTClassOrInterfaceDeclaration node, Object data)
  {
    List<ASTFieldDeclaration> fields = node.findDescendantsOfType(ASTFieldDeclaration.class);
    List<ASTLocalVariableDeclaration> vars = node.findDescendantsOfType(ASTLocalVariableDeclaration.class);
    for (ASTFieldDeclaration field : fields) {
      findVar(field);
    }
    for (ASTLocalVariableDeclaration var : vars) {
      findVar(var);
    }
    return super.visit(node, data);
  }
  
  private void findVar(AbstractJavaAccessNode node)
  {
    ASTClassOrInterfaceType type = (ASTClassOrInterfaceType)((ASTType)node.getFirstChildOfType(ASTType.class)).getFirstDescendantOfType(ASTClassOrInterfaceType.class);
    String typeName = null;
    if (type == null) {
      return;
    }
    typeName = type.getImage();
    if (typeName == null) {
      return;
    }
    if (this.mTypes.contains(typeName))
    {
      ASTVariableDeclaratorId id = (ASTVariableDeclaratorId)node.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
      String imageName = id.getImage();
      
      utlity(typeName, imageName);
    }
  }
  
  private void utlity(String typeName, String imageName)
  {
    if ((typeName.equals("android.content.SharedPreferences")) || (typeName.equals("SharedPreferences"))) {
      appendSharedPreferenceFun(imageName);
    } else if ((typeName.equals("android.content.SharedPreferences.Editor")) || (typeName.equals("SharedPreferences.Editor"))) {
      appendEditFun(imageName);
    } else if ((typeName.equals("java.io.BufferedInputStream")) || (typeName.equals("java.io.BufferedOutputStream")) || (typeName.equals("java.io.BufferedReader")) || (typeName.equals("java.io.BufferedWriter")) || (typeName.equals("java.io.FileInputStream")) || (typeName.equals("java.io.FileOutputStream")) || (typeName.equals("java.io.InputStreamReader")) || (typeName.equals("java.io.OutputStreamWriter"))) {
      appendFileFun(imageName);
    } else if (typeName.equals("SQLiteDatabase")) {
      appendSQLiteDatabaseFun(imageName);
    } else if ((typeName.equals("java.util.zip.ZipInputStream")) || (typeName.equals("java.util.zip.ZipOutputStream"))) {
      appendZipFun(imageName);
    } else if ((typeName.equals("android.content.ContentResolver")) || (typeName.equals("ContentResolver"))) {
      appendProviderFun(imageName);
    }
  }
  
  private void ensureTimeCostInMainThread(Node node, Object data)
  {
    List<ASTPrimaryPrefix> vars = node.findDescendantsOfType(ASTPrimaryPrefix.class);
    
    List<ASTBlockStatement> statements = node.findDescendantsOfType(ASTBlockStatement.class);
    if (statements.size() > 0) {
      return;
    }
    if (vars == null) {
      return;
    }
    boolean mark = false;
    String variableToClose = null;
    for (int i = 0; i < vars.size(); i++)
    {
      ASTName name = (ASTName)((ASTPrimaryPrefix)vars.get(i)).getFirstDescendantOfType(ASTName.class);
      if (name != null)
      {
        variableToClose = name.getImage();
        if (this.mUsefulFunSet.contains(variableToClose)) {
          mark = true;
        }
      }
    }
    if (mark) {
      addViolation(data, node);
    }
    otherCheck(node, data);
  }
  
  private void otherCheck(Node node, Object data)
  {
    ASTPrimaryExpression pe;
    List<ASTName> names = node.findDescendantsOfType(ASTName.class);
    for (ASTName name : names)
    {
      String image = name.getImage();
      if ((image != null) && (image.equals("getContentResolver")))
      {
        pe = null;
        Node ex = name.getNthParent(2);
        if ((ex instanceof ASTPrimaryExpression)) {
          pe = (ASTPrimaryExpression)ex;
        }
        if (pe != null)
        {
          List<ASTPrimarySuffix> suffixs = pe.findChildrenOfType(ASTPrimarySuffix.class);
          for (ASTPrimarySuffix suffix : suffixs)
          {
            String s = suffix.getImage();
            if ((s != null) && (isProviderMethod(s)))
            {
              addViolation(data, pe);
              break;
            }
          }
        }
      }
    }
  }
  
  private boolean isProviderMethod(String s)
  {
    return ("insert".equals(s)) || ("update".equals(s)) || ("delete".equals(s)) || ("query".equals(s));
  }
}
