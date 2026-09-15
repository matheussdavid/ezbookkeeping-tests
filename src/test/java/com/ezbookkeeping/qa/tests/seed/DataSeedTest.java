package com.ezbookkeeping.qa.tests.seed;

import com.ezbookkeeping.qa.fixtures.DataSeeder;
import org.junit.jupiter.api.Test;

public class DataSeedTest {

    @Test
    void deveSeedearBaseDeTeste() {
        DataSeeder.seed();
    }
}