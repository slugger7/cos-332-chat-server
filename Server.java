//Kevin David Heritage u13044924
//Quinton Weenink u13176545

import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
	private ArrayList<ClientThread> clients;
	private static int port;
	private boolean notDone;
	
	private static Scanner scan = new Scanner(System.in);

	//Constructor
	public Server()
	{
		clients = new ArrayList<ClientThread>();
	}

	public void start()
	{
		notDone = true;
		try
		{
			//creating a socket for the server
			ServerSocket serverSocket = new ServerSocket(port);

			//listening for new clients
			while (notDone)
			{
				System.out.println("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();

				//to stop notDone will be false and then a new blank client will connect
				if (!notDone)
					break;

				//create new thread for the new client and add it to the list
				ClientThread clientThread = new ClientThread(socket);
				clients.add(clientThread);
				clientThread.start();
				broadcast(clientThread.username + " has just connected to the chat");
			}

			//when the server needs to stop
			try
			{
				serverSocket.close();
				for (int i = 0; i < clients.size(); ++i)
				{
					ClientThread tempClient = clients.get(i);
					try
					{
						//close all client sockets and streams
						tempClient.input.close();
						tempClient.output.close();
						tempClient.socket.close();
					}
					catch (IOException ioe)
					{
						System.out.println(ioe);
					}
				}
			}
			catch (Exception ex)
			{
				System.out.println("Exception closing the server and clients: " + ex.toString());
			}
		}
		catch (IOException ex)
		{
			System.out.println("error: " + ex);
		}
	}

	public void stop()
	{
		//indicating that the next connecting client should stop the server
		notDone = false;
		try
		{
			//the new blank client. Just use the server itself
			new Socket("localhost", port);
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}

	private synchronized void broadcast(String message)
	{
		System.out.println(message);

		for (int i = clients.size(); --i >= 0;)
		{
			ClientThread clientThread = clients.get(i);

			//write message to all connected clients if the message write fails remove the client from the client thread list
			if (!clientThread.writeMessage(message))
			{
				clients.remove(i);
				System.out.println("Server disconnected client");
			}
		}
	}

	synchronized void remove(int clientId)
	{
		for (int i = 0; i < clients.size(); ++i)
		{
			ClientThread clientThread = clients.get(i);
			if (clientThread.id == clientId)
			{
				clients.remove(i);
				return;
			}
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Please enter the port number: ");
		port = Integer.parseInt(scan.nextLine());

		Server server = new Server();
		server.start();
	}

	class ClientThread extends Thread
	{
		Socket socket;
		DataInputStream input;
		DataOutputStream output;
		int id;
		String username;

		ClientThread(Socket socket)
		{			
			this.socket = socket;
			//System.out.println("Thread trying to create Object Input/Output streams");
			try
			{
				output = new DataOutputStream(socket.getOutputStream());
				input = new DataInputStream(socket.getInputStream());

				username = (String) input.readUTF();
				writeMessage("Clients on the server (" + clients.size() + "):");
				for (int i = 0; i < clients.size(); i++)
				{
					writeMessage(clients.get(i).username);
				}
				System.out.println(username + " just connected to the chat server");
			}
			catch (IOException ex)
			{
				System.out.println("Error: " + ex);
			}
		}

		public void run()
		{
			boolean notDone = true;
			String message;
			while (notDone)
			{
				try 
				{
					message = (String) input.readUTF();
				}
				catch (IOException ex)
				{
					System.out.println(username + " Could not read the input stream error: " + ex);
					break;
				}

				if (message.equalsIgnoreCase("logout"))
				{
					System.out.println(username + " has disconnected from the server.");
					broadcast(username + " has disconnected");
					notDone = false;
				}
				else
				{
					broadcast(username + ": " + message);
				}
			}
			remove(id);
			close();
		}

		private void close()
		{
			try
			{
				if (output != null)
					output.close();
			}
			catch (Exception e){}

			try
			{
				if (input != null)
				{
					input.close();
				}
			}
			catch (Exception e){}

			try
			{
				if (socket != null)
					socket.close();
			}
			catch (Exception e){}
		}

		private boolean writeMessage(String message)
		{
			if (!socket.isConnected())
			{
				close();
				return false;
			}

			try
			{
				output.writeUTF(message);
				output.flush();
			}
			catch (IOException ioe)
			{
				System.out.println("Error sendieng message\n" + ioe.toString());
			}
			return true;
		}
	}
}