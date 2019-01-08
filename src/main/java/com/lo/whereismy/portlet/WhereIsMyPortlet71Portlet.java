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

package com.lo.whereismy.portlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.util.comparator.LayoutComparator;
import com.lo.whereismy.portlet.constants.WhereIsMyPortlet71PortletKeys;
import com.lo.whereismy.portlet.helpers.Constants;
import com.lo.whereismy.portlet.helpers.ValueComparer;
import com.lo.whereismy.portlet.views.LayoutView;
import com.lo.whereismy.portlet.views.PortletView;

/**
 * Portlet class
 *
 * @author Nagendra
 */
@Component(immediate = true, property = {"com.liferay.portlet.display-category=category.simple",
		"com.liferay.portlet.instanceable=false", "com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=where-is-my-portlet71",
		"com.liferay.portlet.render-weight=100", "com.liferay.portlet.preferences-owned-by-group=true",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/html/where-is-my-portlet/view.jsp",
		"javax.portlet.init-param.help-template=/html/where-is-my-portlet/help.jsp",
		"javax.portlet.init-param.about-template=/html/where-is-my-portlet/about.jsp",
		"javax.portlet.init-param.config-template=/html/where-is-my-portlet/config.jsp",
		"javax.portlet.name=" + WhereIsMyPortlet71PortletKeys.WhereIsMyPortlet71,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=administrator", "javax.portlet.expiration-cache=0"},
		service = Portlet.class)
public class WhereIsMyPortlet71Portlet extends MVCPortlet implements Constants {

	public void doView(RenderRequest request, RenderResponse renderResponse)
			throws IOException, PortletException {

		boolean selectedPrivate = ParamUtil.getBoolean(request, ATTRIBUTE_SELECTED_PAGES_SCOPE, false);
		String tab = ParamUtil.getString(request, ATTRIBUTE_TAB, TAB_PORTLETS);
		boolean searchDone = ParamUtil.getBoolean(request, ATTRIBUTE_SEARCH_DONE, false);

		if (logger.isDebugEnabled()) {
			logger.debug("RENDER PHASE: ");
			logger.debug("  selectedPrivate: " + selectedPrivate);
			logger.debug("  tab: " + tab);
			logger.debug("  searchDone: " + searchDone);
		}

		if (!searchDone && tab.equals(TAB_LAYOUTS)) {
			try {
				List<Layout> layouts = getLayouts(request, selectedPrivate);
				request.setAttribute(ATTRIBUTE_ALL_LAYOUTS, layouts);
				request.setAttribute("portlets", getPortletsFromSelectedLayout(request));
			} catch (
				SystemException e
			) {
				logger.error(e.getMessage(), e);
			}
		}

		if (!searchDone && tab.equals(TAB_PORTLETS)) {
			List<LayoutView> layouts = findLayoutsFromSelectedPortlet(request, selectedPrivate, null);
			request.setAttribute(ATTRIBUTE_PORTLET_LAYOUTS, layouts);
			request.setAttribute(ATTRIBUTE_PORTLET_LAYOUTS_SIZE, layouts.size());

			if (logger.isDebugEnabled()) {
				logger.debug("RETURNING FROM DO VIEW:");
				logger.debug("  layouts:" + layouts.size());
			}
		}

		super.doView(request, renderResponse);
	}

	public void serveResource(ResourceRequest request, ResourceResponse resourceResponse)
			throws IOException, PortletException {

		boolean selectedPrivate =
				ParamUtil.getBoolean(request, "json" + ATTRIBUTE_SELECTED_PAGES_SCOPE, false);
		String tab = ParamUtil.getString(request, "json" + ATTRIBUTE_TAB, TAB_PORTLETS);
		int action = ParamUtil.getInteger(request, "json" + ATTRIBUTE_ACTION);
		boolean pageScope = ParamUtil.getBoolean(request, "json" + ATTRIBUTE_SELECTED_PAGES_SCOPE);

		if (logger.isDebugEnabled()) {
			logger.debug("JSON REQUEST:");
			logger.debug("  selectedPrivate:" + selectedPrivate);
			logger.debug("  tab:" + tab);
			logger.debug("  action:" + action);
			logger.debug("  pageScope:" + pageScope);
		}

		JSONArray results = JSONFactoryUtil.getJSONFactory().createJSONArray();

		try {
			switch (action) {
				case ACTION_PAGE_SCOPE_PORTLETS:

					SortedMap<String, String> portletNames =
							getSortedPortletNames(request, getLayouts(request, selectedPrivate));

					for (String portletName : portletNames.keySet()) {
						JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
						String portletDisplayName = portletNames.get(portletName);

						jsonObject.put(JSON_PORTLET_NAME, portletName);
						jsonObject.put(JSON_PORTLET_DISPLAY_NAME, portletDisplayName);

						results.put(jsonObject);
					}

					break;
				case ACTION_PAGE_SCOPE_LAYOUTS:

					break;
				default:

					break;
			}
		} catch (
			PortalException e
		) {
			logger.error(e.getMessage(), e);
		} catch (
			SystemException e
		) {
			logger.error(e.getMessage(), e);
		}

		resourceResponse.setContentType(ContentTypes.APPLICATION_JSON);
		resourceResponse.setCharacterEncoding(StringPool.UTF8);
		PrintWriter pw = null;

		try {
			pw = resourceResponse.getWriter();
			pw.write(results.toString());
		} catch (
			IOException e
		) {
			logger.error("IOException: " + e.getMessage());
		} finally {
			if (Validator.isNotNull(pw)) {
				pw.flush();
				pw.close();
			}
		}
	}

	public void showLayoutPortlets(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {

		String selectedLayout = ParamUtil.getString(actionRequest, ATTRIBUTE_SELECTED_LAYOUT);
		boolean selectedPrivate = ParamUtil.getBoolean(actionRequest, "selectedPrivate");

		if (logger.isDebugEnabled()) {
			logger.debug("selectedLayout: " + selectedLayout);
			logger.debug("selectedPrivate: " + selectedPrivate);
		}

		List<PortletView> portlets = getPortletsFromSelectedLayout(actionRequest);
		actionRequest.setAttribute("portlets", portlets);
		actionRequest.setAttribute("searchDone", Boolean.TRUE);
		actionRequest.setAttribute("selectedPrivate", selectedPrivate);

		actionResponse.setRenderParameter("selectedPrivate", String.valueOf(selectedPrivate));
		actionResponse.setRenderParameter(ATTRIBUTE_SELECTED_LAYOUT, selectedLayout);
		actionResponse.setRenderParameter("tab", "layouts");
	}

	public void showPortletLayouts(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {

		String selectedPortlet = ParamUtil.getString(actionRequest, "selectedPortlet");
		boolean selectedPrivate = ParamUtil.getBoolean(actionRequest, "selectedPrivate");

		if (logger.isDebugEnabled()) {
			logger.debug("selectedPortlet: " + selectedPortlet);
			logger.debug("selectedPrivate: " + selectedPrivate);
		}

		List<LayoutView> layouts =
				findLayoutsFromSelectedPortlet(actionRequest, selectedPrivate, selectedPortlet);

		actionRequest.setAttribute("portletLayouts", layouts);
		actionRequest.setAttribute("portletLayoutsSize", layouts.size());
		actionRequest.setAttribute("searchDone", Boolean.TRUE);
		actionRequest.setAttribute("selectedPortlet", selectedPortlet);
		actionRequest.setAttribute("selectedPrivate", selectedPrivate);

		actionResponse.setRenderParameter("selectedPrivate", String.valueOf(selectedPrivate));
		actionResponse.setRenderParameter("searchDone", Boolean.TRUE.toString());

		if (logger.isDebugEnabled()) {
			logger.debug("RETURNING:");
			logger.debug("  selectedPortletName:" + selectedPortlet);
			logger.debug("  portletLayouts:" + layouts);
			logger.debug("  portletLayoutsSize:" + layouts.size());
		}
	}

	public Log logger = LogFactoryUtil.getLog(WhereIsMyPortlet71Portlet.class);

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
			SortedMap<String, String> sortedData = getSortedPortletNames(request, allLayouts);

			String portletName = null;

			if (Validator.isNull(selectedPortlet) && !sortedData.isEmpty()) {
				portletName = (String) sortedData.keySet().toArray()[0];
			} else {
				portletName = ParamUtil.getString(request, ATTRIBUTE_SELECTED_PORTLET);
			}

			results = wrapInView(getLayoutsContainingPortlet(allLayouts, portletName), portletName);
		} catch (
			SystemException e
		) {
			logger.error(e.getMessage(), e);
		} catch (
			PortalException e
		) {
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

		boolean ignoreScopeGroupIdFlag =
				Boolean.parseBoolean(prefs.getValue(PREFERENCE_IGNORE_SCOPE_GROUP_ID, "false"));

		return ignoreScopeGroupIdFlag;
	}

	private List<Layout> getLayouts(PortletRequest request, boolean selectedPrivate) {

		boolean ignoreScopeGroupIdFlag = getIgnoreScopeGroupIdFlag(request);

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		List<Layout> allLayouts = new LinkedList<>();

		if (ignoreScopeGroupIdFlag) {
			allLayouts = LayoutLocalServiceUtil.getLayouts(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		} else {
			allLayouts =
					LayoutLocalServiceUtil.getLayouts(themeDisplay.getScopeGroupId(), selectedPrivate);
		}

		List<Layout> sortedLayouts = new ArrayList<>(allLayouts);
		Collections.sort(sortedLayouts, new LayoutComparator());
		request.setAttribute("allLayouts", sortedLayouts);
		request.setAttribute("ignoreScopeGroupIdFlag", ignoreScopeGroupIdFlag);

		return allLayouts;
	}

	private List<Layout> getLayoutsContainingPortlet(List<Layout> layouts, String portletName) {

		List<Layout> results = new LinkedList<>();

		if (logger.isDebugEnabled()) {
			logger.debug("FILTERING LAYOUTS BY PORTLET NAME:" + portletName);
		}

		LAYOUTS: for (Layout layout : layouts) {
			if (logger.isDebugEnabled()) {
				logger.debug("  CHECKING LAYOUT " + layout.getFriendlyURL());
			}

			LayoutView layoutView = new LayoutView(layout, portletName);

			for (com.liferay.portal.kernel.model.Portlet portlet : layoutView.getPortletInstances()) {
				if (logger.isDebugEnabled()) {
					logger.debug("  CHECKING PORTLET " + portlet.getPortletName());
				}

				if (portlet.getPortletName().equals(portletName)) {
					if (logger.isDebugEnabled()) {
						logger.debug("    ADDING PORTLET");
					}

					results.add(layout);

					continue LAYOUTS;
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("RETURNING " + results.size() + " FILTERED LAYOUTS");
		}

		return results;
	}

	private List<PortletView> getPortletsFromSelectedLayout(PortletRequest request) {

		boolean selectedPrivate = ParamUtil.getBoolean(request, ATTRIBUTE_SELECTED_PAGES_SCOPE, false);
		String selectedLayout = ParamUtil.getString(request, ATTRIBUTE_SELECTED_LAYOUT);
		List<com.liferay.portal.kernel.model.Portlet> portlets = new LinkedList<>();
		Layout selLayout = null;

		try {
			if (Validator.isNotNull(selectedLayout)) {
				if (logger.isDebugEnabled()) {
					logger.debug("GOT SELECTED LAYOUT FROM REQEUST:" + selectedLayout);
				}
			} else {
				List<Layout> layouts = getLayouts(request, selectedPrivate);

				if (!layouts.isEmpty())
					selectedLayout = String.valueOf(layouts.get(0).getPlid());

				if (logger.isDebugEnabled()) {
					logger.debug("GOT SELECTED LAYOUT FROM LIST (NOT FROM REQEUST):" + selectedLayout);
				}
			}

			if (Validator.isNotNull(selectedLayout)) {
				selLayout = LayoutLocalServiceUtil.getLayout(Long.parseLong(selectedLayout));
				LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) selLayout.getLayoutType();

				portlets = layoutTypePortlet.getAllPortlets();

				if (logger.isDebugEnabled()) {
					logger.debug("PORTLETS FOUND:" + portlets.size());
				}
			}
		} catch (
			NumberFormatException e
		) {
			logger.error(e.getMessage(), e);
		} catch (
			PortalException e
		) {
			logger.error(e.getMessage(), e);
		} catch (
			SystemException e
		) {
			logger.error(e.getMessage(), e);
		}

		return wrap(portlets, selLayout);
	}

	private SortedMap<String, String> getSortedPortletNames(PortletRequest renderRequest,
			List<Layout> allLayouts) throws PortalException {

		Map<String, String> portletNames = new HashMap<>();

		for (Layout layout : allLayouts) {
			LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout.getLayoutType();

			List<com.liferay.portal.kernel.model.Portlet> temp = layoutTypePortlet.getAllPortlets();

			for (com.liferay.portal.kernel.model.Portlet portlet : temp) {
				if (!portletNames.containsKey(portlet.getPortletName()))
					portletNames.put(portlet.getPortletName(), portlet.getDisplayName());
			}
		}

		SortedMap<String, String> sortedData = new TreeMap<>(new ValueComparer(portletNames));
		sortedData.putAll(portletNames);
		renderRequest.setAttribute(ATTRIBUTE_PORTLET_NAMES, sortedData);

		return sortedData;
	}

	private List<PortletView> wrap(List<com.liferay.portal.kernel.model.Portlet> portlets,
			Layout layout) {
		List<PortletView> results = new LinkedList<>();

		for (com.liferay.portal.kernel.model.Portlet portlet : portlets) {
			results.add(wrap(portlet, layout));
		}

		return results;
	}

	private PortletView wrap(com.liferay.portal.kernel.model.Portlet portlet, Layout layout) {
		return new PortletView(portlet, layout);
	}

	/**
	 * Wraps a Layout object in a LayoutView. It needs the portlet name to work correctly
	 *
	 * @param allLayouts
	 * @param portletName
	 * @return
	 */
	private List<LayoutView> wrapInView(List<Layout> allLayouts, String portletName) {

		List<LayoutView> results = new LinkedList<>();

		if (Validator.isNotNull(portletName)) {
			for (Layout layout : allLayouts) {
				results.add(new LayoutView(layout, portletName));
			}
		}

		return results;
	}

}
