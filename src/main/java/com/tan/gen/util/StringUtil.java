package com.tan.gen.util;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 字符串 工具类
 * @autor qwop
 * @date 2020-05-11
 */
public class StringUtil {
	private static final Pattern DigitPattern = Pattern.compile( "[0-9]+", Pattern.DOTALL);
	private static final Pattern UpperPattern = Pattern.compile( "[A-Z]+", Pattern.DOTALL);
	private static final Pattern LowerPattern = Pattern.compile( "[a-z]+", Pattern.DOTALL);
	/**
	 * 换行符
	 */
	public static final String LN = System.getProperty("line.separator");

	public static boolean needDecode( final String text ) {
//		return  text.matches( "^[\\w\\+=/]+$" );
		boolean result = false;
		boolean endsWithEqualsCharacter = false;
		// 0. when ends with the character =
		if ( text.endsWith( "==" )) {
			endsWithEqualsCharacter = true;
		}
		// 1. had the lower case character
		Matcher m = LowerPattern.matcher( text );
		result = m.find(); if ( endsWithEqualsCharacter && !result ) { return false; }
		// 2. had the upper case character
		m = UpperPattern.matcher( text );
		result = m.find(); if ( endsWithEqualsCharacter && !result ) { return false; }
		// 3. had the digit character.
		m = DigitPattern.matcher( text );
//		if ( endsWithEqualsCharacter && !result ) { return false; }
		if ( endsWithEqualsCharacter && m.find() ) { return true; }
		// 4. if had the character '/' and endsWith character '=' then must be right.
		if ( text.indexOf( "/" ) > 0 && endsWithEqualsCharacter ) {
			return true;
		}
		return result;
	}
	
	
	public static boolean isEmpty(String text) {
		return null == text || text.trim().length() == 0;
	}

	public static String getClipboardStringValue() {
		try {
			return String.valueOf( Toolkit.getDefaultToolkit().getSystemClipboard().getData( DataFlavor.stringFlavor ) );
		} catch (UnsupportedFlavorException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
