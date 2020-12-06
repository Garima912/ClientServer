package helpers;

import controllers.ClientController;
import javafx.application.Platform;
import model.ClientPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;



public class Client extends Thread {

	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private Consumer<Serializable> callback;
	private Consumer<Serializable> callback2;
	ClientPacket packet;
	ClientController clientController;

	public Client(Consumer<Serializable> call, Consumer<Serializable> call2, ClientController clientController){
		callback = call;
		callback2 = call2;
		this.clientController = clientController;
	}

	@Override
	public void run() {
		
		try {
		socketClient= new Socket("127.0.0.1",5555);
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {}
		
		while(true) {
			 
			try {
				packet = (ClientPacket) in.readObject();
				callback.accept(packet.getMessage());
				System.out.println("Online client ::::" + packet.getClientIds());
				refreshLists();

				for(int x: packet.getClientIds()){
					callback2.accept(x);
				}
			}
			catch(Exception e) {}
		}
    }

    // refreshes all the GUI lists after a new packet is received with new clients
	private void refreshLists() {
		Platform.runLater(
				() -> {
					clientController.onlineClientsList.getItems().clear();
					clientController.recipientChoices.clear();
					clientController.recipientsBox.getItems().clear();
					clientController.recipientsBox.getItems().add("All");
				}
		);
	}

	public void send(String data, ArrayList<Integer> sendTo) {
		if(sendTo.equals(clientController.recipientChoices) || sendTo.contains("All") ){
			packet.setSendToAll(true);
		}
		try {
			packet.setMessage(data);
			out.writeObject(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
