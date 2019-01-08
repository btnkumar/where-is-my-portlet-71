<%@ page import="com.lo.whereismy.portlet.helpers.Constants" %>

<%@ include file="../init.jsp" %>

<liferay-portlet:renderURL var="portletURL">
	<liferay-portlet:param name="selectedPrivate" value="${selectedPrivate}" />
</liferay-portlet:renderURL>

<c:if test="${ignoreScopeGroupIdFlag}">
	<div id="<portlet:namespace/>remarks">
		<img src="<%= request.getContextPath() %>/images/warn.png" /> <liferay-ui:message key="ignoreScopeGroupIdFlag.on" />
	</div>
</c:if>

<%
String tab = ParamUtil.getString(request, "tab", Constants.TAB_PORTLETS);
String tabsURL = "/html/where-is-my-portlet/" + tab.trim() + ".jsp";
%>

<liferay-ui:tabs
	names="portlets,layouts"
	param="tab"
	tabsValues="portlets,layouts"
	url="<%= portletURL %>"
	type="tabs nav-tabs-default"
/>

<c:import url="<%= tabsURL %>"></c:import>

<script>
AUI().ready(
'aui-tooltip',
function(A) {
	new A.Tooltip(
	{
		title: true,
		trigger: '.tooltip'
	}
	).render();
}
);

</script>