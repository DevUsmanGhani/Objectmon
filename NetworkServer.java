import java.io.IOException;
import java.nio.channels.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import edu.uab.cs203.Team;
import edu.uab.cs203.attacks.AbstractAttack;
import edu.uab.cs203.effects.StatusEffect;
import edu.uab.cs203.lab09.Hashmon;
import edu.uab.cs203.network.GymClient;
import edu.uab.cs203.network.GymServer;
import edu.uab.cs203.network.NetworkGym;

public class NetworkServer extends UnicastRemoteObject implements GymServer, NetworkGym {

	private ArrayList<GymClient> clients;
	private Team<Hashmon> teamA;
	private Team<Hashmon> teamB;
	private boolean teamAReady;
	private boolean teamBReady;

	protected NetworkServer() throws RemoteException {
		this.clients = new ArrayList<>();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String networkToString() throws RemoteException {
		return null;
	}

	public void printMessage(String message) throws RemoteException {
		System.out.println(message);
	}

	public void registerClientA(String host, int port, String registryName)
			throws RemoteException {
		System.out.println("Registering client: " + host + ":" + port + ":"
				+ registryName);
		try {
			GymClient client;
			client = (GymClient) LocateRegistry.getRegistry(host, port).lookup(
					registryName);
			this.clients.add(client);
			client.printMessage("You have connected; you have been designated as Client A/Team A.");
			setTeamA(client.getTeam());
			client.printMessage("Are you ready? Type Y for ready:");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public void registerClientB(String host, int port, String registryName)
			throws RemoteException {
		System.out.println("Registering client: " + host + ":" + port + ":"
				+ registryName);
		try {
			GymClient client;
			client = (GymClient) LocateRegistry.getRegistry(host, port).lookup(
					registryName);
			this.clients.add(client);
			client.printMessage("You have connected; you have been designated as Client B/Team B.");
			setTeamB(client.getTeam());
			client.printMessage("Are you ready? Type Y for ready:");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void setTeamA(@SuppressWarnings("rawtypes") Team team) throws RemoteException {
		teamA = team;
	}

	public void setTeamAReady(boolean b) throws RemoteException {
		this.teamAReady = b;
		if (this.teamAReady && this.teamBReady) {
			fight(270);
		} else {
			getClientA().printMessage("Waiting for Client B");
			getClientB().printMessage("Waiting for Client B");
		}
	}

	@SuppressWarnings("unchecked")
	public void setTeamB(@SuppressWarnings("rawtypes") Team team) throws RemoteException {
		teamB = team;
	}

	public void setTeamBReady(boolean b) throws RemoteException {
		this.teamBReady = b;
		if (this.teamAReady && this.teamBReady) {
			fight(270);
		} else {
			getClientA().printMessage("Waiting for Client A");
			getClientB().printMessage("Waiting for Client A");
		}
	}

	public void broadcastMessage(String message) {
		try {
			this.printMessage(message);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		for (GymClient c : this.clients) {
			try {
				c.printMessage(message);
			} catch (RemoteException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	
	public void executeTurn() {
		try {
			getClientA().networkTick();
			getClientB().networkTick();
			Hashmon A = (Hashmon) getClientA().nextObjectmon();
			Hashmon B = (Hashmon) getClientB().nextObjectmon();
			AbstractAttack attackA = getClientA().nextAttack(A);
			if (attackA != null) {
				if (getClientA().nextAttack(A).getStatusEffect(B) != null) {
					StatusEffect effectA = attackA.getStatusEffect(B);
					B.addStatusEffect(effectA);
					getClientB().addStatusEffectFromAttack(attackA,
							getClientB().nextObjectmon());
				} else {
					B = (Hashmon) getClientB().networkApplyDamage(A,
							getClientB().nextObjectmon(), attackA.getDamage(B));
				}
			}

			AbstractAttack attackB = getClientB().nextAttack(B);
			if (!B.isFainted() && attackB != null) {
				if (attackB.getStatusEffect(A) != null) {
					StatusEffect effectB = attackB.getStatusEffect(A);
					A.addStatusEffect(effectB);
					getClientA().addStatusEffectFromAttack(attackB,
							getClientA().nextObjectmon());
				} else {
					A = (Hashmon) getClientA().networkApplyDamage(B,
							getClientA().nextObjectmon(), attackB.getDamage(A));
					}
			}
			broadcastMessage(A.toString());
			broadcastMessage(B.toString());
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void fight(int arg0) {
		int i = 0;
		try {
			while (getClientA().nextObjectmon() != null && getClientB().nextObjectmon() != null && i < arg0) {
				i++;
				broadcastMessage("Round " + i);
				executeTurn();
			}
			if (getClientA().nextObjectmon() == null
					&& getClientB().nextObjectmon() == null) {
				broadcastMessage("\n\nIt's a draw!");
			} else if (getClientB().nextObjectmon() == null) {
				broadcastMessage("\n\nTeam A wins!");
			} else if (getClientA().nextObjectmon() == null) {
				broadcastMessage("\n\nClient B/Team B wins!");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args)
			throws java.rmi.AlreadyBoundException {
		int port = 10001;

		try {
			NetworkServer server = new NetworkServer();
			Runtime.getRuntime().exec("rmiregistry " + port);
			Registry registry = LocateRegistry.createRegistry(port);
			registry.bind("NetworkServer", server);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	public GymClient getClientA() {
		return clients.get(0);
	}

	public GymClient getClientB() {
		return clients.get(1);
	}

	@SuppressWarnings("rawtypes")
	public Team getTeamA() {
		return teamA;
	}

	@SuppressWarnings("rawtypes")
	public Team getTeamB() {
		return teamB;
	}

}
