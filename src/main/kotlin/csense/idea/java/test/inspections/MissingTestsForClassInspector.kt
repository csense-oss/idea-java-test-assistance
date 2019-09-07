package csense.idea.java.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.lang.jvm.*
import com.intellij.psi.*
import csense.idea.java.test.bll.*
import csense.idea.java.test.visitors.*
import org.jetbrains.kotlin.asJava.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.lexer.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

class MissingTestsForClassInspector : LocalInspectionTool(), CustomSuppressableInspectionTool {


    override fun getDisplayName(): String {
        return "Missing tests for class"
    }

    override fun getStaticDescription(): String? {
        return "Highlights classes that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String? {
        return "Highlights for classes that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestClass"
    }

    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): PsiElementVisitor {
        return PsiClassVisitor { ourClass ->
            if (ourClass.isInTestModule() || ourClass.isAbstract()) {
                return@PsiClassVisitor
            }
            //skip classes /things with no functions
            val functions = ourClass.allMethods
            if (functions.isEmpty()) {
                //if no functions are there, we have "nothing" to test. bail.
                return@PsiClassVisitor
            }
            //if we are an interface, and we have default impl's, then we should inspect the interface
            // otherwise NO
            if (ourClass.isInterfaceClassAndNoDefaultImpl()) {
                return@PsiClassVisitor
            }
            //step 2 is to find the test file in the test root
            val testModule = ourClass.findTestModule() ?: return@PsiClassVisitor
            val resultingDirectory = testModule.findPackageDir(ourClass.containingFile)
            val testFile = resultingDirectory?.findTestFile(ourClass.containingFile)

            //TODO search for class refs in testfile ??? hmm especially since we can have multiple classes in each file.
            if (testFile != null) {
                return@PsiClassVisitor //there are tests for this class so just skip this.
            }
            holder.registerProblem(ourClass.nameIdentifier ?: ourClass,
                    "You have properly not tested this class")
        }
    }

    override fun getSuppressActions(element: PsiElement?): Array<SuppressIntentionAction>? {
        return arrayOf()
    }
}

fun PsiClass.isAbstract(): Boolean {
    return hasModifier(JvmModifier.ABSTRACT)
}

fun PsiNamedElement.isInterfaceClass(): Boolean = when (this) {
    is PsiClass -> isInterface
    else -> false
}

fun PsiNamedElement.isInterfaceClassAndNoDefaultImpl(): Boolean {
    val klass = this as? PsiClass ?: return false
    if (!klass.isInterface) {
        return false
    }
    return klass.allMethods.any {
        it.body != null
    }
}