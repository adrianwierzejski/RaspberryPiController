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
	public static final int sGetConfig = 1;
	public static final int sSaveConfig = 2;
	public static final int sEndConnection = 3;
	public static final int sClosedConnection = 4;
	public static final int sUnknownCommand = 5;
	private int command;
	private Socket socket;
	private String hostAddress;
	private int hostPort;
	private Object lock;
	private boolean pause;
	private String config;
	private ClientLoopListener mListener;

	Client(String address, int port){
		command=sGetConfig;
		hostAddress = address;
		this.hostPort = port;
		this.socket = null;
		this.lock = new Object();
		this.pause = false;
		this.mListener = null;
	}

    Client(){
		socket = new Socket();
		this.command=sGetConfig;
		this.hostAddress = null;
		this.hostPort = 0;
		this.socket = null;
		this.lock = new Object();
		this.mListener = null;
	}
	public void setClient(String address, int port){
    	synchronized (lock) {
    		pause=false;
			this.command = sGetConfig;
			this.hostAddress = address;
			this.hostPort = port;
		}
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
		setCommand(sSaveConfig);

	}
	public boolean isWaiting(){
		synchronized (lock){
			return pause;
		}
	}
	private void waitThread(){
		boolean result;
		synchronized (lock){
			pause = true;
			result = pause;
		}
		while(!isInterrupted()&&result) {
			try {
				sleep(5000);
				synchronized (lock){
					result=pause;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void closeConnection(){
			setCommand(sEndConnection);
	}
	public void run()
	{
		while (!isInterrupted()) {
			String response = "";
			try {
				socket =new Socket();
				socket.connect(new InetSocketAddress(hostAddress, hostPort), 60000);
				while (!isInterrupted()&&getCommand()!=sClosedConnection) {
					int command = getCommand();
					DataInputStream inputStream = new DataInputStream(
							socket.getInputStream());
					DataOutputStream outputStream = new DataOutputStream(
							socket.getOutputStream());
					if(command == sSaveConfig)
						synchronized (lock){
						outputStream.writeBytes(""+command+" "+config+'\0');}
					else
						outputStream.writeBytes(""+command + '\0');
					response = inputStream.readLine();
					int receivedCommand = sUnknownCommand;
					int tab[]=null;
					try {
						receivedCommand=Integer.parseInt(response.substring(0, 0));
						String responsetab[]=response.split(" ");
						tab = new int[responsetab.length-1];
						for (int i = 1; i < responsetab.length; i++) {
							try {
								tab[i-1] = Integer.parseInt(responsetab[i]);
							} catch (Exception ex) {
								Log.v(currentThread().getName(),"Błąd zamiany string na int wartość:"+responsetab[i]);
								tab[i] = 0;
							}
						}
					}catch(NumberFormatException e){
						e.printStackTrace();
						receivedCommand=sUnknownCommand;
					}
					if(receivedCommand!=command)
						Log.v(currentThread().getName(),"Serwer błędnie odpowiedział na żądanie: "+command+" odpowiedz sewrwera: "+receivedCommand+" pakiet: "+response);
					if(command==sEndConnection)
						setCommand(sClosedConnection);
					else if(getCommand()==command)
						setCommand(sGetData);
					if (mListener != null)
						mListener.onSomeEvent(receivedCommand,tab,response);
				}
			} catch(SocketTimeoutException e) {
				e.printStackTrace();
			} catch(UnknownHostException e){
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			} finally {
				setCommand(sClosedConnection);
				if (mListener != null)
					mListener.onSomeEvent(sClosedConnection, null, response);
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			waitThread();
		}
	}
}
