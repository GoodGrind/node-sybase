package goodgrind;

import goodgrind.sybaseDb.SybaseDb;

public class SQLRequestListenerImpl implements SQLRequestListener {
    private final SybaseDb dbConnection;

    public SQLRequestListenerImpl(SybaseDb dbConnection) {
        this.dbConnection = dbConnection;
    }
    @Override
    public void sqlRequest(SQLRequest request) {
        dbConnection.execSQL(request);
    }
}
