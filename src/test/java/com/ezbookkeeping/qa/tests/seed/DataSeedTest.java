package com.ezbookkeeping.qa.tests.seed;

import com.ezbookkeeping.qa.fixtures.DataSeeder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public class DataSeedTest {

    @Test
    void deveSeedearBaseDeTeste() {
        DataSeeder.seed();
    }
}