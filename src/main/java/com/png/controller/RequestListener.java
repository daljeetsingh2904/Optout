package com.png.controller;

import javax.servlet.http.HttpServlet;

public class RequestListener extends HttpServlet{

	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception
	    {
	    	AsteriskController obj=new AsteriskController();
	    	obj.start();
	    }
}
