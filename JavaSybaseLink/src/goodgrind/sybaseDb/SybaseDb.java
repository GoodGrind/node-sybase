package goodgrind.sybaseDb;

import goodgrind.SQLRequest;

/**
 *
 * @author rod
 */
public interface SybaseDb {
    boolean connect();
    void execSQL(SQLRequest request);
}
