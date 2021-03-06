package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Beans Utilities.
 *
 */
public class BeansUtils {

	private final static Pattern patternForCommonNameParsing = Pattern.compile("(([\\w]*. )*)([\\p{L}-']+) ([\\p{L}-']+)[, ]*(.*)");
	public static final char LIST_DELIMITER = ',';
	public static final char KEY_VALUE_DELIMITER = ':';

	/**
	 * Method create formatter with default settings for perun timestamps and set lenient on false
	 * Timestamp format:  "yyyy-MM-dd HH:mm:ss.S" - "ex. 2014-01-01 10:10:10.0"
	 *
	 * Lenient on false means that formatter will be more strict to creating timestamp from string
	 *
	 * IMPORTANT: SimpleDateFormat is not thread safe !!!
	 *
	 * @return date formatter
	 */
	public static DateFormat getDateFormatter() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		df.setLenient(false);
		return df;
	}

	/**
	 * This method take text and for every chars in "<>\" erase escaping
	 * Escaping char is \.
	 * Expecting: Before using this method, text must be escaped by using method createEscaping.
	 *            So in text will never be string like "\\>", "\" or "\\\".
	 *
	 * For every \ in text it put \\ and for every < it put \< and for every > it put \>
	 *
	 * @param text text from which will be erase escaping
	 * @return nonescaped text
	 */
	public static String eraseEscaping(String text) {
		if(text == null || text.equals("\\0")) return null;
		text = text.replace("\\>", ">");
		text = text.replace("\\<", "<");
		text = text.replace("\\\\", "\\");
		return text;
	}
	/**
	 * This method take text and for every chars in "<>\" create escaping
	 * Escaping char is \.
	 * For every \\ in text it put \ and for every \< it put < and for every \> it put >
	 *
	 * @param text text from which will be erase escaping
	 * @return escaped text
	 */
	public static String createEscaping(String text) {
		if(text == null) return "\\0";
		text = text.replace("\\", "\\\\");
		text = text.replace(">", "\\>");
		text = text.replace("<", "\\<");
		return text;
	}

	/**
	 * This method get text and all nonescaped characters < and > replace by apostrophe
	 *
	 * @param text
	 * @return text where nonescaped characters < and  > will be reaplace by apostrophe '
	 */
	public static String replacePointyBracketsByApostrophe(String text) {
		StringBuilder stringBuilder = new StringBuilder(text);
		for(int i=0; i<text.length(); i++) {
			if(text.charAt(i)=='<' || text.charAt(i)=='>') {
				if(!isEscaped(text, i-1)) {
					stringBuilder.setCharAt(i, '\'');
				}
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * This method get text and all escaped \0 replace for text null
	 *
	 * @param text
	 * @return text where \0 is replaced for null
	 */
	public static String replaceEscapedNullByStringNull(String text) {
		StringBuilder stringBuilder = new StringBuilder(text);
		for(int i=0; i<stringBuilder.length(); i++) {
			if(stringBuilder.charAt(i)=='0') {
				if(isEscaped(stringBuilder.toString(), i-1)) {
					stringBuilder.replace(i-1, i+1, "null");
					i=i+2;
				}
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Return true, if char on position in text is escaped by '\' Return false,
	 * if not.
	 *
	 * @param text text in which will be searching
	 * @param position position in text <0-text.length>
	 * @return true if char is escaped, false if not
	 */
	public static boolean isEscaped(String text, int position) {
		boolean escaped = false;
		while (text.charAt(position) == '\\') {
			escaped = !escaped;
			position--;
			if (position < 0) {
				return escaped;
			}
		}
		return escaped;
	}

	/**
	 * Serialize map to string
	 *
	 * @param map
	 * @return string of escaped map
	 */
	public static String serializeMapToString(Map<String, String> map) {
		if(map == null) return "\\0";
		Map<String, String> attrNew = new HashMap<String, String>(map);
		Set<String> keys = new HashSet<String>(attrNew.keySet());
		for(String s: keys) {
			attrNew.put("<" + BeansUtils.createEscaping(s) + ">", "<" + BeansUtils.createEscaping(attrNew.get(s)) + ">");
			attrNew.remove(s);
		}
		return attrNew.toString();
	}


	/**
	 * Converts attribute value to string (for storing into DB)
	 *
	 * @param attribute value of the attribute
	 * @return string representation of the value
	 *
	 * @throws InternalErrorException
	 */
	public static String attributeValueToString(Attribute attribute) throws InternalErrorException {
		if(attribute == null) throw new InternalErrorException(new NullPointerException("attribute is null"));
		if(attribute.getValue() == null) return null;

		if(!attribute.getType().equals(attribute.getValue().getClass().getName())) {
			throw new InternalErrorException("Attribute's type mismatch " + attribute + ". The type of attribute's value (" + attribute.getValue().getClass().getName() + ") doesn't match the type of attribute (" + attribute.getType() + ").");
		}

		if(attribute.getType().equals(String.class.getName())) {
			return (String) attribute.getValue();
		} else if(attribute.getType().equals(Integer.class.getName())) {
			return Integer.toString((Integer) attribute.getValue());
		} else if(attribute.getType().equals(ArrayList.class.getName())) {
			StringBuilder sb = new StringBuilder();
			for(String item : (List<String>) attribute.getValue()) {
				if(item == null) {
					item = "\\0";
				} else {
					item = item.replace("\\", "\\\\");   //escape char '\'
					item = item.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
				}
				sb.append(item);
				sb.append(LIST_DELIMITER);
			}
			return sb.toString();
		} else if(attribute.getType().equals(LinkedHashMap.class.getName())) {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, String> entry : ((Map<String, String>) attribute.getValue()).entrySet()) {
				String key = entry.getKey();
				if(key == null) {
					key = "\\0";
				} else {
					key = key.replace("\\", "\\\\");   //escape char '\'
					key = key.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
					key = key.replace(Character.toString(KEY_VALUE_DELIMITER), "\\" + KEY_VALUE_DELIMITER); //escape KEY_VALUE_DELIMITER
				}

				String value = entry.getValue();
				if(value == null) {
					value = "\\0";
				} else {
					value = value.replace("\\", "\\\\");   //escape char '\'
					value = value.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
					value = value.replace(Character.toString(KEY_VALUE_DELIMITER), "\\" + KEY_VALUE_DELIMITER); //escape KEY_VALUE_DELIMITER
				}

				sb.append(key);
				sb.append(KEY_VALUE_DELIMITER);
				sb.append(value);
				sb.append(LIST_DELIMITER);
			}
			return sb.toString();
		} else throw new InternalErrorException("Unknown java type of attribute's value.");
	}

	/**
	 * This method get map created by example : {<key1>=<value1>, <key2>=<value2>}
	 * Keys and values are escaped for "\", "<" and ">"
	 * Example of escaping key="key\\s\>" is "key\s>"
	 * Return Map<String, String> attribute to value.
	 *
	 * @param text text from which will be parsed map
	 * @return map<string, string> attributes
	 */
	public static Map<String, String> deserializeStringToMap(String text) {
		if(text.equals("\\0")) return null;
		Map<String, String> map = new HashMap<String, String>();
		int startName = -1;
		int endName = -1;
		int startValue = -1;
		int endValue = -1;
		int pointyBrackets = 0;
		boolean notValue = true;

		for(int i = 0; i < text.length(); i++) {
			if(text.charAt(i) == '<' && notValue && startName == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					startName = i;
				}
			}else if(text.charAt(i) == '>' && notValue && endName == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					endName = i;
					notValue = false;
				}
			} else if(text.charAt(i) == '<' && !notValue && startValue == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					startValue = i;
				}
			} else if(text.charAt(i) == '>' && !notValue && endValue == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					endValue = i;
					notValue = true;
				}
			}
			if (startName != -1 && endName != -1 && startValue != -1 && endValue != -1) {
				map.put(BeansUtils.eraseEscaping(text.substring(startName + 1, endName)), BeansUtils.eraseEscaping(text.substring(startValue + 1, endValue)));
				startName = -1;
				endName = -1;
				startValue = -1;
				endValue = -1;
			}
		}
		return map;
	}

	/**
	 * Converts string representation of an attribute value to correct java object
	 *
	 * @param stringValue string representation of the attribute value
	 * @param type type of the value ("Java.lang.String" for example)/
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	public static Object stringToAttributeValue(String stringValue, String type) throws InternalErrorException {
		if(stringValue == null || stringValue.isEmpty()) return null;

		Class<?> attributeClass;
		try {
			attributeClass = Class.forName(type);
		} catch (ClassNotFoundException e) {
			throw new InternalErrorException("Unknown attribute type", e);
		} catch (NoClassDefFoundError e) {
			throw new InternalErrorException("Unknown attribute def type", e);
		}

		if(attributeClass.equals(String.class)) {
			return stringValue;
		} else if(attributeClass.equals(Integer.class)) {
			return Integer.parseInt(stringValue);
		} else if(attributeClass.equals(ArrayList.class)) {
			String[] array = stringValue.split(Character.toString(LIST_DELIMITER), -1);
			List<String> attributeValue =  new ArrayList<String>();

			//join items which was splited on escaped LIST_DELIMITER
			for(int i = 0; i < array.length -1; i++) { //itarate to lenght -1  ... last array item is always empty
				String item = array[i];
				while(item.matches("^(.*[^\\\\])?(\\\\\\\\)*\\\\$")) { //item last char is '\' . Next item start with ',', so we need to concat this items.
					item = item.substring(0, item.length()-1);  //cut off last char ('\')
					try {
						item = item.concat(Character.toString(LIST_DELIMITER)).concat(array[i+1]);
						i++;
					} catch(ArrayIndexOutOfBoundsException ex) {
						throw new ConsistencyErrorException("Bad format in attribute value", ex);
					}
				}
				//unescape
				item = item.replaceAll("\\\\([\\\\" + Character.toString(LIST_DELIMITER) + "])", "$1");
				if(item.equals("\\0")) item = null;

				//return updated item back to list
				attributeValue.add(item);
			}

			return attributeValue;
		} else if(attributeClass.equals(LinkedHashMap.class)) {
			String[] array = stringValue.split(Character.toString(LIST_DELIMITER), -1);
			Map<String, String> attributeValue = new LinkedHashMap<String, String>();

			//join items which was splited on escaped LIST_DELIMITER
			for(int i = 0; i < array.length -1; i++) {  //itarate to lenght -1  ... last array item is always empty
				String mapEntry = array[i];

				while(mapEntry.matches("^(.*[^\\\\])?(\\\\\\\\)*\\\\$")) { //mapEntry last char is '\' . Next mapEntry start with ',', so we need to concat this mapEntries.
					mapEntry = mapEntry.substring(0, mapEntry.length()-1);  //cut off last char ('\')
					try {
						mapEntry = mapEntry.concat(Character.toString(LIST_DELIMITER)).concat(array[i+1]);
						i++;
					} catch(ArrayIndexOutOfBoundsException ex) {
						throw new ConsistencyErrorException("Bad format in attribute value", ex);
					}
				}

				boolean delimiterFound = false;
				int delimiterIndex = -1;


				while(!delimiterFound) {
					delimiterIndex++; //start searching at next char then last time
					delimiterIndex = mapEntry.indexOf(Character.toString(KEY_VALUE_DELIMITER), delimiterIndex);
					if(delimiterIndex == -1) throw new ConsistencyErrorException("Bad format in attribute value. KEY_VALUE_DELIMITER not found.");

					//check if this delimiter is not escaped
					boolean isEscaped = false;  //is delimiter escaped
					boolean stop = false;
					int processedIndex = delimiterIndex - 1;
					while(!stop && processedIndex >= 0) {
						if(mapEntry.charAt(processedIndex) == '\\') {
							isEscaped = !isEscaped;
						} else {
							stop = true;
						}
						processedIndex--;
					}
					if(!isEscaped) delimiterFound = true;
				}

				String key = mapEntry.substring(0, delimiterIndex);
				String value = mapEntry.substring(delimiterIndex+1);

				//unescape
				key = key.replaceAll("\\\\([\\\\" + Character.toString(LIST_DELIMITER) + Character.toString(KEY_VALUE_DELIMITER) + "])", "$1");
				value = value.replaceAll("\\\\([\\\\" + Character.toString(LIST_DELIMITER) + Character.toString(KEY_VALUE_DELIMITER) + "])", "$1");

				if(key.equals("\\0")) key = null;
				if(value.equals("\\0")) value = null;

				//return updated item back to list
				attributeValue.put(key, value);
			}

			return attributeValue;
		} else {
			throw new InternalErrorException("Unknown attribute type. ("+ attributeClass.toString() + ")");
		}

	}
}
