package goodgrind;

/*
 * The idea is to recive json messages in containing
 * { "msgId" : 1, "sql" : "select * from blar"}   on standard in.
 *
 * Then on standard out send
 * { "msgId" : 1, "rows" : [{},{}]}  back on standard out where the msgId matches the sent message.
 */

import goodgrind.sybaseDb.SybaseDb;
import goodgrind.sybaseDb.SybaseDbFactory;

public class Main {

	public static void main(String[] args) {
		if (args.length != 1 && args.length != 5 && args.length != 4)
		{
			System.err.println("Expecting the arguments: odbc_name(1) or host,port,dbname,username,password(4,5)");
			System.exit(1);
		}

		SybaseDb dbConnection = createConnection(args);
		if (!dbConnection.connect()) {
			System.exit(1);
		}

		// send the connected message.
		System.out.println("connected");

		SQLRequestListenerImpl listener = new SQLRequestListenerImpl(dbConnection);
		StdInputReader input = new StdInputReader(listener);
		input.startReadLoop();
	}

	private static SybaseDb createConnection(String[] args) {
		SybaseDbFactory dbFactory = new SybaseDbFactory();
		if (args.length == 1) {
			return dbFactory.createSybaseDbConnectionWithODBC(args[0]);
		}
		String password = args.length == 5 ? args[4] : "";
		return dbFactory.createSybaseDbConnectionWithCredentials(args[0], Integer.parseInt(args[1]), args[2], args[3], password);
	}
}
