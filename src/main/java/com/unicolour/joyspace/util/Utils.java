package com.unicolour.joyspace.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class Utils {
	public static String calcMD5Hash(String str) {
		try {
			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(str.getBytes());
			return toHexString(messagedigest.digest());
		} catch (NoSuchAlgorithmException nsaex) {
			nsaex.printStackTrace();
			return null;
		}
	}

	private static final char[] hexCode = "0123456789abcdef".toCharArray();

	public static String toHexString(byte[] data) {
		StringBuilder r = new StringBuilder(data.length * 2);
		for (byte b : data) {
			r.append(hexCode[(b >> 4) & 0xF]);
			r.append(hexCode[(b & 0xF)]);
		}
		return r.toString();
	}
	
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.equals("");
	}

	public static <T> List<T> toList(Iterator<T> iterator) {
		ArrayList<T> list = new ArrayList<>();
		if (iterator != null) {
			while (iterator.hasNext()) {
				list.add(iterator.next());
			}
		}
		return list;
	}

	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 格式化日期对象 (yyyy-MM-dd HH:mm:ss)
	 * @param time
	 * @return
	 */
	public synchronized static String formatTime(Calendar time) {
		return time == null ? null : String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", time);
	}
	
	/**
	 * 解析日期字符串 (yyyy-MM-dd HH:mm:ss),  返回的日期对象的时区是系统的本地时区
	 * @param timeStr
	 * @return
	 */
	public synchronized static Calendar parseTime(String timeStr) {
		if (isNullOrEmpty(timeStr)) {
			return null;
		}
		else {
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(DATE_TIME_FORMAT.parse(timeStr));
				return cal;
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static Calendar checkDate(Calendar date) {
		if (date == null ||
			date.get(Calendar.YEAR) == 1970 && date.get(Calendar.MONTH) == Calendar.JANUARY && date.get(Calendar.DAY_OF_MONTH) == 1) {
			return null;
		}
		else {
			return date;
		}
	}

	public static String encodePassword(String password, String timeStr)
	{
	    return calcMD5Hash(calcMD5Hash(password) + timeStr);
	}

	public static String nonNullString(String str) {
		return str == null ? "" : str;
	}
}