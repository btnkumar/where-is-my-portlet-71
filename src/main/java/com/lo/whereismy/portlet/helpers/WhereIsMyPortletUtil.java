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
package com.lo.whereismy.portlet.helpers;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.util.comparator.LayoutComparator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.lo.whereismy.portlet.views.LayoutView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

/**
 * Utility class for where is my portlet
 * @author Nagendra
 */
public class WhereIsMyPortletUtil implements Constants {

	public static String buildI18NPath(Locale locale) {
		String languageId = LocaleUtil.toLanguageId(locale);

		if (Validator.isNull(languageId)) {
			return null;
		}

		if (LanguageUtil.isDuplicateLanguageCode(locale.getLanguage())) {
			Locale priorityLocale = LanguageUtil.getLocale(
				locale.getLanguage());

			if (locale.equals(priorityLocale)) {
				languageId = locale.getLanguage();
			}
		} else {
			languageId = locale.getLanguage();
		}

		return StringPool.SLASH.concat(languageId);
	}

	public static String getBaseUrl(
		ThemeDisplay themeDisplay, Layout layout, boolean privatePage) {

		return getUrl(themeDisplay, layout, false, privatePage);
	}

	public static String getIconUrl(Portlet portlet) {
		if (portlet == null || Validator.isNull(portlet.getIcon()))

			return null;
		else

			return portlet.getContextPath() + portlet.getIcon();
	}

	public static String getLayoutHREF(Layout layout, ThemeDisplay themeDisplay,
			boolean privatePage) {

		return getLayoutHREF(layout, themeDisplay, false, privatePage);
	}

	public static String getLayoutHREF(Layout layout, ThemeDisplay themeDisplay,
			boolean openInNewPage, boolean privatePage) {

		return getLayoutHREF(
			layout, themeDisplay, openInNewPage, null, null, false,
			privatePage);
	}

	public static String getLayoutHREF(Layout layout, ThemeDisplay themeDisplay,
			boolean openInNewPage, String title, String className,
			boolean showBaseUrl, boolean privatePage) {

		String url = "";

		url = "<a ";
		url += openInNewPage ? "target=\"_blank\" " : "";

		if (Validator.isNotNull(title)) {
			url += openInNewPage ? "title=\"" + title + "\" " : "";
		}

		if (Validator.isNotNull(className)) {
			url += openInNewPage ? "class=\"" + className + "\" " : "";
		}

		url += "href=\"";
		url += getBaseUrl(themeDisplay, layout, privatePage) +
			layout.getFriendlyURL();
		url += "\">";

		if (showBaseUrl) {
			url += getBaseUrl(themeDisplay, layout, privatePage) +
				layout.getFriendlyURL();
		} else {
			url += layout.getFriendlyURL();
		}

		url += "</a>";

		return url;
	}

	public static String getLayoutUrl(
		ThemeDisplay themeDisplay, Layout layout, boolean privatePage) {

		return getUrl(themeDisplay, layout, true, privatePage);
	}

	public static String getLayoutUrl(
			ThemeDisplay themeDisplay, long layoutId, boolean privatePage)
		throws PortalException {

		return getUrl(themeDisplay,
				LayoutLocalServiceUtil.getLayout(themeDisplay.getScopeGroupId(), false, layoutId),
				true, privatePage);
	}

	public static long getPortletPlid(long groupId, String portletId) {
		try {
			return PortalUtil.getPlidFromPortletId(groupId, portletId);
		} catch (PortalException e) {
			logger.error(e.getMessage(), e);
		} catch (SystemException e) {
			logger.error(e.getMessage(), e);
		}

		return 0;
	}

	public static String getPortletTitleBarName(ThemeDisplay themeDisplay, String portletId,
			String portletDisplayName, long plid) {

		if (logger.isDebugEnabled()) {
			logger.debug("GETTING PORTLET TITLE BAR NAME...");
			logger.debug("  portletId: " + portletId);
			logger.debug("  portletDisplayName: " + portletDisplayName);
		}

		List<com.liferay.portal.kernel.model.PortletPreferences> preferencesList =
				PortletPreferencesLocalServiceUtil.getPortletPreferences(themeDisplay.getCompanyId(),
						themeDisplay.getScopeGroupId(),
						PortletKeys.PREFS_OWNER_ID_DEFAULT,
						PortletKeys.PREFS_OWNER_TYPE_LAYOUT, portletId, false);

		for (com.liferay.portal.kernel.model.PortletPreferences portletPreferences :
				preferencesList) {

			if (logger.isDebugEnabled()) {
				logger.debug("pp:" + portletPreferences);
			}

			if (portletPreferences.getPreferences() != null &&
				!portletPreferences.getPreferences().isEmpty() &&
				portletPreferences.getPlid() == plid) {

				try {
					Document document = SAXReaderUtil.read(
						portletPreferences.getPreferences());
					String propertyName =
						"portletSetupTitle_" + themeDisplay.getLocale();

					if (logger.isDebugEnabled())
						logger.debug("propertyName: " + propertyName);
					Node altTitleTabNode = document.selectSingleNode(
							"/portlet-preferences/preference[name='" + propertyName +
								"']/value");

					if (Validator.isNotNull(altTitleTabNode)) {
						portletDisplayName = altTitleTabNode.getText();

						if (logger.isDebugEnabled())
							logger.debug("new name:" + portletDisplayName);
					}
				} catch (DocumentException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return portletDisplayName;
	}

	public static String getUrl(ThemeDisplay themeDisplay, Layout layout, boolean appendLayoutUrl,
			boolean privatePage) {

		StringBundler url = new StringBundler();

		try {
			Locale locale = themeDisplay.getLocale();

			String virtualHostname = layout.getLayoutSet().getVirtualHostname();

			url.append(themeDisplay.getPortalURL());

			if (Validator.isNull(virtualHostname) ||
				!virtualHostname.equals(themeDisplay.getServerName())) {

				String pathFriendlyUrl =
					themeDisplay.getPathFriendlyURLPublic();

				if (privatePage) {
					pathFriendlyUrl =
						themeDisplay.getPathFriendlyURLPrivateGroup();
				}

				url.append(
					pathFriendlyUrl).append(layout.getGroup().getFriendlyURL());
			} else {
				if (!LocaleUtil.getDefault().equals(locale)) {
					String i18nPath = buildI18NPath(locale);

					if (Validator.isNotNull(i18nPath)) {
						url.append(i18nPath);
					}
				}
			}

			if (appendLayoutUrl) {
				url.append(layout.getFriendlyURL());
			}
		} catch (SystemException e) {
			logger.error(
				"SystemException: " + e.getMessage(), e.fillInStackTrace());
		}

		return url.toString();
	}

	/**
	 * If selectedPortlet is null, the we will try to get the first portlet of the available pages. If
	 * no portletName available then the result will be an empty array
	 *
	 * @param request the portlet request
	 * @param selectedPrivate working with private or public pages
	 * @param selectedPortlet the portlet name of the selected portlet
	 * @return a list of Layout views that contain portlets with the given portlet name
	 */
	private List<LayoutView> findLayoutsFromSelectedPortlet(PortletRequest request,
			boolean selectedPrivate, String selectedPortlet) {

		List<LayoutView> results = new ArrayList<>();

		try {
			List<Layout> allLayouts = getLayouts(request, selectedPrivate);
			SortedMap<String, String> sortedData = getSortedPortletNames(
				request, allLayouts);

			String portletName = null;

			if (Validator.isNull(selectedPortlet) && !sortedData.isEmpty()) {
				portletName = (String)sortedData.keySet().toArray()[0];
			} else {
				portletName = ParamUtil.getString(
					request, ATTRIBUTE_SELECTED_PORTLET);
			}

			results = wrapInView(
				getLayoutsContainingPortlet(allLayouts, portletName),
				portletName);
		} catch (SystemException e) {
			logger.error(e.getMessage(), e);
		} catch (PortalException e) {
			logger.error(e.getMessage(), e);
		}

		return results;
	}

	/**
	 * Returns the preference that indicates if we want to ignore the scope group id that is selected
	 * through the control panel in the content area
	 *
	 * @param request
	 * @return a boolean that indicates the value of the requested preference
	 */
	private boolean getIgnoreScopeGroupIdFlag(PortletRequest request) {
		PortletPreferences prefs = request.getPreferences();

		boolean ignoreScopeGroupIdFlag = Boolean.parseBoolean(
			prefs.getValue(PREFERENCE_IGNORE_SCOPE_GROUP_ID, "false"));

		return ignoreScopeGroupIdFlag;
	}

	private List<Layout> getLayouts(
		PortletRequest request, boolean selectedPrivate) {

		boolean ignoreScopeGroupIdFlag = getIgnoreScopeGroupIdFlag(request);

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);
		List<Layout> allLayouts = new LinkedList<>();

		if (ignoreScopeGroupIdFlag) {
			allLayouts = LayoutLocalServiceUtil.getLayouts(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		} else {
			allLayouts = LayoutLocalServiceUtil.getLayouts(
				themeDisplay.getScopeGroupId(), selectedPrivate);
		}

		List<Layout> sortedLayouts = new ArrayList<>(allLayouts);
		Collections.sort(sortedLayouts, new LayoutComparator());
		request.setAttribute("allLayouts", sortedLayouts);
		request.setAttribute("ignoreScopeGroupIdFlag", ignoreScopeGroupIdFlag);

		return allLayouts;
	}

	private List<Layout> getLayoutsContainingPortlet(
		List<Layout> layouts, String portletName) {

		List<Layout> results = new LinkedList<>();

		if (logger.isDebugEnabled())
			logger.debug("FILTERING LAYOUTS BY PORTLET NAME:" + portletName);

		LAYOUTS: for (Layout layout : layouts) {
			if (logger.isDebugEnabled())
				logger.debug("  CHECKING LAYOUT " + layout.getFriendlyURL());
			LayoutView layoutView = new LayoutView(layout, portletName);

			for (Portlet portlet : layoutView.getPortletInstances()) {
				if (logger.isDebugEnabled())
					logger.debug(
						"  CHECKING PORTLET " + portlet.getPortletName());

				if (portlet.getPortletName().equals(portletName)) {
					if (logger.isDebugEnabled())
						logger.debug("    ADDING PORTLET");
					results.add(layout);

					continue LAYOUTS;
				}
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("RETURNING " + results.size() + " FILTERED LAYOUTS");

		return results;
	}

	private SortedMap<String, String> getSortedPortletNames(PortletRequest renderRequest,
			List<Layout> allLayouts) throws PortalException {

		Map<String, String> portletNames = new HashMap<>();

		for (Layout layout : allLayouts) {
			LayoutTypePortlet layoutTypePortlet =
				(LayoutTypePortlet)layout.getLayoutType();

			List<Portlet> temp = layoutTypePortlet.getAllPortlets();

			for (Portlet portlet : temp) {
				if (!portletNames.containsKey(portlet.getPortletName()))
					portletNames.put(
						portlet.getPortletName(), portlet.getDisplayName());
			}
		}

		SortedMap<String, String> sortedData = new TreeMap<>(
			new ValueComparer(portletNames));
		sortedData.putAll(portletNames);
		renderRequest.setAttribute(ATTRIBUTE_PORTLET_NAMES, sortedData);

		return sortedData;
	}

	/**
	 * Wraps a Layout object in a LayoutView. It needs the portlet name to work correctly
	 *
	 * @param allLayouts
	 * @param portletName
	 * @return
	 */
	private List<LayoutView> wrapInView(
		List<Layout> allLayouts, String portletName) {

		List<LayoutView> results = new LinkedList<>();

		if (Validator.isNotNull(portletName)) {
			for (Layout layout : allLayouts) {
				results.add(new LayoutView(layout, portletName));
			}
		}

		return results;
	}

	private static final Log logger = LogFactoryUtil.getLog(
		WhereIsMyPortletUtil.class);

}