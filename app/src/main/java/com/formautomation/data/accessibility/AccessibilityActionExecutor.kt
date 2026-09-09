package com.formautomation.data.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import com.formautomation.service.AccessibilityNodeData
import kotlinx.coroutines.delay

data class ClickAction(
    val nodeId: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class AccessibilityActionExecutor(
    private val accessibilityService: AccessibilityService
) {
    suspend fun clickButton(nodeData: AccessibilityNodeData): Result<ClickAction> {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow
                ?: return Result.failure(Exception("Root node is null"))

            val targetNode = findNodeByResourceId(rootNode, nodeData.resourceId)
                ?: return Result.failure(Exception("Target node not found"))

            if (!targetNode.isClickable) {
                return Result.failure(Exception("Node is not clickable"))
            }

            delay(300) // Simulate click timing
            val success = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            if (success) {
                delay(500) // Wait for action to complete
                Result.success(
                    ClickAction(
                        nodeId = nodeData.nodeId,
                        success = true
                    )
                )
            } else {
                Result.failure(Exception("Click action failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun focusAndTypeIntoField(
        nodeData: AccessibilityNodeData,
        text: String
    ): Result<ClickAction> {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow
                ?: return Result.failure(Exception("Root node is null"))

            val targetNode = findNodeByResourceId(rootNode, nodeData.resourceId)
                ?: return Result.failure(Exception("Target node not found"))

            if (!targetNode.isEditable) {
                return Result.failure(Exception("Node is not editable"))
            }

            // Focus the field
            delay(200)
            targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            delay(300)

            // Clear existing text
            targetNode.performAction(AccessibilityNodeInfo.ACTION_SELECT_ALL)
            delay(100)

            // Input new text
            val bundle = android.os.Bundle()
            bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            val success = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)

            delay(300)

            if (success) {
                Result.success(
                    ClickAction(
                        nodeId = nodeData.nodeId,
                        success = true
                    )
                )
            } else {
                Result.failure(Exception("Text input action failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scrollToView(
        nodeData: AccessibilityNodeData,
        direction: Int = AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
    ): Result<ClickAction> {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow
                ?: return Result.failure(Exception("Root node is null"))

            val targetNode = findNodeByResourceId(rootNode, nodeData.resourceId)
                ?: return Result.failure(Exception("Target node not found"))

            delay(200)
            val success = targetNode.performAction(direction)
            delay(500)

            if (success) {
                Result.success(
                    ClickAction(
                        nodeId = nodeData.nodeId,
                        success = true
                    )
                )
            } else {
                Result.failure(Exception("Scroll action failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun performCustomAction(
        nodeData: AccessibilityNodeData,
        actionId: Int
    ): Result<ClickAction> {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow
                ?: return Result.failure(Exception("Root node is null"))

            val targetNode = findNodeByResourceId(rootNode, nodeData.resourceId)
                ?: return Result.failure(Exception("Target node not found"))

            delay(200)
            val success = targetNode.performAction(actionId)
            delay(300)

            if (success) {
                Result.success(
                    ClickAction(
                        nodeId = nodeData.nodeId,
                        success = true
                    )
                )
            } else {
                Result.failure(Exception("Custom action failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
}
