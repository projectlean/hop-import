package org.apache.hop.imports;

import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.database.DatabasePluginType;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.api.IHopMetadataSerializer;
import org.apache.hop.metadata.serializer.json.JsonMetadataProvider;
import org.apache.hop.projects.config.ProjectsConfig;
import org.apache.hop.projects.config.ProjectsConfigSingleton;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.hop.projects.project.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class HopImport {

    private static IHopMetadataProvider metadataProvider;
    private static String inputFolderName, outputFolderName;
    public PluginRegistry registry;
    public IHopMetadataSerializer<DatabaseMeta> databaseSerializer;

    public File inputFolder, outputFolder;
    public LogChannel log;

    public HopImport(){
        try {
            HopEnvironment.init();
            metadataProvider = new JsonMetadataProvider();

            log = new LogChannel( "hop-config" );

            Project project = new Project();
            ProjectConfig projectConfig = new ProjectConfig("DCN Auto", inputFolderName, "project-config.json");
            ProjectsConfig projectsConfig = ProjectsConfigSingleton.getConfig();
            projectsConfig.addProjectConfig(projectConfig);

            registry = PluginRegistry.getInstance();
            databaseSerializer = metadataProvider.getSerializer(DatabaseMeta.class);
            DatabasePluginType dbpt = DatabasePluginType.getInstance();
        } catch (HopException e) {
            e.printStackTrace();
        }

    }

    public HopImport(String inputFolderName, String outputFolderName){
        this();
        setInputFolder(inputFolderName);
        setOutputFolder(outputFolderName);
    }

    public String getInputFolder() {
        return inputFolderName;
    }

    public void setInputFolder(String inputFolderName) {
        this.inputFolderName = inputFolderName;
        inputFolder = new File(inputFolderName);
        if(!inputFolder.exists() || !inputFolder.isDirectory()){
            log.logBasic("input folder '" + inputFolderName + "' doesn't exist or is not a folder.");
//            System.exit(1);
        }
    }

    public String getOutputFolder() {
        return outputFolderName;
    }

    public void setOutputFolder(String outputFolderName) {
        this.outputFolderName = outputFolderName;
        outputFolder = new File(outputFolderName);

        if(!outputFolder.exists() || !outputFolder.isDirectory()){
            log.logBasic("output folder '" + outputFolderName + "' doesn't exist or is not a folder.");
            outputFolder.mkdir();
//            System.exit(1);
        }

        if(outputFolder.listFiles().length > 0){
            try{
                Files.walk(outputFolder.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }catch(IOException e){
                log.logError(outputFolderName + " could not be cleared");
                e.printStackTrace();
            }
        }
    }

}
