package com.formautomation.data.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import com.formautomation.domain.model.FieldMapping
import com.formautomation.service.AccessibilityNodeData

data class FieldDetectionResult(
    val fieldId: String,
    val fieldName: String,
    val nodeInfo: AccessibilityNodeData?,
    val found: Boolean,
    val errorMessage: String? = null
)

class FieldDetector(
    private val accessibilityService: AccessibilityService
) {
    fun detectFieldOnScreen(mapping: FieldMapping): FieldDetectionResult {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow
                ?: return FieldDetectionResult(
                    fieldId = mapping.formFieldId,
                    fieldName = mapping.formFieldName,
                    nodeInfo = null,
                    found = false,
                    errorMessage = "Root node is null"
                )

            // Try to find by resource ID first
            val nodeByResource = findNodeByResourceId(rootNode, mapping.formFieldId)
            if (nodeByResource != null) {
                return FieldDetectionResult(
                    fieldId = mapping.formFieldId,
                    fieldName = mapping.formFieldName,
                    nodeInfo = convertNodeToData(nodeByResource),
                    found = true
                )
            }

            // Try to find by field name (as hint or label)
            val nodeByName = findNodeByText(rootNode, mapping.formFieldName)
            if (nodeByName != null && nodeByName.isEditable) {
                return FieldDetectionResult(
                    fieldId = mapping.formFieldId,
                    fieldName = mapping.formFieldName,
                    nodeInfo = convertNodeToData(nodeByName),
                    found = true
                )
            }

            // Try to find by content description
            val nodeByDesc = findNodeByContentDescription(rootNode, mapping.formFieldName)
            if (nodeByDesc != null && nodeByDesc.isEditable) {
                return FieldDetectionResult(
                    fieldId = mapping.formFieldId,
                    fieldName = mapping.formFieldName,
                    nodeInfo = convertNodeToData(nodeByDesc),
                    found = true
                )
            }

            FieldDetectionResult(
                fieldId = mapping.formFieldId,
                fieldName = mapping.formFieldName,
                nodeInfo = null,
                found = false,
                errorMessage = "Field '${mapping.formFieldName}' not found on screen"
            )
        } catch (e: Exception) {
            FieldDetectionResult(
                fieldId = mapping.formFieldId,
                fieldName = mapping.formFieldName,
                nodeInfo = null,
                found = false,
                errorMessage = e.message ?: "Unknown error during field detection"
            )
        }
    }

    fun detectAllFieldsOnScreen(mappings: List<FieldMapping>): List<FieldDetectionResult> {
        return mappings.map { mapping ->
            detectFieldOnScreen(mapping)
        }
    }

    fun detectSaveButton(buttonTexts: List<String> = listOf("Save", "Submit", "OK")): AccessibilityNodeData? {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow ?: return null

            for (buttonText in buttonTexts) {
                val button = findButtonByText(rootNode, buttonText)
                if (button != null) {
                    return convertNodeToData(button)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun detectNextButton(buttonTexts: List<String> = listOf("Next", "Continue", "Forward")): AccessibilityNodeData? {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow ?: return null

            for (buttonText in buttonTexts) {
                val button = findButtonByText(rootNode, buttonText)
                if (button != null) {
                    return convertNodeToData(button)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun detectFormSubmissionSuccess(successIndicators: List<String>): Boolean {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow ?: return false

            for (indicator in successIndicators) {
                if (nodeContainsText(rootNode, indicator)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun findNodeByResourceId(node: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (node.viewIdResourceName?.contains(resourceId) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByResourceId(child, resourceId)
            if (result != null) return result
        }

        return null
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.contains(text, ignoreCase = true) == true && node.isEditable) {
            return node
        }

        // Check hint text
        if (node.hintText?.contains(text, ignoreCase = true) == true && node.isEditable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByText(child, text)
            if (result != null) return result
        }

        return null
    }

    private fun findNodeByContentDescription(node: AccessibilityNodeInfo, description: String): AccessibilityNodeInfo? {
        if (node.contentDescription?.contains(description, ignoreCase = true) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByContentDescription(child, description)
            if (result != null) return result
        }

        return null
    }

    private fun findButtonByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.isClickable && node.text?.contains(text, ignoreCase = true) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findButtonByText(child, text)
            if (result != null) return result
        }

        return null
    }

    private fun nodeContainsText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (nodeContainsText(child, text)) {
                return true
            }
        }

        return false
    }

    private fun convertNodeToData(node: AccessibilityNodeInfo): AccessibilityNodeData {
        return AccessibilityNodeData(
            nodeId = node.viewIdResourceName ?: "",
            text = node.text?.toString() ?: "",
            contentDescription = node.contentDescription?.toString() ?: "",
            className = node.className?.toString() ?: "",
            resourceId = node.viewIdResourceName ?: "",
            isClickable = node.isClickable,
            isEditable = node.isEditable,
            isVisible = node.isVisibleToUser,
            bounds = getBoundsString(node)
        )
    }

    private fun getBoundsString(node: AccessibilityNodeInfo): String {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        return "${rect.left},${rect.top},${rect.width()},${rect.height()}"
    }
}
