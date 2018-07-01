package pl.wierzejski_adrian.wyswietlaczprasa;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client extends Thread {

	public static final int sGetData = 0;
	public static final int sReceiveConfig = 1;
	public static final int sSendConfig = 2;
	public static final int sEndConnection = 3;
	public static final int sClosedConnection = 4;
	private int command;
	private Socket socket;
	private String hostAddress;
	private int hostPort;
	private Object lock;
	private boolean zatrzymany;
	private String config;
	private ClientLoopListener mListener;

	Client(String address, int port){
		socket = new Socket();
		command=sReceiveConfig;
		hostAddress = address;
		this.hostPort = port;
		this.socket = null;
		this.lock = new Object();
		this.zatrzymany = false;
		this.mListener = null;
	}

    Client(){
		socket = new Socket();
		this.command=sReceiveConfig;
		this.hostAddress = null;
		this.hostPort = 0;
		this.socket = null;
		this.lock = new Object();
		this.zatrzymany = false;
		this.mListener = null;
	}
	public void setClient(String address, int port){
		this.hostAddress = address;
		this.hostPort = port;
	}
	public int getCommand(){
		int out;
		synchronized (lock){
			out=command;
		}
		return out;
	}
	private void setCommand(int cmd){
		synchronized (lock) {
			command = cmd;
		}
	}
	public void setClientListener(ClientLoopListener listener) {
		this.mListener = listener;
	}

	public interface ClientLoopListener {
		void onSomeEvent(final int command, final int[] data, final String response);
	}
	public void setConfig(int [] config){
		String result=""+config[0];
		for (int i=1; i<config.length;i++)
			result+=" "+config[i];
		synchronized (lock){
			this.config = result;
		}
		setCommand(sSendConfig);

	}
	public void closeConnection(){
			setCommand(sEndConnection);
	}
	public void run()
	{
		while (!isInterrupted()&&getCommand()!=sClosedConnection) {
			String response = "";
			try {
				socket.connect(new InetSocketAddress(hostAddress, hostPort), 60000);
				while (getCommand()!=sClosedConnection) {
					int command = getCommand();
					DataInputStream inputStream = new DataInputStream(
							socket.getInputStream());
					DataOutputStream outputStream = new DataOutputStream(
							socket.getOutputStream());
					if(command == sSendConfig)
						synchronized (lock){
						outputStream.writeBytes(""+command+" "+config+'\0');}
					else
						outputStream.writeBytes(""+command + '\0');
					response = inputStream.readLine();
					int receivedCommand = Integer.parseInt(response.substring(0,0));
					String responsetab[]=response.split(" ");
					int tab[] = new int[responsetab.length-1];
					for (int i = 1; i < responsetab.length; i++) {
						try {
							tab[i-1] = Integer.parseInt(responsetab[i]);
						} catch (Exception ex) {
							Log.v(currentThread().getName(),"Błąd zamiany string na int wartość:"+responsetab[i]);
							tab[i] = 0;
						}
					}
					if(receivedCommand!=command)
						Log.v(currentThread().getName(),"Serwer błędnie odpowiedział na żądanie: "+command+" odpowiedz sewrwera: "+receivedCommand+" pakiet: "+response);
					if(receivedCommand==sEndConnection)
						setCommand(sClosedConnection);
					else setCommand(sGetData);
					if (mListener != null)
						mListener.onSomeEvent(receivedCommand,tab,response);
				}
				socket.close();
			} catch (NumberFormatException e){
				e.printStackTrace();
			} catch(SocketTimeoutException e){
				e.printStackTrace();
				setCommand(sClosedConnection);
			} catch(UnknownHostException e){
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			} finally{
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
