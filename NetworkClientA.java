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

public class NetworkClientA extends UnicastRemoteObject implements GymClient {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Team<Objectmon> team;
	private static Scanner scan;

	protected NetworkClientA() throws RemoteException {
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


//
//	public Objectmon nextObjectmon() throws RemoteException {
//		Team<Objectmon> currentteam = getTeam();
//		for (int i = 0; i < currentteam.size(); i++) {
//			Objectmon CurrentObj = currentteam.get(i);
//			if (!CurrentObj.isFainted()) {
//				if (CurrentObj.getStatusEffects() != null) {
//					if (preventcounter(CurrentObj) == 0)
//						return CurrentObj;
//				} else if (CurrentObj.getStatusEffects() == null)
//					return CurrentObj;
//			}
//		}
//		return null;
//	}
//
//	public int preventcounter(Objectmon obj) throws RemoteException {
//		int counter = 0;
//		for (StatusEffect effect : obj.getStatusEffects()) {
//			if (effect.preventAttack()) {
//				counter++;
//			}
//		}
//		return counter;
//	}

	public void printMessage(String arg0) throws RemoteException {
		System.out.println(arg0);

	}

	@SuppressWarnings("unchecked")
	public void setTeam(@SuppressWarnings("rawtypes") Team team) throws RemoteException {
		this.team = team;

	}

	public static void main(String[] args) throws Exception {
		GymClient clientA = new NetworkClientA();
		GymServer server = new NetworkServer();

		String filePath = "objectdex_01.txt";
		Hashmon.loadObjectdex(filePath);

		Hashmon omon1 = new Hashmon("beartic");
		Hashmon omon2 = new Hashmon("bulbasaur");
		Hashmon omon3 = new Hashmon("carbink");
		Team<Hashmon> teamB = new BasicTeam<>("Team B", 3);
		teamB.add(omon1);
		teamB.add(omon2);
		teamB.add(omon3);
		clientA.setTeam(teamB);
		try {

			Runtime.getRuntime().exec("rmiregistry 10002");
			Registry registry2 = LocateRegistry.createRegistry(10002); // Server to Client
			registry2.bind("GymClientA", clientA);

			Registry remoteRegistry = LocateRegistry.getRegistry("localhost", 10001); // Client to Server
			server = (GymServer) remoteRegistry.lookup("NetworkServer");
			server.registerClientA("localhost", 10002, "GymClientA");

		} catch (Exception e) {
			e.printStackTrace();
		}
		scan = new Scanner(System.in);
		while (true) {
			String msg = scan.nextLine();
			if (msg.equals("y")) {
				server.setTeamAReady(true);
			} else {
				server.setTeamAReady(false);
			}
		}
	}
}
