import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ElectionFileManagerTest {
    private ElectionFileManager electionFileManager;

    @BeforeEach
    void setUp() {
        electionFileManager = new ElectionFileManager(new ElectionProcessor());
    }

    @Test
    void testAddValidFile() throws IOException {
        File validFile = new File("src/test/TestData/ValidPO");
        electionFileManager.addFile(validFile);
        assertEquals(1, electionFileManager.getSelectedFilesList().size());
    }

    @Test
    void testAddInvalidFile() {
        File invalidFile = new File("src/test/TestData/invalid");
        assertThrows(IOException.class, () -> electionFileManager.addFile(invalidFile));
    }

    @Test
    void testAddFilesWithDifferentElectionTypes() throws IOException {
        File validPOFile = new File("src/test/TestData/ValidCPL");
        File validIRFile = new File("src/test/TestData/ValidPO");
        electionFileManager.addFile(validPOFile);
        assertThrows(IOException.class, () -> electionFileManager.addFile(validIRFile));
    }

    @Test
    void testGetFileElectionType() {
        File validFile = new File("src/test/TestData/CPLTest.csv");
        assertEquals("CPL", ElectionFileManager.getFileElectionType(validFile));
    }
}
