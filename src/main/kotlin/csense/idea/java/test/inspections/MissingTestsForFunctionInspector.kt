package csense.idea.java.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.lang.jvm.*
import com.intellij.psi.*
import csense.idea.java.test.bll.*
import csense.idea.java.test.quickfixes.*
import csense.idea.java.test.visitors.*
import kotlin.system.*

class MissingTestsForFunctionInspector : LocalInspectionTool(), CustomSuppressableInspectionTool {


    override fun getDisplayName(): String {
        return "Missing test for function"
    }

    override fun getStaticDescription(): String? {
        return "Highlights functions that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String? {
        return "Highlights functions that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestFunction"
    }

    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }


    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): PsiElementVisitor {
        return PsiMethodVisitor { ourFunction: PsiMethod ->
            if (ourFunction.isPrivate() ||
                    ourFunction.isProtected() ||
                    ourFunction.isInTestModule()) {
                return@PsiMethodVisitor//ignore private & protected methods or test methods
            }
            if (ourFunction.containingClass?.isAbstract() == true) {
                return@PsiMethodVisitor //do not look in abstract classes.
            }
            val timeInMs = measureTimeMillis {
                val testModule = ourFunction.findTestModule() ?: return@PsiMethodVisitor
                val resultingDirectory = testModule.findPackageDir(ourFunction.containingFile)

                val testFile = resultingDirectory
                        ?.findTestFile(ourFunction.containingFile)
                        ?: return@PsiMethodVisitor //skip class / obj functions if no test file is found

                val haveTestFunction = testFile.haveTestOfMethod(ourFunction)
                if (!haveTestFunction) {
                    val fixes = createQuickFixesForFunction(
                            testFile,
                            ourFunction)
                    holder.registerProblem(
                            ourFunction.nameIdentifier ?: ourFunction,
                            "You have properly not tested this method",
                            *fixes)
                }

            }
            if (timeInMs > 10) {
                println("Took $timeInMs ms")
            }
        }
    }

    override fun getSuppressActions(element: PsiElement?): Array<SuppressIntentionAction>? {
        return arrayOf()
    }

    fun createQuickFixesForFunction(file: PsiFile?, ourFunction: PsiMethod): Array<LocalQuickFix> {
        val javaFile = file as? PsiJavaFile ?: return arrayOf()
        val firstClass = javaFile.classes.firstOrNull() ?: return arrayOf()

        return arrayOf(AddTestMethodQuickFix(
                ourFunction,
                "test" + ourFunction.name.capitalize(),
                firstClass
        ))
    }
}


fun PsiMethod.isPrivate(): Boolean {
    return hasModifier(JvmModifier.PRIVATE)
}

fun PsiMethod.isProtected(): Boolean {
    return hasModifier(JvmModifier.PROTECTED)
}
