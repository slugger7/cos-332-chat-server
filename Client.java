//Kevin David Heritage u13044924
//Quinton Weenink u13176545

import java.net.*;
import java.io.*;
import java.util.*;

public class Client
{
	private DataInputStream input;
	private DataOutputStream output;
	private Socket socket;

	private static String host;
	private static String username;
	private static int port;
	
	private static Scanner scan = new Scanner(System.in);

	Client()
	{
		//do nothing
	}

	public boolean start()
	{
		try
		{
			socket = new Socket(host, port);
		}
		catch (Exception ex)
		{
			System.out.println("There was an error connecting to the server: " + ex.toString());
			return false;
		}

		System.out.println("Connection accepted " + socket.getInetAddress() + " : " + socket.getPort());

		try
		{
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException ioe)
		{
			System.out.println("Exception was caught while setting up streams: " + ioe.toString());
			return false;
		}

		new ListenFromServer().start();

		try
		{
			output.writeUTF(username);
			output.flush();
		}
		catch (IOException ioe)
		{
			System.out.println("Exception at login: " + ioe.toString());
			disconnect();
			return false;
		}

		return true;
	}

	void sendMessage(String message)
	{
		try
		{
			output.writeUTF(message);
			output.flush();
		}
		catch (IOException ioe)
		{
			System.out.println("Something went wrong when writing to server: " + ioe.toString());
		}
	}

	private void disconnect()
	{
		try
		{
			if (input != null)
				input.close();
			if (output != null)
				output.close();
			if (socket != null)
				socket.close();
		}
		catch (Exception ex)
		{
		
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Please enter the server address: ");
		host = scan.nextLine();
		System.out.println("Please enter the port number: ");
		port = Integer.parseInt(scan.nextLine());
		System.out.println("Enter your username: ");
		username = scan.nextLine();

		Client client = new Client();
		
		if (!client.start())
			return;

		while (true)
		{
			String msg = scan.nextLine();

			client.sendMessage(msg);
			if (msg.equalsIgnoreCase("logout"))
			{
				break;
			}
		}
		client.disconnect();
	}

	class ListenFromServer extends Thread
	{
		public void run()
		{
			while (true)
			{
				try
				{
					String message = (String) input.readUTF();
					System.out.println(message);
				}
				catch (IOException ioe)
				{
					System.out.println("Seems like the server has closed the connection: " + ioe.toString());
					break;
				}
				catch (Exception e)
				{
					System.out.println("Error" + e.getMessage());
				}
			}
		}
	}
}