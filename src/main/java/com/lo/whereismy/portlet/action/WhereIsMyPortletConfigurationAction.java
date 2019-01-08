/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

package com.lo.whereismy.portlet.action;

import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.lo.whereismy.configuration.WhereIsMyPortletConfiguration;
import com.lo.whereismy.portlet.constants.WhereIsMyPortlet71PortletKeys;

/**
 * Configuration action for portlet
 * @author Nagendra
 */
@Component(configurationPid = "com.lo.whereismy.configuration.WhereIsMyPortletConfiguration",
		configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
		property = "javax.portlet.name=" + WhereIsMyPortlet71PortletKeys.WhereIsMyPortlet71,
		service = ConfigurationAction.class)
public class WhereIsMyPortletConfigurationAction extends DefaultConfigurationAction {

	@Override
	public void include(PortletConfig portletConfig, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws Exception {

		_log.log(LogService.LOG_INFO, "Where is my Portlet configuration include");

		httpServletRequest.setAttribute(WhereIsMyPortletConfiguration.class.getName(),
				_whereIsMyPortletConfiguration);

		super.include(portletConfig, httpServletRequest, httpServletResponse);
	}

	@Override
	public void processAction(PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse) throws Exception {

		String ignoreScopeGroupIdFlag = ParamUtil.getString(actionRequest, "ignoreScopeGroupIdFlag");
		String showBaseUrl = ParamUtil.getString(actionRequest, "showBaseUrl");
		String popupWidth = ParamUtil.getString(actionRequest, "popupWidth");
		String popupHeight = ParamUtil.getString(actionRequest, "popupHeight");
		String enableFilters = ParamUtil.getString(actionRequest, "enableFilters");
		_log.log(LogService.LOG_INFO,
				"Preference values : \nignoreScopeGroupIdFlag=" + ignoreScopeGroupIdFlag + "\nshowBaseUrl="
						+ showBaseUrl + "\npopupWidth=" + popupWidth + "\npopupWidth=" + popupWidth
						+ "\npopupHeight=" + popupHeight + "\nenableFilters=" + enableFilters);
		setPreference(actionRequest, "ignoreScopeGroupIdFlag", ignoreScopeGroupIdFlag);
		setPreference(actionRequest, "showBaseUrl", showBaseUrl);
		setPreference(actionRequest, "popupWidth", popupWidth);
		setPreference(actionRequest, "popupHeight", popupHeight);
		setPreference(actionRequest, "enableFilters", enableFilters);

		super.processAction(portletConfig, actionRequest, actionResponse);
	}

	@Activate
	@Modified
	protected void activate(Map<Object, Object> properties) {
		_whereIsMyPortletConfiguration =
				ConfigurableUtil.createConfigurable(WhereIsMyPortletConfiguration.class, properties);
	}

	@Reference
	private LogService _log;

	private volatile WhereIsMyPortletConfiguration _whereIsMyPortletConfiguration;

}
