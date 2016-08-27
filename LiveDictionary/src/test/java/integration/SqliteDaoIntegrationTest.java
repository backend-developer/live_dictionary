package integration;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SqliteDaoIntegrationTest extends DaoIntegrationTest {
    public SqliteDaoIntegrationTest() {
        super(true);
    }
}
