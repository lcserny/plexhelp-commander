package net.cserny;

import org.springframework.test.context.ActiveProfiles;

import static net.cserny.BaseIntegrationTest.TestConfig.JIMFS;

@ActiveProfiles(JIMFS)
public abstract class IntegrationTest extends BaseIntegrationTest {
}
