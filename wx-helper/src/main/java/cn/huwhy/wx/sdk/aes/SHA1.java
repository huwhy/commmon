/**
 * 对公众平台发送给公众账号的消息加解密示例代码.
 * 
 * @copyright Copyright (c) 1998-2014 Tencent Inc.
 */

// ------------------------------------------------------------------------

package cn.huwhy.wx.sdk.aes;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * SHA1 class
 *
 * 计算公众平台的消息签名接口.
 */
public class SHA1 {

	/**
	 * 串接arr参数，生成sha1 digest
	 *
	 * @param arr
	 * @return
	 */
	public static String gen(String... arr) throws NoSuchAlgorithmException {
		Arrays.sort(arr);
		StringBuilder sb = new StringBuilder();
		for (String a : arr) {
			sb.append(a);
		}
		return DigestUtils.sha1Hex(sb.toString());
	}

	/**
	 * 用&串接arr参数，生成sha1 digest
	 *
	 * @param arr
	 * @return
	 */
	public static String genWithAmple(String... arr) throws NoSuchAlgorithmException {
		Arrays.sort(arr);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			String a = arr[i];
			sb.append(a);
			if (i != arr.length - 1) {
				sb.append('&');
			}
		}
		return DigestUtils.sha1Hex(sb.toString());
	}


}
