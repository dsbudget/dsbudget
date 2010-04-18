package dsbudget.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dsbudget.Main;

public class StopServlet extends ServletBase {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		Main.stop();
	}
}
