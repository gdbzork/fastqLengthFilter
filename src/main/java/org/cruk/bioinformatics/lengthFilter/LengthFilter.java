package org.cruk.bioinformatics.lengthFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.fastq.FastqWriter;
import htsjdk.samtools.fastq.FastqWriterFactory;

public class LengthFilter {
  protected Logger log = LogManager.getLogger(LengthFilter.class);
  protected Options options = new Options();
  protected File goodFN1;
  protected File shortFN1;
  protected File longFN1;
  protected File goodFN2;
  protected File shortFN2;
  protected File longFN2;
  protected FastqWriter goodFQ1;
  protected FastqWriter shortFQ1;
  protected FastqWriter longFQ1;
  protected FastqWriter goodFQ2;
  protected FastqWriter shortFQ2;
  protected FastqWriter longFQ2;
  protected int minLength = 17;
  protected int maxLength = 36;

  /////////////////////////////
  //
  // Simple tuple for returning counts
  //
  public class CountsTuple {
    int shorter;
    int good;
    int longer;

    public CountsTuple(int s, int g, int l) {
      shorter = s;
      good = g;
      longer = l;
    }
  }

  /////////////////////////////
  //
  // Initial setup
  //
  public LengthFilter() {
    configureOptions();
  }

  protected void configureOptions() {
    Option o;
    o = new Option(null, "goodF1", true, "length-filtered output Fastq (r1)");
    o.setType(File.class);
    options.addOption(o);
    o = new Option(null, "shortF1", true, "too-short output Fastq (r1)");
    o.setType(File.class);
    options.addOption(o);
    o = new Option(null, "longF1", true, "too-long output Fastq (r1)");
    o.setType(File.class);
    options.addOption(o);
    o = new Option(null, "goodF2", true, "length-filtered output Fastq (r2)");
    o.setType(File.class);
    options.addOption(o);
    o = new Option(null, "shortF2", true, "too-short output Fastq (r2)");
    o.setType(File.class);
    options.addOption(o);
    o = new Option(null, "longF2", true, "too-long output Fastq (r2)");
    o.setType(File.class);
    options.addOption(o);
    o = new Option(null, "minLength", true, "minimum length for a candidate sequence (default 17)");
    o.setType(File.class);
    options.addOption(o);
    o = new Option(null, "maxLength", true, "maximum length for a candidate sequence (default 36)");
    o.setType(File.class);
    options.addOption(o);
  }

  ///////////////////////////
  //
  // Main Processing
  //
  public CountsTuple scanFile(FastqReader rdr1, FastqReader rdr2, int minLen, int maxLen) {
    // copy rdr to good, possibly also saving too-short and too-long reads,
    // and counting all three categories
    int shorter = 0;
    int good = 0;
    int longer = 0;

    if (rdr2 == null) {
      for (FastqRecord fx1 : rdr1) {
        int slen1 = fx1.getReadLength();
        if (slen1 < minLen) {
          if (shortFQ1 != null)
            shortFQ1.write(fx1);
          shorter++;
        } else if (slen1 <= maxLen) {
          if (goodFQ1 != null)
            goodFQ1.write(fx1);
          good++;
        } else {
          if (longFQ1 != null)
            longFQ1.write(fx1);
          longer++;
        }
      }
    } else {
      FastqRecord fx2;
      for (FastqRecord fx1 : rdr1) {
        fx2 = rdr2.next();
        int slen1 = fx1.getReadLength();
        int slen2 = fx2.getReadLength();
        if (slen1 < minLen || slen2 < minLen) {
          if (shortFQ1 != null)
            shortFQ1.write(fx1);
          if (shortFQ2 != null)
            shortFQ2.write(fx2);
          shorter++;
        } else if (slen1 <= maxLen || slen2 <= maxLen) {
          if (goodFQ1 != null)
            goodFQ1.write(fx1);
          if (goodFQ2 != null)
            goodFQ2.write(fx2);
          good++;
        } else {
          if (longFQ1 != null)
            longFQ1.write(fx1);
          if (longFQ2 != null)
            longFQ2.write(fx2);
          longer++;
        }
      }
    }

    return new CountsTuple(shorter, good, longer);
  }

  public CountsTuple filterFile(File inputFN1, File inputFN2, int minLen, int maxLen) throws IOException {
    FastqReader fqr1;
    FastqReader fqr2 = null;
    goodFQ1 = null;
    shortFQ1 = null;
    longFQ1 = null;
    goodFQ2 = null;
    shortFQ2 = null;
    longFQ2 = null;

    //
    // open files
    //
    fqr1 = new FastqReader(inputFN1);
    if (inputFN2 != null) {
      fqr2 = new FastqReader(inputFN2);
    }
    FastqWriterFactory fwf = new FastqWriterFactory();
    if (goodFN1 != null) {
      goodFQ1 = fwf.newWriter(goodFN1);
    }
    if (shortFN1 != null) {
      shortFQ1 = fwf.newWriter(shortFN1);
    }
    if (longFN1 != null) {
      longFQ1 = fwf.newWriter(longFN1);
    }
    if (goodFN2 != null) {
      goodFQ2 = fwf.newWriter(goodFN2);
    }
    if (shortFN2 != null) {
      shortFQ2 = fwf.newWriter(shortFN2);
    }
    if (longFN2 != null) {
      longFQ2 = fwf.newWriter(longFN2);
    }

    //
    // pass fds to loop func
    //
    CountsTuple counts = scanFile(fqr1, fqr2, minLen, maxLen);

    //
    // write counts to summary file and tidy up
    //
    System.out.println(inputFN1.toString() + "\t" + counts.shorter + "\t" + counts.good + "\t" + counts.longer);
    fqr1.close();
    if (fqr2 != null)
      fqr2.close();
    if (goodFQ1 != null)
      goodFQ1.close();
    if (shortFQ1 != null)
      shortFQ1.close();
    if (longFQ1 != null)
      longFQ1.close();
    if (goodFQ2 != null)
      goodFQ2.close();
    if (shortFQ2 != null)
      shortFQ2.close();
    if (longFQ2 != null)
      longFQ2.close();
    return counts;
  }

  //////////////////////
  //
  // Parse and check parameters, invoke primary processing function
  //
  public int run(String[] args) throws ParseException, FileNotFoundException, IOException {
    log.debug("Starting filterByLength...");
    try {

      //
      // Parse the command line, check for at least one input file
      //
      CommandLine cli = new DefaultParser().parse(options, args);
      List<String> inputs = cli.getArgList();
      if (inputs.size() < 1 || inputs.size() > 2) {
        log.error("Expecting one or two input arguments; got {}.", inputs.size());
        System.exit(-1);
      }

      //
      // get the options, set their corresponding instance variables
      //
      goodFN1 = (File) cli.getParsedOptionValue("goodF1");
      shortFN1 = (File) cli.getParsedOptionValue("shortF1");
      longFN1 = (File) cli.getParsedOptionValue("longF1");
      goodFN2 = (File) cli.getParsedOptionValue("goodF2");
      shortFN2 = (File) cli.getParsedOptionValue("shortF2");
      longFN2 = (File) cli.getParsedOptionValue("longF2");
      if (cli.hasOption("minLength")) {
        minLength = Integer.parseInt(cli.getOptionValue("minLength"));
      }
      if (cli.hasOption("maxLength")) {
        maxLength = Integer.parseInt(cli.getOptionValue("maxLength"));
      }

      //
      // Read the files, counting and (possibly) saving short, good, and long reads
      //
      File inputFQ1 = new File(inputs.get(0));
      log.info("Processing file " + inputFQ1.getName());
      File inputFQ2 = null;
      if (inputs.size() > 1) {
        inputFQ2 = new File(inputs.get(1));
        log.info("Processing file " + inputFQ2.getName());
      }

      CountsTuple ct = filterFile(inputFQ1, inputFQ2, minLength, maxLength);

      if (inputs.size() != 1) {
        System.out.println(ct.shorter + "\t" + ct.good + "\t" + ct.longer);
      }
    } catch (ParseException pe) {
      log.error("Error with command line options: " + pe.getMessage());
      log.error("Command line: " + String.join(" ", args));
      throw pe;
    } catch (NumberFormatException nfe) {
      log.error("Command line: minLength or maxLength is not a number: " + nfe.getMessage());
      log.error("Command line: " + String.join(" ", args));
      throw nfe;
    }
    log.debug("Finishing filterByLength...");
    return 0;
  }

  ////////////////////////
  //
  // Where it all begins...
  //
  public static void main(String[] args) throws ParseException, FileNotFoundException, IOException {
    int exitCode;
    LengthFilter ftr = new LengthFilter();
    exitCode = ftr.run(args);
    System.exit(exitCode);
  }
}
