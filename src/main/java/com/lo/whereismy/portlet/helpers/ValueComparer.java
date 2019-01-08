package com.lo.whereismy.portlet.helpers;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Nagendra
 *
 */
public class ValueComparer implements Comparator<String> {

	public ValueComparer(Map<String, String> data) {
		_data = data;
	}

	public int compare(String o1, String o2) {
		String e1 = (String) _data.get(o1);
		String e2 = (String) _data.get(o2);

		return e1.compareTo(e2);
	}

	private Map<String, String> _data = null;

}