/*
 * Copyright 2009-2016 Weibo, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.huwhy.bees.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.huwhy.bees.common.BeesConstants;
import cn.huwhy.bees.exception.BeesServiceException;
import cn.huwhy.bees.util.MathUtil;

/**
 * 
 * Config tools
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-27
 */

public class ConfigUtil {

    /**
     * export fomart: protocol1:port1,protocol2:port2
     * 
     * @param export
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Integer> parseExport(String export) {
        if (StringUtils.isBlank(export)) {
            return Collections.emptyMap();
        }
        Map<String, Integer> pps = new HashMap<String, Integer>();
        String[] protocolAndPorts = BeesConstants.COMMA_SPLIT_PATTERN.split(export);
        for (String pp : protocolAndPorts) {
            if (StringUtils.isBlank(pp)) {
                continue;
            }
            String[] ppDetail = pp.split(":");
            if (ppDetail.length == 2) {
                pps.put(ppDetail[0], Integer.parseInt(ppDetail[1]));
            } else if (ppDetail.length == 1) {
                if (BeesConstants.PROTOCOL_INJVM.equals(ppDetail[0])) {
                    pps.put(ppDetail[0], BeesConstants.DEFAULT_INT_VALUE);
                } else {
                    int port = MathUtil.parseInt(ppDetail[0], 0);
                    if (port <= 0) {
                        throw new BeesServiceException("Export is malformed :" + export);
                    } else {
                        pps.put(BeesConstants.PROTOCOL_MOTAN, port);
                    }
                }

            } else {
                throw new BeesServiceException("Export is malformed :" + export);
            }
        }
        return pps;
    }

    public static String extractProtocols(String export) {
        Map<String, Integer> protocols = parseExport(export);
        StringBuilder sb = new StringBuilder(16);
        for (String p : protocols.keySet()) {
            sb.append(p).append(BeesConstants.COMMA_SEPARATOR);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();

    }
}
