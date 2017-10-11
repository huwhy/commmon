package cn.huwhy.bees.registry.zookeeper;

import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.exception.BeesFrameworkException;
import cn.huwhy.bees.rpc.URL;

public class ZkUtils {

    public static String toGroupPath(URL url) {
        return BeesConstants.ZOOKEEPER_REGISTRY_NAMESPACE + BeesConstants.PATH_SEPARATOR + url.getGroup();
    }

    public static String toServicePath(URL url) {
        return toGroupPath(url) + BeesConstants.PATH_SEPARATOR + url.getPath();
    }

    public static String toCommandPath(URL url) {
        return toGroupPath(url) + BeesConstants.ZOOKEEPER_REGISTRY_COMMAND;
    }

    public static String toNodeTypePath(URL url, ZkNodeType nodeType) {
        String type;
        if (nodeType == ZkNodeType.AVAILABLE_SERVER) {
            type = "server";
        } else if (nodeType == ZkNodeType.UNAVAILABLE_SERVER) {
            type = "unavailableServer";
        } else if (nodeType == ZkNodeType.CLIENT) {
            type = "client";
        } else {
            throw new BeesFrameworkException(String.format("Failed to get nodeTypePath, url: %s type: %s", url, nodeType.toString()));
        }
        return toServicePath(url) + BeesConstants.PATH_SEPARATOR + type;
    }

    public static String toNodePath(URL url, ZkNodeType nodeType) {
        return toNodeTypePath(url, nodeType) + BeesConstants.PATH_SEPARATOR + url.getServerPortStr();
    }
}
