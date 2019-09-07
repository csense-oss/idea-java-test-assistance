package csense.idea.java.test.inspections

import com.intellij.codeInspection.*

class InspectionsProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<*>> {
        return arrayOf(
                MissingTestsForFunctionInspector::class.java,
                MissingTestsForClassInspector::class.java
        )
    }
}