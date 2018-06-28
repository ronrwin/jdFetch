package com.example.jddata.util;

import android.accessibilityservice.AccessibilityService;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityUtils {

    /** 处理点击事件 */
    public static boolean performClickByText(AccessibilityService service, String className, String text, boolean isLongClick) {
        List<AccessibilityNodeInfo> nodes = findAccessibilityNodeInfosByText(service, text);
        if (nodes == null) return false;

        for (AccessibilityNodeInfo node : nodes) {
            if (className.equals(node.getClassName())) {
                if (node.isEnabled() && node.isClickable()) {
                    return node.performAction(isLongClick ? AccessibilityNodeInfo.ACTION_LONG_CLICK : AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }

        return false;
    }

    public static boolean performClick(AccessibilityService service, String viewId, boolean isLongClick) {
        List<AccessibilityNodeInfo> nodes = findAccessibilityNodeInfosByViewId(service, viewId);
        if (nodes == null) return false;

        for (AccessibilityNodeInfo node : nodes) {
            return node.performAction(isLongClick ? AccessibilityNodeInfo.ACTION_LONG_CLICK : AccessibilityNodeInfo.ACTION_CLICK);
        }

        return false;
    }

    public static boolean performChildClick(AccessibilityService service, String className, String childClassName, String viewId, boolean isLongClick) {
        List<AccessibilityNodeInfo> nodes = findAccessibilityNodeInfosByViewId(service, viewId);
        if (nodes == null) return false;

        for (AccessibilityNodeInfo node : nodes) {
            if (className.equals(node.getClassName())) {
                for (int i = 0; i < node.getChildCount(); i++) {
                    AccessibilityNodeInfo child = node.getChild(i);
                    if (childClassName.equals(child.getClassName())) {
                        if (child.isEnabled() && child.isClickable()) {
                            child.performAction(isLongClick ? AccessibilityNodeInfo.ACTION_LONG_CLICK : AccessibilityNodeInfo.ACTION_CLICK);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(AccessibilityService service, String viewId) {
        if (TextUtils.isEmpty(viewId)) return null;
        AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
        if (nodeInfo == null) return null;
        try {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
            if (isNodesAvalibale(list)) {
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(AccessibilityService service, String text) {
        if (TextUtils.isEmpty(text)) return null;
        AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
        if (nodeInfo == null) return null;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (isNodesAvalibale(list)) {
            return list;
        }
        return null;
    }

    public static boolean isNodesAvalibale(List<AccessibilityNodeInfo> nodes) {
        return nodes != null && nodes.size() > 0;
    }

    public static boolean performGlobalActionBack(AccessibilityService service) {
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public static boolean performGlobalActionHome(AccessibilityService service) {
        return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    private static AccessibilityNodeInfo getParent(AccessibilityNodeInfo node) {
        if (node != null && node.getParent() != null) {
            return node.getParent();
        }
        return null;
    }

    public static AccessibilityNodeInfo findParentClickable(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo currentNode = node;
        AccessibilityNodeInfo parentClickable = null;
        do {
            parentClickable = getParent(currentNode);
            currentNode = parentClickable;
        } while (parentClickable != null && !parentClickable.isClickable());
        return parentClickable;
    }

    public static AccessibilityNodeInfo findParentByClassname(AccessibilityNodeInfo node, String classname) {
        AccessibilityNodeInfo currentNode = node;
        AccessibilityNodeInfo parent = null;
        do {
            parent = getParent(currentNode);
            currentNode = parent;
        } while (parent != null && !parent.getClassName().equals(classname));
        return parent;
    }

    public static List<AccessibilityNodeInfo> findChildByClassname(AccessibilityNodeInfo node, String classname) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo index = node.getChild(i);
            if (index != null) {
                if (index.getClassName().equals(classname)) {
                    result.add(index);
                } else {
                    if (index.getChildCount() > 0) {
                        result.addAll(findChildByClassname(index, classname));
                    }
                }
            }
        }

        return result;
    }

    public static boolean performParentClickableByText(AccessibilityService service, String text) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(service, text);
        if (nodes == null) return false;
        for (AccessibilityNodeInfo item : nodes) {
            if (text.equals(item.getContentDescription()) || text.equals(item.getText())) {
                AccessibilityNodeInfo parent = AccessibilityUtils.findParentClickable(item);
                if (parent != null) {
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        return false;
    }

    public static boolean performParentClickableByViewId(AccessibilityService service, String viewId) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(service, viewId);
        if (nodes == null) return false;
        for (AccessibilityNodeInfo item : nodes) {
            AccessibilityNodeInfo parent = AccessibilityUtils.findParentClickable(item);
            if (parent != null) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
        return false;
    }
}
