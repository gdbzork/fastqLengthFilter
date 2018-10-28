package org.cruk.bioinformatics.lengthFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class ReadingTest {

  @Test
  public void testReadUncompressed() {
    File fn = new File("src/test/testData/test1000.fq");
    LengthFilter lf = new LengthFilter();
    try {
      LengthFilter.CountsTuple ct = lf.filterFile(fn, null, 10, 32);
      assertEquals(7, ct.shorter);
      assertEquals(978, ct.good);
      assertEquals(15, ct.longer);
    } catch (FileNotFoundException fnfe) {
      fail("Test file test1000.fq not found: " + fn.toString());
    } catch (IOException ioe) {
      fail("Test file test1000.fq: " + ioe.toString());
    }
  }

  @Test
  public void testReadCompressed() {
    File fn = new File("src/test/testData/test1000.fq.gz");
    LengthFilter lf = new LengthFilter();
    try {
      LengthFilter.CountsTuple ct = lf.filterFile(fn, null, 10, 32);
      assertEquals(7, ct.shorter);
      assertEquals(978, ct.good);
      assertEquals(15, ct.longer);
    } catch (FileNotFoundException fnfe) {
      fail("Test file test1000.fq.gz not found: " + fn.toString());
    } catch (IOException ioe) {
      fail("Test file test1000.fq.gz: " + ioe.toString());
    }
  }
}
