package goodgrind.sybaseDb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SybaseDbFactory {
    private final int NUMBER_OF_THREADS = 5;
    private final DateFormat dateFormat;
    private final ExecutorService executorService;

    public SybaseDbFactory() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    public SybaseDb createSybaseDbConnectionWithCredentials(
            String host, int port, String dbName, String user, String password) {
        return new SybaseDbCredentials(
                host,
                port,
                dbName,
                user,
                password,
                executorService,
                dateFormat);
    }

    public SybaseDb createSybaseDbConnectionWithODBC(String odbcName) {
        return new SybaseDbODBC(odbcName,
                executorService,
                dateFormat);
    }
}
