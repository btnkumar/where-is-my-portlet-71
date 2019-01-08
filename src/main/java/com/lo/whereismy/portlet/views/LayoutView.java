/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.lo.whereismy.portlet.views;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.Portlet;

import java.io.Serializable;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Nagendra
 *
 */
public class LayoutView implements Serializable {

	public LayoutView(Layout layout, String portletName) {
		this.layout = layout;
		this.layoutId = layout.getLayoutId();
		this.portletName = portletName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		LayoutView other = (LayoutView)obj;

		if (layoutId != other.layoutId) {
			return false;
		}

		return true;
	}

	public Layout getLayout() {
		return layout;
	}

	public long getLayoutId() {
		return layoutId;
	}

	public List<Portlet> getPortletInstances() {
		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		try {
			return layoutTypePortlet.getAllPortlets();
		}
		catch (SystemException se) {
			logger.error(se.getMessage(), se);
		}

		return null;
	}

	public List<Portlet> getPortletInstancesForPortletName() {
		List<Portlet> results = new LinkedList<>();

		for (Portlet portlet : getPortletInstances()) {
			if (portlet.getPortletName().equals(getPortletName())) {
				results.add(portlet);
			}
		}

		return results;
	}

	public String getPortletName() {
		return portletName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (layoutId ^ (layoutId >>> 32));

		return result;
	}

	private static final Log logger = LogFactoryUtil.getLog(LayoutView.class);
	private static final long serialVersionUID = 1L;

	private final Layout layout;
	private final long layoutId;
	private final String portletName;

}