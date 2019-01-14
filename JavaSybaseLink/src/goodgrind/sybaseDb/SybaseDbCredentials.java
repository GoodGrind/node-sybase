package goodgrind.sybaseDb;

import java.sql.Connection;
import java.sql.DriverManager;
import goodgrind.ExecSQLCallable;
import goodgrind.SQLRequest;

import java.text.DateFormat;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class SybaseDbCredentials implements SybaseDb {

    private final ExecutorService executorService;
    private final DateFormat dateFormat;
    private final String host;
    private final Integer port;
    private final String dbName;
    private final Properties props;
    private Connection conn;

    public SybaseDbCredentials(String host, Integer port, String dbName, String userName, String password, ExecutorService executorService, DateFormat dateFormat) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.executorService = executorService;
        this.dateFormat = dateFormat;
        this.props = new Properties();
        this.props.put("user", userName);
        this.props.put("password", password);
    }

    @Override
    public boolean connect() {
        try {
            Class.forName("com.sybase.jdbc3.jdbc.SybDriver").newInstance();
            conn = DriverManager.getConnection("jdbc:sybase:Tds:" + host + ":" + port + "/" + dbName, props);
            return true;

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
    }

    @Override
    public void execSQL(SQLRequest request) {
        this.executorService.submit(new ExecSQLCallable(conn, dateFormat, request));
    }
}
