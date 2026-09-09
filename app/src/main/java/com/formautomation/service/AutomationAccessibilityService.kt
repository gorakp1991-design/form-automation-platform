package com.formautomation.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AccessibilityNodeData(
    val nodeId: String,
    val text: String,
    val contentDescription: String,
    val className: String,
    val resourceId: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isVisible: Boolean,
    val bounds: String, // "x,y,width,height"
    val children: List<AccessibilityNodeData> = emptyList()
)

class AutomationAccessibilityService : AccessibilityService() {
    private val _currentWindowState = MutableStateFlow<AccessibilityNodeData?>(null)
    val currentWindowState: StateFlow<AccessibilityNodeData?> = _currentWindowState

    private val _focusedNode = MutableStateFlow<AccessibilityNodeData?>(null)
    val focusedNode: StateFlow<AccessibilityNodeData?> = _focusedNode

    private val _eventLog = MutableStateFlow<List<String>>(emptyList())
    val eventLog: StateFlow<List<String>> = _eventLog

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val logEntry = "[${System.currentTimeMillis()}] Event: ${event.eventType} - ${event.text}"
        val currentLog = _eventLog.value.toMutableList()
        currentLog.add(logEntry)
        if (currentLog.size > 100) {
            currentLog.removeAt(0) // Keep only last 100 events
        }
        _eventLog.value = currentLog

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                onWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                onViewFocused(event)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                onViewTextChanged(event)
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    private fun onWindowStateChanged(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val nodeData = convertNodeToData(rootNode)
            _currentWindowState.value = nodeData
        }
    }

    private fun onViewFocused(event: AccessibilityEvent) {
        val source = event.source
        if (source != null) {
            val nodeData = convertNodeToData(source)
            _focusedNode.value = nodeData
        }
    }

    private fun onViewTextChanged(event: AccessibilityEvent) {
        val source = event.source
        if (source != null) {
            val nodeData = convertNodeToData(source)
            _focusedNode.value = nodeData
        }
    }

    fun findNodeByResourceId(resourceId: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findNodeRecursive(rootNode, resourceId)
    }

    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findNodeByTextRecursive(rootNode, text)
    }

    fun focusNode(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
    }

    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun inputText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (!node.isEditable) return false

        // Clear existing text
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        node.performAction(AccessibilityNodeInfo.ACTION_SELECT_ALL)

        // Input new text
        val bundle = android.os.Bundle()
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
    }

    fun typeText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (!node.isEditable) return false

        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)

        // Simulate typing character by character
        for (char in text) {
            val keyEvent = android.view.KeyEvent(
                android.view.KeyEvent.ACTION_DOWN,
                getKeyCodeForChar(char)
            )
            // This would integrate with InputMethodManager in real implementation
        }
        return true
    }

    fun scrollNode(node: AccessibilityNodeInfo, direction: Int): Boolean {
        // Direction: AccessibilityNodeInfo.ACTION_SCROLL_FORWARD / ACTION_SCROLL_BACKWARD
        return node.performAction(direction)
    }

    fun getNodeBounds(node: AccessibilityNodeInfo): String {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        return "${rect.left},${rect.top},${rect.width()},${rect.height()}"
    }

    fun getAllEditableFields(): List<AccessibilityNodeData> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val fields = mutableListOf<AccessibilityNodeData>()
        findEditableFieldsRecursive(rootNode, fields)
        return fields
    }

    fun findButtonByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findButtonRecursive(rootNode, text)
    }

    private fun findNodeRecursive(node: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (node.viewIdResourceName?.contains(resourceId) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeRecursive(child, resourceId)
            if (result != null) return result
        }

        return null
    }

    private fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.contains(text) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByTextRecursive(child, text)
            if (result != null) return result
        }

        return null
    }

    private fun findButtonRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.isClickable && node.text?.contains(text) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findButtonRecursive(child, text)
            if (result != null) return result
        }

        return null
    }

    private fun findEditableFieldsRecursive(node: AccessibilityNodeInfo, fields: MutableList<AccessibilityNodeData>) {
        if (node.isEditable) {
            fields.add(convertNodeToData(node))
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findEditableFieldsRecursive(child, fields)
        }
    }

    private fun convertNodeToData(node: AccessibilityNodeInfo): AccessibilityNodeData {
        val children = mutableListOf<AccessibilityNodeData>()
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                children.add(convertNodeToData(child))
            }
        }

        return AccessibilityNodeData(
            nodeId = node.viewIdResourceName ?: "",
            text = node.text?.toString() ?: "",
            contentDescription = node.contentDescription?.toString() ?: "",
            className = node.className?.toString() ?: "",
            resourceId = node.viewIdResourceName ?: "",
            isClickable = node.isClickable,
            isEditable = node.isEditable,
            isVisible = node.isVisibleToUser,
            bounds = getNodeBounds(node),
            children = children
        )
    }

    private fun getKeyCodeForChar(char: Char): Int {
        return when (char) {
            '0' -> android.view.KeyEvent.KEYCODE_0
            '1' -> android.view.KeyEvent.KEYCODE_1
            '2' -> android.view.KeyEvent.KEYCODE_2
            '3' -> android.view.KeyEvent.KEYCODE_3
            '4' -> android.view.KeyEvent.KEYCODE_4
            '5' -> android.view.KeyEvent.KEYCODE_5
            '6' -> android.view.KeyEvent.KEYCODE_6
            '7' -> android.view.KeyEvent.KEYCODE_7
            '8' -> android.view.KeyEvent.KEYCODE_8
            '9' -> android.view.KeyEvent.KEYCODE_9
            else -> android.view.KeyEvent.KEYCODE_UNKNOWN
        }
    }
}
