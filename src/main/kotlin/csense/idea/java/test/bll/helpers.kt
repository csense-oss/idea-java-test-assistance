package csense.idea.java.test.bll

import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import java.io.*


fun Project.executeWriteCommand(name: String, command: () -> Unit) {
    CommandProcessor.getInstance().executeCommand(this, {
        ApplicationManager.getApplication().runWriteAction(command)
    }, name, null)
}

inline fun <reified T> PsiElement.findDescendantOfType(noinline predicate: (T) -> Boolean = { true }): T? {
    return findDescendantOfType({ true }, predicate)
}

inline fun <reified T> PsiElement.findDescendantOfType(crossinline canGoInside: (PsiElement) -> Boolean, noinline predicate: (T) -> Boolean = { true }): T? {
    var result: T? = null
    this.accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element is T && predicate(element)) {
                result = element
                stopWalking()
                return
            }

            if (canGoInside(element)) {
                super.visitElement(element)
            }
        }
    })
    return result
}

val Module.sourceRoots: Array<VirtualFile>
    get() = rootManager.sourceRoots


fun File.toPsiDirectory(project: Project): PsiDirectory? {
    return toVirtualFile()?.let { vfile -> PsiManager.getInstance(project).findDirectory(vfile) }
}

fun VirtualFile.toPsiFile(project: Project): PsiFile? = PsiManager.getInstance(project).findFile(this)

fun VirtualFile.toPsiDirectory(project: Project): PsiDirectory? = PsiManager.getInstance(project).findDirectory(this)

fun File.toPsiFile(project: Project): PsiFile? = toVirtualFile()?.toPsiFile(project)

fun File.toVirtualFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByIoFile(this)