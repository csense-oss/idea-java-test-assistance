package csense.idea.java.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import csense.idea.java.test.bll.*

class AddTestMethodQuickFix(
        element: PsiElement,
        val testName: String,
        val whereToWrite: PsiClass
) : LocalQuickFixOnPsiElement(element) {

    override fun getText(): String {
        return "Add test for this method"
    }

    override fun getFamilyName(): String {
        return "Csense - kotlin - test"
    }


    override fun invoke(
            project: Project,
            file: PsiFile,
            startElement: PsiElement,
            endElement: PsiElement
    ) {
        val factory = JavaPsiFacade.getInstance(project).elementFactory
        val testMethod = factory.createMethod(
                testName, PsiType.VOID)
        val comment = factory.createCommentFromText("//TODO create test", null)
        testMethod.body?.add(comment)
        project.executeWriteCommand("update test class") {
            try {
                val body = whereToWrite
                val annotation = factory.createAnnotationFromText(
                        "@Test",
                        null)
                body.addBefore(annotation, body.lastChild)
                body.addBefore(testMethod, body.lastChild)
            } catch (e: Throwable) {
                TODO("Add error handling here")
            }
        }
    }
}
