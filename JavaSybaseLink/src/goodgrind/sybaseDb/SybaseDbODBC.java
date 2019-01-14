package goodgrind.sybaseDb;

import goodgrind.ExecSQLCallable;
import goodgrind.SQLRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DateFormat;
import java.util.concurrent.ExecutorService;

public class SybaseDbODBC implements SybaseDb {
    private final ExecutorService executorService;
    private final DateFormat dateFormat;
    private final String odbcName;
    private Connection conn;

    public SybaseDbODBC(String odbcName, ExecutorService executorService, DateFormat dateFormat) {
        this.odbcName = odbcName;
        this.executorService = executorService;
        this.dateFormat = dateFormat;
    }
    @Override
    public boolean connect() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
            conn = DriverManager.getConnection("jdbc:odbc:" + odbcName);
            return true;

        } catch (Exception ex) {
            System.err.println(ex);
            System.err.println(ex.getMessage());
            return false;
        }
    }

    @Override
    public void execSQL(SQLRequest request) {
        this.executorService.submit(new ExecSQLCallable(conn, this.dateFormat, request));
    }
}
