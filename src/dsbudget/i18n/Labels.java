package dsbudget.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class handles all the label resources used in dsbudget.
 * 
 */
public class Labels {
	/**
	 * The bundle name.
	 */
	private static final String BUNDLE_NAME = "dsbudget.i18n.labels";

	/**
	 * The resource bundle.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = UTF8ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Retrieve the value associated to the key, and replace the parameters.
	 * 
	 * @param key
	 *            the key.
	 * @param params
	 *            the values that should be used as the parameters values ({0},
	 *            {1}...).
	 * @return the value.
	 */
	public static String getString(String key, Object[] params) {
		return getValue(key, false, params);
	}

	/**
	 * Retrieve the HTML escaped value associated to the key, and replace the
	 * parameters.
	 * 
	 * @param key
	 *            the key.
	 * @param params
	 *            the values that should be used as the parameters values ({0},
	 *            {1}...).
	 * @return the value.
	 */
	public static String getHtmlEscapedString(String key, Object[] params) {
		return getValue(key, true, params);
	}

	/**
	 * Retrieve the value associated to the key, and replace the parameter.
	 * 
	 * @param key
	 *            the key.
	 * @param param
	 *            the values that should be used as the parameters values ({0}).
	 * @return the value.
	 */
	public static String getString(String key, Object param) {
		return getString(key, new Object[] { param });
	}

	/**
	 * Retrieve the HTML escaped value associated to the key, and replace the
	 * parameter.
	 * 
	 * @param key
	 *            the key.
	 * @param param
	 *            the values that should be used as the parameters values ({0}).
	 * @return the value.
	 */
	public static String getHtmlEscapedString(String key, Object param) {
		return getHtmlEscapedString(key, new Object[] { param });
	}

	/**
	 * Retrieve the value associated to the key.
	 * 
	 * @param key
	 *            the key.
	 * @return the value.
	 */
	public static String getString(String key) {
		return getValue(key, false);
	}

	/**
	 * Retrieve the HTML escaped value associated to the key.
	 * 
	 * @param key
	 *            the key.
	 * @return the value.
	 */
	public static String getHtmlEscapedString(String key) {
		return getValue(key, true);
	}

	/**
	 * Retrieve the value associated to the key
	 * 
	 * @param key
	 *            the key.
	 * @param escape
	 *            escape the HTML charaters.
	 * @param params
	 *            the values that should be used as the parameters values ({0},
	 *            {1}...).
	 * @return
	 */
	private static String getValue(String key, boolean escape, Object[] params) {
		String value = getValue(key, escape);

		if (value != null) {
			MessageFormat formatter = new MessageFormat(value);
			value = formatter.format(params);
		}

		return value;
	}

	/**
	 * Retrieve the value associated to the key.
	 * 
	 * @param key
	 *            the key.
	 * @param escape
	 *            escape HTML.
	 * @return the value.
	 */
	private static String getValue(String key, boolean escape) {
		String value = null;
		try {
			value = RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			value = "!NO VALUE FOR:" + key + "!";
		}
		if (escape) {
			return StringEscapeUtils.escapeHtml(value);
		}
		return value;
	}

}
