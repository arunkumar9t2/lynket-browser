package arun.com.chromer.db;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Arun on 21/02/2016.
 */
@SuppressWarnings("ALL")
public class BlacklistedApps extends SugarRecord {
    @Unique
    String packageName;

    public BlacklistedApps() {

    }

    public BlacklistedApps(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
