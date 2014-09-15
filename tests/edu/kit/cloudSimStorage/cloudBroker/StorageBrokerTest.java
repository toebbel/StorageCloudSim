package edu.kit.cloudSimStorage.cloudBroker;

import junit.framework.TestCase;
import org.cloudbus.cloudsim.core.CloudSim;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

public class StorageBrokerTest extends TestCase {

    @Before
    public void setUp()
    {
        // Initialize the CloudSim library
        CloudSim.init(2, Calendar.getInstance(), false);
    }

    @Test
    public void testCtorWithMetaBroker()
    {
        StorageBroker broker = new StorageBroker(0, 0);
    }

    public void testCtorWithoutMetBroker()
    {
        StorageBroker broker = new StorageBroker(0);
    }

}