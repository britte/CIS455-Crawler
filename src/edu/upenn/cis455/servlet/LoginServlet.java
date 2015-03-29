package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.storage.UserDB;

@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {
	
	private DBWrapper createDB(String dir) throws Exception {
		return new DBWrapper(dir);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
	    request.getSession(true);
	    
	    try {
		    // Set up DB
	    	UserDB db = createDB(getServletContext().getInitParameter("BDBstore")).getUserDB();
	    	
	    	// Get login params
		    String email = request.getParameter("email").trim();
		    String pwd = request.getParameter("pwd").trim();
		    
		    if (email == null || pwd == null) {
		    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		    	return;
		    }
		    
			// Check that no user is currently logged in
			boolean loggedIn = false;
			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("cis455session")) loggedIn = true;
			}
			
			User usr = db.getUserByEmail(email);	
			boolean loginSuccess = (usr != null && usr.getPwd().equals(pwd));
			
			if (loggedIn || !loginSuccess) { 
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Login</title></head><body>");	
			    out.println("<h3>Login Unsuccessful!</h3>");
			    if (loggedIn) {
			    	out.println("<p>You are already logged in. Please log out before logging into a new account.</p>");
			    	out.println("<a href=\"/HW2/logout\">Logout</a>");
			    } else {
			    	out.println("<p>That account was not found in our system. Please try again or sign up.</p>");
			    	out.println("<a href=\"/HW2/login\">Login</a><br />");
			    	out.println("<a href=\"/HW2/signup\">Sign Up</a>");
			    }
			    out.println("</body></html>");
			} else {
				// Create a session cookie for the user
				Cookie login = new Cookie("cis455session", email);
				login.setMaxAge(-1); // session persistent
				response.addCookie(login);
				
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Sign Up</title></head><body>");	
			    out.println("<h3>Login Successful!</h3>");
			    out.println("</body></html>");
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/html");
	    request.getSession(true);
	    
		try {
			PrintWriter out = response.getWriter();
		    out.println("<html><head><title>XPath Servlet - Login</title></head><body>");	
		    out.println("<h1>Login</h1>");
		    out.println("<form method=\"post\">");
		    out.println("Email: <input type=\"text\" name=\"email\"><br/>");
		    out.println("Password: <input type=\"text\" name=\"pwd\"><br/>");
		    out.println("<input type=\"submit\" value=\"Login\"></form>");
		    out.println("<a href=\"/HW2/signup\">Don't have an account? Sign up here!</a>");
		    out.println("</body></html>");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
