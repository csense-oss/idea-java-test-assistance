package csense.idea.java.test.visitors

import csense.kotlin.Function0
import com.intellij.psi.*

class PsiMethodVisitor(
        val block: Function0<PsiMethod>
) : PsiElementVisitor() {
    override fun visitElement(element: PsiElement?) {
        if (element is PsiMethod) {
            block(element)
        }
    }
}