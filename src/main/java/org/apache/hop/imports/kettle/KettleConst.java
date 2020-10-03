package org.apache.hop.imports.kettle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KettleConst {

    public static final Map<String, String> kettleElementReplacements = Stream.of(new Object[][]{
            // transformations
            {"transformation", "pipeline"},
            {"trans_type", "pipeline_type"},
            {"trans_version", "pipeline_version"},
            {"trans_status", "pipeline_status"},
            {"step", "transform"},
            {"step_error_handling", "transform_error_handling"},
            {"capture_step_performance","capture_transform_performance"},
            {"step_performance_capturing_size_limit", "transform_performance_capturing_size_limit"},
            {"step_performance_capturing_delay", "transform_performance_capturing_delay"},
            // jobs
            {"job", "workflow"},
            {"job_version", "workflow_version"},
            {"entries","actions"},
            {"entry","action"},
            {"job-log-table","workflow-log-table"},
            {"source_step", "source_transform"},
            {"target_step", "target_transform"}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (String)data[1]));

    public static final Map<String, String> kettleElementsToRemove = Stream.of(new Object[][]{
            {"size_rowset", ""},
            {"sleep_time_empty", ""},
            {"sleep_time_full",""},
            {"unique_connections",""},
            {"feedback_shown",""},
            {"feedback_size",""},
            {"using_thread_priorities",""},
            {"shared_objects_file",""},
            {"dependencies",""},
            {"partitionschemas",""},
            {"slaveservers",""},
            {"remotesteps",""},
            {"clusterschemas",""},
            {"maxdate",""},
            {"log", ""},
            {"connection", "pipeline"},
            {"slave-step-copy-partition-distribution", ""},
            {"slave_transformation", ""}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (String)data[1]));

    public static final Map<String, String> kettleReplaceContent = Stream.of(new Object[][]{
            {"JOB", "WORKFLOW"},
            {"TRANS", "PIPELINE"},
            {"BlockUntilStepsFinish", "BlockUntilTransformsFinish"}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (String)data[1]));

    public static final Map<String, String> kettleReplaceInContent = Stream.of(new Object[][]{
            {".kjb", ".hwf"},
            {".ktr", ".hpl"},
            {"Internal.Job", "Internal.Workflow"},
            {"Internal.Transformation", "Internal.Pipeline"},
            {"Filename.Directory", "Filename.Folder"},
            {"Repository.Directory", "Repository.Folder"},
            {"Current.Directory", "Current.Folder"}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (String)data[1]));

    public static final HashMap<String, String> replacements = new HashMap<String, String>();

    public KettleConst(){
        HashMap<String, String> kettleElementReplacements = new HashMap<String, String>();
        // ktr elements
        replacements.put("transformation", "pipeline");
        replacements.put("trans_type", "pipeline_type");
        replacements.put("trans_status", "pipeline_status");
        replacements.put("step", "transform");
        replacements.put("step_error_handling", "transform_error_handling");
        // kjb elements
        replacements.put("job", "workflow");
        replacements.put("job_version", "workflow_version");
        replacements.put("entries", "actions");
        replacements.put("entry", "action");
        replacements.put("job-log-table", "workflow-log-table");
    }
}