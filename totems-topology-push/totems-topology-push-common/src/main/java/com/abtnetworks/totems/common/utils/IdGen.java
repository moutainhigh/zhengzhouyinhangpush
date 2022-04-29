/**
 * Copyright hw
 */
package com.abtnetworks.totems.common.utils;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 封装各种生成唯一性ID算法的工具类.
 * @author hw
 * @version
 */
@Service("pushIdGen")
@Lazy(false)
public class IdGen {

	private static SecureRandom random = new SecureRandom();

	private static final int DEFAULT_NUMBER_LENGTH = 4;
	
	/**
	 * 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割.
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	/**
	 * 使用SecureRandom随机生成Long. 
	 */
	public static long randomLong() {
		return Math.abs(random.nextLong());
	}

	/**
	 * 基于Base62编码的SecureRandom随机生成bytes.
	 */
	public static String randomBase62(int length) {
		byte[] randomBytes = new byte[length];
		random.nextBytes(randomBytes);
		return Encodes.encodeBase62(randomBytes);
	}
	
	public static void main(String[] args) {
		System.out.println(IdGen.uuid());
		System.out.println(IdGen.uuid().length());
		for (int i=0; i<1000; i++){
			System.out.println(IdGen.randomLong() + "  " + IdGen.randomBase62(5));
		}
	}

	public static String getRandomNumberString() {
		return getRandomNumberString(DEFAULT_NUMBER_LENGTH);
	}


	public static String getRandomNumberString(int length) {
		Set<Integer> used = new HashSet<>();
		if (length <= 0 || length >= 10) {
			return "length out of range";
		}

		int digit = 1;
		while(length > 0 ) {
			digit = digit * 10;
			length --;
		}

		long randomLong = IdGen.randomLong();
		int id = (int)(randomLong % digit);
		int maxTryTimes = 3;
		//试3次
		while(used.contains(id) && maxTryTimes > 0) {
			randomLong = IdGen.randomLong();
			id = (int)(randomLong % 100);
			maxTryTimes --;
		}
		return String.valueOf(id);
	}

	/**
	 * 生成随机负数
	 * @return
	 */
	public static int randomNegativeInt() {
		int i = random.nextInt();
		if (i < 0) {
			return i;
		} else {
			return i*-1;
		}
	}
}
