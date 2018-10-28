package org.cruk.bioinformatics.lengthFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class CommandParsingTest {

  @Test
  public void testSanity() {
    assertTrue(true);
    assertEquals(0, 0);
  }

  @Test
  public void testCommandLineSanity() {
    String fn = new String("src/test/testData/test1000.fq");
    String[] cl = { fn };
    LengthFilter lf = new LengthFilter();
    try {
      CommandLine cli = new DefaultParser().parse(lf.options, cl);
      assertEquals(null, cli.getOptionValue("goodF1"));
      assertEquals(null, cli.getOptionValue("shortF1"));
      assertEquals(null, cli.getOptionValue("longF1"));
      assertEquals(null, cli.getOptionValue("minLength"));
      assertEquals(null, cli.getOptionValue("maxLength"));
      assertEquals(17, lf.minLength);
      assertEquals(36, lf.maxLength);
    } catch (ParseException pe) {
      fail("Caught exception parsing empty list of arguments:" + pe.toString());
    }
  }

  @Test
  public void testSimpleCommandLine() {
    String[] cl = { "--minLength", "5", "--maxLength", "20", "--goodF1", "zork.fq.gz", "in.fq.gz" };
    LengthFilter lf = new LengthFilter();
    try {
      CommandLine cli = new DefaultParser().parse(lf.options, cl);
      assertEquals("zork.fq.gz", cli.getOptionValue("goodF1"));
      assertEquals(null, cli.getOptionValue("shortF1"));
      assertEquals(null, cli.getOptionValue("longF1"));
      assertEquals("5", cli.getOptionValue("minLength"));
      assertEquals("20", cli.getOptionValue("maxLength"));
    } catch (ParseException pe) {
      fail("Caught exception parsing simple list of arguments:" + pe.toString());
    }
  }

  @Test
  public void testEqualsCommandLine() {
    String[] cl = { "--minLength=5", "--maxLength=20", "--goodF1=zork.fq.gz", "in.fq.gz" };
    LengthFilter lf = new LengthFilter();
    try {
      CommandLine cli = new DefaultParser().parse(lf.options, cl);
      assertEquals("zork.fq.gz", cli.getOptionValue("goodF1"));
      assertEquals(null, cli.getOptionValue("shortF1"));
      assertEquals(null, cli.getOptionValue("longF1"));
      assertEquals("5", cli.getOptionValue("minLength"));
      assertEquals("20", cli.getOptionValue("maxLength"));
    } catch (ParseException pe) {
      fail("Caught exception parsing equals list of arguments:" + pe.toString());
    }
  }

  @Test
  public void testNoInputs() {
    String[] cl = { "--minLength=5", "--maxLength=20", "src/test/testData/test1000.fq" };
    LengthFilter lf = new LengthFilter();
    try {
      int rc = lf.run(cl);
      assertEquals(0, rc);
    } catch (ParseException pe) {
      fail("Caught exception parsing simple list of arguments:" + pe.toString());
    } catch (FileNotFoundException fnfe) {
      fail("Caught FileNotFound Exception testing 'no inputs': " + fnfe.toString());
    } catch (IOException ioe) {
      fail("Caught IO Exception testing 'no inputs': " + ioe.toString());
    }
  }
}
