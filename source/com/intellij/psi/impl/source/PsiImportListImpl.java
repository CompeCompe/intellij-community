package com.intellij.psi.impl.source;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.java.stubs.PsiImportListStub;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PsiImportListImpl extends JavaStubPsiElement<PsiImportListStub> implements PsiImportList {
  private volatile Map<String,PsiImportStatement> myClassNameToImportMap = null;
  private volatile Map<String,PsiImportStatement> myPackageNameToImportMap = null;
  private volatile Map<String,PsiImportStatementBase> myNameToSingleImportMap = null;
  private static final PsiImportStatementBase[] EMPTY_IMPORTS = new PsiImportStatementBase[0];

  public PsiImportListImpl(final PsiImportListStub stub) {
    super(stub, JavaStubElementTypes.IMPORT_LIST);
  }

  public PsiImportListImpl(final ASTNode node) {
    super(node);
  }

  protected Object clone() {
    PsiImportListImpl clone = (PsiImportListImpl)super.clone();
    clone.myClassNameToImportMap = null;
    clone.myPackageNameToImportMap = null;
    clone.myNameToSingleImportMap = null;
    return clone;
  }

  public void subtreeChanged() {
    myClassNameToImportMap = null;
    myPackageNameToImportMap = null;
    myNameToSingleImportMap = null;
    super.subtreeChanged();
  }

  @NotNull
  public PsiImportStatement[] getImportStatements() {
    return getStubOrPsiChildren(Constants.IMPORT_STATEMENT_BIT_SET, PsiImportStatementImpl.EMPTY_ARRAY);
  }

  @NotNull
  public PsiImportStaticStatement[] getImportStaticStatements() {
    return getStubOrPsiChildren(Constants.IMPORT_STATIC_STATEMENT_BIT_SET, PsiImportStaticStatementImpl.EMPTY_ARRAY);

  }

  @NotNull
  public PsiImportStatementBase[] getAllImportStatements() {
    return getStubOrPsiChildren(Constants.IMPORT_STATEMENT_BASE_BIT_SET, EMPTY_IMPORTS);
  }

  public PsiImportStatement findSingleClassImportStatement(String name) {
    while (true) {
      Map<String, PsiImportStatement> map = myClassNameToImportMap;
      if (map == null) {
        initializeMaps();
      }
      else {
        return map.get(name);
      }
    }
  }

  public PsiImportStatement findOnDemandImportStatement(String name) {
    while (true) {
      Map<String, PsiImportStatement> map = myPackageNameToImportMap;
      if (map == null) {
        initializeMaps();
      }
      else {
        return map.get(name);
      }
    }
  }

  public PsiImportStatementBase findSingleImportStatement(String name) {
    while (true) {
      Map<String, PsiImportStatementBase> map = myNameToSingleImportMap;
      if (map == null) {
        initializeMaps();
      }
      else {
        return map.get(name);
      }
    }
  }

  public boolean isReplaceEquivalent(PsiImportList otherList) {
    return getText().equals(otherList.getText());
  }

  private void initializeMaps() {
    Map<String, PsiImportStatement> classNameToImportMap = new HashMap<String, PsiImportStatement>();
    Map<String, PsiImportStatement> packageNameToImportMap = new HashMap<String, PsiImportStatement>();
    Map<String, PsiImportStatementBase> nameToSingleImportMap = new HashMap<String, PsiImportStatementBase>();
    PsiImportStatement[] imports = getImportStatements();
    for (PsiImportStatement anImport : imports) {
      String qName = anImport.getQualifiedName();
      if (qName == null) continue;
      if (anImport.isOnDemand()) {
        packageNameToImportMap.put(qName, anImport);
      }
      else {
        classNameToImportMap.put(qName, anImport);
        PsiJavaCodeReferenceElement importReference = anImport.getImportReference();
        if (importReference == null) continue;
        nameToSingleImportMap.put(importReference.getReferenceName(), anImport);
      }
    }

    PsiImportStaticStatement[] importStatics = getImportStaticStatements();
    for (PsiImportStaticStatement importStatic : importStatics) {
      if (!importStatic.isOnDemand()) {
        String referenceName = importStatic.getReferenceName();
        if (referenceName != null) {
          nameToSingleImportMap.put(referenceName, importStatic);
        }
      }
    }

    myClassNameToImportMap = classNameToImportMap;
    myPackageNameToImportMap = packageNameToImportMap;
    myNameToSingleImportMap = nameToSingleImportMap;
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor)visitor).visitImportList(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  public String toString() {
    return "PsiImportList";
  }
}
