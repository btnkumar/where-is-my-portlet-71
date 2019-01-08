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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.PortletPreferences;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.lo.whereismy.portlet.helpers.WhereIsMyPortletUtil;
import java.io.Serializable;

import java.util.List;

/**
 * Portlet view
 * @author Nagendra
 */
public class PortletView implements Serializable {

	public PortletView(Portlet portlet, Layout layout) {
		if (portlet == null) {
			throw new IllegalArgumentException(
				"CANNOT PASS A null IN PortletView CONSTRUCTOR");
		}

		this.portlet = portlet;
		this.portletId = portlet.getPortletId();
		this.layout = layout;
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

		PortletView other = (PortletView)obj;

		if (portletId == null) {
			if (other.portletId != null) {
				return false;
			}
		} else if (!portletId.equals(other.portletId)) {

			return false;
		}

		return true;
	}

	public Layout getLayout() {
		return layout;
	}

	public long getPlid(long groupId) throws PortalException {
		return PortalUtil.getPlidFromPortletId(
			groupId, getPortlet().getPortletId());
	}

	public Portlet getPortlet() {
		return portlet;
	}

	public String getPortletBarName(ThemeDisplay themeDisplay) {
		String result = "";

		try {
			result = WhereIsMyPortletUtil.getPortletTitleBarName(themeDisplay,
				getPortlet().getPortletId(), getPortlet().getDisplayName(),
				getLayout().getPlid());
		} catch (SystemException e) {
			logger.error(e.getMessage(), e);
		}

		return result;
	}

	public String getPortletId() {
		return portletId;
	}

	public List<PortletPreferences> getPortletPreferences(long groupId)
		throws PortalException {

		return PortletPreferencesLocalServiceUtil.getPortletPreferences(getPlid(groupId),
			getPortlet().getPortletId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result + ((portletId == null) ? 0 : portletId.hashCode());

		return result;
	}

	private static final Log logger = LogFactoryUtil.getLog(PortletView.class);
	private static final long serialVersionUID = 1L;

	private final Layout layout;
	private final Portlet portlet;
	private final String portletId;

}