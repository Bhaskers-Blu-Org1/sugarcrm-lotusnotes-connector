package com.ibm.socialcrm.notesintegration.sfawebapi;

/****************************************************************
 * IBM Confidential
 *
 * SFA050-Collaboration Source Materials
 *
 * (C) Copyright IBM Corp. 2012
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office
 *
 ***************************************************************/

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateMeeting extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5256666343048728656L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// String relatedId = request.getParameter("relatedId");
		String[] relatedIds = request.getParameterValues("relatedId"); //$NON-NLS-1$

		String module = request.getParameter("module");
		if (module == null) {
			module = request.getParameter("?module");
		}
		boolean errorOccurred = false;
		String errorMsg = "";
		String errorCode = "";
		// System.out.println("In CreateMeeting servlet " + request.getRequestURL());

		// Defect 18880: responding right away since Sugar is sensitive to creating the meeting taking longer than expected and causes multiple invites.
		if (relatedIds == null || relatedIds.length == 0 || module == null || module.equals("")) {
			errorOccurred = true;
			errorMsg = "Invalid parameters";
			errorCode = "1";
		} else if (!module.equals("Accounts") && !module.equals("Opportunities") && !module.equals("Contacts") && !module.equals("Leads")) {
			errorOccurred = true;
			errorMsg = "Invalid module";
			errorCode = "2";
		}
		String responseStr = "";
		response.setContentType("application/json");

		// determine if using jsonp for the response. if so, pad with the function
		String format = request.getParameter("format");
		if (format != null && format.equals("jsonp")) {
			String callback = request.getParameter("callback");
			responseStr += callback + "(";

		}
		if (errorOccurred) {
			response.setStatus(500);
			responseStr += "{\"status\":\"error\",\"code\":\"" + errorCode + "\",\"message\":\"" + errorMsg + "\"}";
		} else {
			response.setStatus(200);
			responseStr += "{\"status\":\"success\"}";
		}
		// close the jsonp call
		if (format != null && format.equals("jsonp")) {
			responseStr += ");";
		}
		// System.out.println("generated output:" + responseStr);
		OutputStream out = response.getOutputStream();
		out.write(responseStr.getBytes());
		out.flush();
		out.close();

		// if no error, then create the meeting. Needs it's own thread as the output doesn't make it to sugar first if we do more work here in this thread
		if (!errorOccurred) {
			Thread t = new Thread(new MeetingHelper(module, relatedIds));
			t.start();
		}

	}

}
