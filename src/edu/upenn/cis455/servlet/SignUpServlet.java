package edu.upenn.cis455.servlet;

import edu.upenn.cis455.storage.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.Transaction;

@SuppressWarnings("serial")
public class SignUpServlet extends HttpServlet {
	
	private DBWrapper createDB(String dir) throws Exception {
		return new DBWrapper(dir);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
	    request.getSession(true);

	    try {
		    // Set up DB
	    	DBWrapper db = createDB(getServletContext().getInitParameter("BDBstore"));
	    	UserDB userDB = db.getUserDB();
	    	
	    	// Get sign up params
		    String usr = request.getParameter("email");
		    String pwd = request.getParameter("pwd");
		    
		    if (usr == null || pwd == null) {
		    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		    	return;
		    }
		    
			// Check that no user is currently logged in
			boolean loggedIn = false;
			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("cis455session")) loggedIn = true;
			}
			
			// Try to insert user
			Transaction t = db.getTransaction();
			boolean insertSuccess = userDB.insertUser(usr, pwd);
			t.commit();
			
			if (loggedIn || !insertSuccess) { 
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Sign Up</title></head><body>");	
			    out.println("<h3>Sign Up Unsuccessful!</h3>");
			    if (loggedIn) {
			    	out.println("<p>You are already logged in. Please log out before creating a new account.</p>");
			    	out.println("<a href=\"/HW2/logout\">Logout</a>");
			    } else {
			    	out.println("<p>That account name is already in use. Please try again.</p>");
			    	out.println("<a href=\"/HW2/signup\">Sign Up</a>");
			    }
			    out.println("</body></html>");
			} else {				
				// Log the user in with a session cookie
				Cookie login = new Cookie("cis455session", usr);
				login.setMaxAge(-1); // session persistent
				response.addCookie(login);
				
				PrintWriter out = response.getWriter();
			    out.println("<html><head><title>XPath Servlet - Sign Up</title></head><body>");	
			    out.println("<h3>Sign Up Successful!</h3>");
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
	    UserDB db;
	    try {
	    	db = createDB(getServletContext().getInitParameter("BDBstore")).getUserDB();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
		try {
			PrintWriter out = response.getWriter();
		    out.println("<html><head><title>XPath Servlet - Sign Up</title></head><body>");	
		    out.println("<h1>Sign Up</h1>");
		    out.println("<form method=\"post\">");
		    out.println("Email: <input type=\"text\" name=\"email\"><br/>");
		    out.println("Password: <input type=\"text\" name=\"pwd\"><br/>");
		    out.println("<input type=\"submit\" value=\"Sign Up\"></form>");
		    out.println("<a href=\"/login\">Already have an account? Login here!</a>");
		    out.println("</body></html>");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
