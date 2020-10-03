package org.apache.hop.imports.kettle;

import org.junit.Before;
import org.junit.Test;


public class KettleImportTest {

    private KettleImport kettleImporter;
    private String inputFolderName, outputFolderName;


    @Before
    public void setUp(){
        inputFolderName = "src/test/resources/kettle";
        outputFolderName = "target/hop-imported";

        kettleImporter = new KettleImport(inputFolderName, outputFolderName);
    }

    @Test
    public void migrateJobToWorkflow(){
        kettleImporter.importKettle();

        // verify the import was done correctly
    }

}
