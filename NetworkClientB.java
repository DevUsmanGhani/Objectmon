import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import edu.uab.cs203.Objectmon;
import edu.uab.cs203.Team;
import edu.uab.cs203.effects.StatusEffect;
import edu.uab.cs203.lab05.BasicTeam;
import edu.uab.cs203.lab09.Hashmon;
import edu.uab.cs203.network.GymClient;
import edu.uab.cs203.network.GymServer;

public class NetworkClientB extends UnicastRemoteObject implements GymClient {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Team<Objectmon> team;
	private static Scanner scan;

	protected NetworkClientB() throws RemoteException {
		this.team = new BasicTeam<>();
	}

	public Team<Objectmon> getTeam() throws RemoteException {
		return team;
	}

	public Objectmon networkApplyDamage(Objectmon from, Objectmon to, int damage)
			throws RemoteException {
		for (Objectmon obj : getTeam()) {
			if (to.equals(obj)) {
				obj.setHp(obj.getHp() - damage);
				return obj;
			}
		}
		return null;
	}

	public void networkTick() throws RemoteException {
		Team<Objectmon> team = getTeam();
		for (Objectmon item : team) {
			item.tick();
		}

	}

	public Objectmon nextObjectmon() throws RemoteException {
		for (Objectmon item : team) {
			if (!item.isFainted()) {
				return item;
			}
		}
		return null;
	}

	public void printMessage(String arg0) throws RemoteException {
		System.out.println(arg0);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setTeam(Team team) throws RemoteException {
		this.team = team;

	}

	public static void main(String[] args) throws Exception {
		GymClient clientB = new NetworkClientB();
		GymServer server = new NetworkServer();

		String filePath = "objectdex_01.txt";
		Hashmon.loadObjectdex(filePath);

		Hashmon omon1 = new Hashmon("hawlucha");
		Hashmon omon2 = new Hashmon("cacturne");
		Hashmon omon3 = new Hashmon("carnivine");
		Team<Hashmon> teamB = new BasicTeam<>("Team B", 3);
		teamB.add(omon1);
		teamB.add(omon2);
		teamB.add(omon3);
		clientB.setTeam(teamB);
		try {

			Runtime.getRuntime().exec("rmiregistry 10003");
			Registry registry3 = LocateRegistry.createRegistry(10003);
			registry3.bind("GymClientB", clientB);

			Registry remoteRegistry = LocateRegistry.getRegistry("localhost",
					10001); // Client to Server
			server = (GymServer) remoteRegistry.lookup("NetworkServer");
			server.registerClientB("localhost", 10003, "GymClientB");

		} catch (Exception e) {
			e.printStackTrace();
		}
		scan = new Scanner(System.in);
		while (true) {
			String msg = scan.nextLine();
			if (msg.equals("y")) {
				server.setTeamBReady(true);
			} else {
				server.setTeamBReady(false);
			}
		}

	}

}
