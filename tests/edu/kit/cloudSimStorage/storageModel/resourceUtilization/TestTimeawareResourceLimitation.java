/*
 * Title:        StorageCloudSim
 * Description:  StorageCloudSim (Storage as a Service Cloud Simulation), an extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2013, Karlsruhe Institute of Technology, Germany
 * https://github.com/toebbel/StorageCloudSim
 * http://www.tobiassturm.de/projects/storagecloudsim.html
 */
package edu.kit.cloudSimStorage.storageModel.resourceUtilization;

import org.junit.Before;
import org.junit.Test;

import static edu.kit.cloudSimStorage.helper.FileSizeHelper.Magnitude.*;
import static edu.kit.cloudSimStorage.helper.FileSizeHelper.toBytes;
import static org.junit.Assert.assertEquals;

/** @author Tobias Sturm, 6/21/13 4:59 PM */
public class TestTimeawareResourceLimitation {
	private static final double DELTA = 0.00001;
	TimeawareResourceLimitation r;
	double maxRate;

	@Before
	public void setUp() throws Exception {

		//Harddrive that has a resource limitation of 100 MByte/s -> convert to ms^-1
		maxRate = toBytes(100, MEGA_BYTE) / 1000;
		r = new FirstFitAllocation(maxRate);
	}

	@Test
	public void testUseWithSingleOperation() throws Exception {
		//use the disk with an operation at max possible speed, with an amount of 10 MB
		UtilizationSequence seq = r.use(0, toBytes(10, MEGA_BYTE));

		//should take 100ms at constant speed
		assertEquals(100, seq.getDuration());
		assertEquals(1, seq.getNumSamples());

		//check values of single sample in sequence
		assertEquals(seq.getValuesAt(10), maxRate, 0.001);
		assertEquals(100, seq.getNextSamplePointFrom(10));
	}

	@Test
	public void testUseWithTwoOperations() throws Exception {
		//use the disk with an operation, with an amount of 10 MB, but only 25% of possible speed
		UtilizationSequence seq1 = r.use(0, toBytes(10, MEGA_BYTE), maxRate / 4.0);

		//should take 400ms at constant speed
		assertEquals(400, seq1.getDuration());
		assertEquals(1, seq1.getNumSamples());

		//use the disk with another operation wit max possible speed with amount of 1GB, and 100% of possible speed
		UtilizationSequence seq2 = r.use(200, toBytes(1, GIGA_BYTE), maxRate);
		assertEquals(2, seq2.getNumSamples());
		assertEquals(200, seq2.getNextSamplePointFrom(0));
		assertEquals(400, seq2.getNextSamplePointFrom(200));

		//200ms for 15MB + 10090ms for 1004MB = 10290ms
		assertEquals(10290, seq2.getDuration());
	}

	@Test
	public void testUseWithFourOperations() throws Exception {
		//Operation 1
		//use the disk with an operation, with an amount of 5 MB, but only 50% of possible speed
		UtilizationSequence seq1 = r.use(0, toBytes(5, MEGA_BYTE), maxRate / 2.0);
		//should take 100ms at constant speed
		assertEquals(100, seq1.getDuration());
		assertEquals(1, seq1.getNumSamples());

		//Operation 2
		//use the disk with an operation, with an amount of 5 MB, but only 50% of possible speed
		UtilizationSequence seq2 = r.use(300, toBytes(5, MEGA_BYTE), maxRate / 2.0);
		//should take 100ms at constant speed
		assertEquals(100, seq2.getDuration());
		assertEquals(1, seq2.getNumSamples());
		assertEquals(300, seq2.getFirstSamplePoint());
		assertEquals(400, seq2.getLastSamplePoint());

		//Operation 3
		//use the disk with an operation, with an amount of 5 MB, but only 50% of possible speed
		UtilizationSequence seq3 = r.use(50, toBytes(15, MEGA_BYTE), maxRate / 2.0);
		//should take 300ms at constant speed
		assertEquals(300, seq3.getDuration());
		assertEquals(3, seq3.getNumSamples());
		assertEquals(50, seq3.getFirstSamplePoint());
		assertEquals(350, seq3.getLastSamplePoint());
		assertEquals(maxRate / 2.0, seq3.getValuesAt(50), DELTA);
		assertEquals(maxRate / 2.0, seq3.getValuesAt(150), DELTA);
		assertEquals(maxRate / 2.0, seq3.getValuesAt(320), DELTA);
		assertEquals(100, seq3.getNextSamplePointFrom(50));
		assertEquals(300, seq3.getNextSamplePointFrom(100));
		assertEquals(350, seq3.getNextSamplePointFrom(300));

		//Operation 3
		//use the disk with an operation, with an amount of 25 MB with max possible speed
		UtilizationSequence seq4 = r.use(0, toBytes(25, MEGA_BYTE), maxRate);
		assertEquals(4, seq4.getNumSamples());
		assertEquals(500, seq4.getDuration());
		assertEquals(0, seq4.getFirstSamplePoint());
		assertEquals(50, seq4.getNextSamplePointFrom(0));
		assertEquals(100, seq4.getNextSamplePointFrom(50));
		assertEquals(300, seq4.getNextSamplePointFrom(100));
		assertEquals(350, seq4.getNextSamplePointFrom(300));
		assertEquals(400, seq4.getNextSamplePointFrom(350));
		assertEquals(500, seq4.getNextSamplePointFrom(400));
		assertEquals(maxRate / 2.0, seq4.getValuesAt(0), DELTA);
		assertEquals(0, seq4.getValuesAt(51), DELTA);
		assertEquals(maxRate / 2.0, seq4.getValuesAt(100), DELTA);
		assertEquals(0, seq4.getValuesAt(301), DELTA);
		assertEquals(maxRate / 2.0, seq4.getValuesAt(350), DELTA);
		assertEquals(maxRate, seq4.getValuesAt(401), DELTA);

	}
}
