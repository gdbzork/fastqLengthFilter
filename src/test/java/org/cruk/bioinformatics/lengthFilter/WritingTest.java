package org.cruk.bioinformatics.lengthFilter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Test;

import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;

public class WritingTest {

  protected boolean checkFile(File fn, int expected, int minLen, int maxLen) {
    boolean okay = true;
    int count = 0;
    FastqReader rdr = new FastqReader(fn);

    for (FastqRecord rec : rdr) {
      int reclen = rec.getReadLength();
      count += 1;
      if (reclen < minLen || reclen > maxLen) {
        okay = false;
        break;
      }
    }
    rdr.close();
    if (count != expected) {
      okay = false;
    }
    return okay;
  }

  protected void cleanUpDir(Path p) {
    try {
      Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException ioe) {
      // we don't really care if this fails.
    }
  }

  @Test
  public void testWriteUncompressed() {
    File fn = new File("src/test/testData/test1000.fq");
    LengthFilter lf = new LengthFilter();
    Path targetDir = null;
    try {
      targetDir = Files.createTempDirectory("lengthFilter_");
      try {
        Path shorterFN = Files.createTempFile(targetDir, "shorter", ".fq");
        Path goodFN = Files.createTempFile(targetDir, "good", ".fq");
        Path longerFN = Files.createTempFile(targetDir, "longer", ".fq");
        lf.goodFN1 = goodFN.toFile();
        lf.shortFN1 = shorterFN.toFile();
        lf.longFN1 = longerFN.toFile();
        lf.filterFile(fn, null, 10, 32);
        assertTrue(checkFile(shorterFN.toFile(), 7, 0, 9));
        assertTrue(checkFile(goodFN.toFile(), 978, 10, 32));
        assertTrue(checkFile(longerFN.toFile(), 15, 33, 200));
      } catch (FileNotFoundException fnfe) {
        fail("Test file test1000.fq not found: " + fn.toString());
      } catch (IOException ioe) {
        fail("Test file test1000.fq: " + ioe.toString());
      }
    } catch (IOException ioe) {
      fail("Could not create temp directory: " + ioe.toString());
    } finally {
      cleanUpDir(targetDir);
    }
  }

  @Test
  public void testWriteCompressed() {
    File fn = new File("src/test/testData/test1000.fq.gz");
    LengthFilter lf = new LengthFilter();
    Path targetDir = null;
    try {
      targetDir = Files.createTempDirectory("lengthFilter_");
      try {
        Path shorterFN = Files.createTempFile(targetDir, "shorter", ".fq.gz");
        Path goodFN = Files.createTempFile(targetDir, "good", ".fq.gz");
        Path longerFN = Files.createTempFile(targetDir, "longer", ".fq.gz");
        lf.goodFN1 = goodFN.toFile();
        lf.shortFN1 = shorterFN.toFile();
        lf.longFN1 = longerFN.toFile();
        lf.filterFile(fn, null, 10, 32);
        assertTrue(checkFile(shorterFN.toFile(), 7, 0, 9));
        assertTrue(checkFile(goodFN.toFile(), 978, 10, 32));
        assertTrue(checkFile(longerFN.toFile(), 15, 33, 200));
      } catch (FileNotFoundException fnfe) {
        fail("Test file test1000.fq.gz not found: " + fn.toString());
      } catch (IOException ioe) {
        fail("Test file test1000.fq.gz: " + ioe.toString());
      }
    } catch (IOException ioe) {
      fail("Could not create temp directory: " + ioe.toString());
    } finally {
      cleanUpDir(targetDir);
    }
  }
}
