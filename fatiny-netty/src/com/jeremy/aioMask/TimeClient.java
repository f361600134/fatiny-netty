package com.jeremy.aioMask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TimeClient {
	
	public static void main(String[] args) {
		String ip = "127.0.0.1";
		int port = 8080;
		
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			//ServerSocket server = new ServerSocket(port);
			//System.out.println("The time server is start in port:"+port);
			socket = new Socket(ip, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("Query time server");
			System.out.println("Send order to server successed");
			String resp = in.readLine();
			System.out.println("Now is :"+resp);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (socket != null) {
				try { 
					socket.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				socket = null;
			}
		}
		}
}
