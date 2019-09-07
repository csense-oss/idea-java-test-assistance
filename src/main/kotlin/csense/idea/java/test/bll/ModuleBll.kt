package csense.idea.java.test.bll

import com.intellij.openapi.module.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.primitives.*


fun PsiDirectory.findTestFile(containingFile: PsiFile): PsiFile? {
    val fileName = containingFile.virtualFile.nameWithoutExtension
    val searchingForNames = listOf(fileName)
    return files.find {
        it.name.startsWithAny(searchingForNames)
    }
}

fun PsiDirectory.findTestFile(vararg fileNames: String): PsiFile? {
    val fileNameList = fileNames.toList()
    return files.find {
        it.name.startsWithAny(fileNameList)
    }
}


fun PsiFile.haveTestOfMethod(ourFunction: PsiMethod): Boolean {
    val functionNamesToFind = setOf(
            ourFunction.name ?: "",
            "test" + ourFunction.name.capitalize(),
            ourFunction.name + "test")
    return findDescendantOfType<PsiMethod> {
        it.name.startsWithAny(functionNamesToFind)
    }.isNotNull
}

fun Module.findPackageDir(containingFile: PsiFile): PsiDirectory? {
    val packageName = (containingFile as? PsiJavaFile)?.packageName ?: return null
    val sourceRoot = sourceRoots.find {
        it.name == "java"
    } ?: return null
    val psiDirectory = sourceRoot.toPsiDirectory(project) ?: return null
    return if (packageName == "") {
        psiDirectory
    } else {
        psiDirectory.findPackage(packageName)
    }
}

fun PsiElement.isInTestModule(): Boolean {
    val module = ModuleUtil.findModuleForPsiElement(this) ?: return false
    return module.isTestModule()
}

fun Module.isTestModule(): Boolean {
    return name.endsWith("_test") || name.endsWith(".test")
}

fun PsiElement.findTestModule(): Module? {
    val module = ModuleUtil.findModuleForPsiElement(this) ?: return null
    //step 2 is to find the test file in the test root
    if (module.isTestModule()) {
        return null
    }

    val searchingFor = module.name
            .replace("_main", "_test")
            .replace(".main", ".test")

    return ModuleManager.getInstance(project).modules.find { mod: Module ->
        searchingFor == mod.name
    }
}

fun PsiDirectory.findPackage(packageName: String): PsiDirectory? {
    if (packageName.isEmpty()) {
        return null
    }
    val folders = packageName.split(".")
    var resultingDirectory = this
    folders.forEach {
        resultingDirectory = resultingDirectory.findSubdirectory(it) ?: return null
    }
    return resultingDirectory
}