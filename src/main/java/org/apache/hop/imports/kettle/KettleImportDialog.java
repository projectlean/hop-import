package org.apache.hop.imports.kettle;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.extension.ExtensionPointHandler;
import org.apache.hop.core.util.SingletonUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.imports.HopDbConnImport;
import org.apache.hop.imports.HopVarImport;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import java.util.HashMap;
import java.util.List;

public class KettleImportDialog extends Dialog {
    private static final Class<?> PKG  = KettleImportDialog.class;

    private String returnValue;

    private IVariables variables;

    private Shell shell;
    private final PropsUi props;
    private int margin;
    private int middle;

    private KettleImport kettleImport;

    private TextVar wImportFrom, wImportPath, wKettleProps, wShared, wJdbcProps;
    private Combo wComboImportTo;
    private Button wcbimportInExisting, wbImportPath;

    private Control lastControl;

    private List<String> projectNames;

    public KettleImportDialog(Shell parent, IVariables variables, KettleImport kettleImport){
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        props = PropsUi.getInstance();

        this.variables = variables;
        this.kettleImport = kettleImport;

        try{
            projectNames = SingletonUtil.getValuesList("org.apache.hop.projects.gui.ProjectsGuiPlugin", "org.apache.hop.projects.config.ProjectsConfigSingleton",
                    "listProjectNames");
        }catch(HopException e){
            e.printStackTrace();
        }
    }

    public void open(){
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        shell.setImage(GuiResource.getInstance().getImageHopUi());
        props.setLook(shell);

        margin = Const.MARGIN + 2;
        middle = Const.MIDDLE_PCT;

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);
        shell.setText("Import code to Hop");


        // Buttons go at the bottom of the dialog
        //
        Button wOK = new Button( shell, SWT.PUSH );
        wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        wOK.addListener( SWT.Selection, event -> ok() );
        Button wCancel = new Button( shell, SWT.PUSH );
        wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
        wCancel.addListener( SWT.Selection, event -> cancel() );
        BaseTransformDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin * 3, lastControl );

        // Select folder to import from
        Label wlImportFrom = new Label( shell, SWT.RIGHT );
        props.setLook( wlImportFrom );
        wlImportFrom.setText( "Import From " );
        FormData fdlImportFrom = new FormData();
        fdlImportFrom.left = new FormAttachment( 0, 0 );
        fdlImportFrom.right = new FormAttachment( middle, 0 );
        fdlImportFrom.top = new FormAttachment( 0, margin );
        wlImportFrom.setLayoutData( fdlImportFrom );

        Button wbImportFrom = new Button(shell, SWT.PUSH);
        props.setLook( wbImportFrom );
        wbImportFrom.setText( "Browse..." );
        FormData fdbImportFrom = new FormData();
        fdbImportFrom.right = new FormAttachment(100, 0);
        fdbImportFrom.top = new FormAttachment(wlImportFrom, 0, SWT.CENTER);
        wbImportFrom.setLayoutData( fdbImportFrom );
        wbImportFrom.addListener( SWT.Selection, this::browseHomeFolder );

        wImportFrom = new TextVar( variables, shell, SWT.SINGLE | SWT.BORDER | SWT.LEFT );
        props.setLook(wImportFrom);
        FormData fdImportFrom = new FormData();
        fdImportFrom.left = new FormAttachment( middle, margin );
        fdImportFrom.right = new FormAttachment( wbImportFrom, -margin );
        fdImportFrom.top = new FormAttachment( wlImportFrom, 0, SWT.CENTER );
        wImportFrom.setLayoutData( fdImportFrom );
        lastControl = wImportFrom;

        // Import in existing project?
        Label wlImportInExisting = new Label(shell, SWT.RIGHT);
        props.setLook(wlImportInExisting);
        wlImportInExisting.setText("Import in existing project");
        FormData fdlImportInExisting = new FormData();
        fdlImportInExisting.left = new FormAttachment(0,0);
        fdlImportInExisting.right = new FormAttachment(middle, 0);
        fdlImportInExisting.top = new FormAttachment(lastControl, margin);
        wlImportInExisting.setLayoutData(fdlImportInExisting);

        wcbimportInExisting = new Button(shell, SWT.CHECK);
        wcbimportInExisting.setSelection(true);
        props.setLook(wcbimportInExisting);
        FormData fdcbImportInExisting = new FormData();
        fdcbImportInExisting.left = new FormAttachment(middle, margin);
        fdcbImportInExisting.right = new FormAttachment(100, 0);
        fdcbImportInExisting.top = new FormAttachment(wlImportInExisting, 0, SWT.CENTER);
        wcbimportInExisting.setLayoutData(fdcbImportInExisting);
        wcbimportInExisting.addListener(SWT.Selection, this::showHideProjectFields);
        lastControl = wlImportInExisting;

        // Import in project
        Label wlImportTo = new Label(shell, SWT.RIGHT);
        props.setLook(wlImportTo);
        wlImportTo.setText("Import in project ");
        FormData fdlImportTo = new FormData();
        fdlImportTo.left = new FormAttachment(0, 0);
        fdlImportTo.right = new FormAttachment(middle, 0);
        fdlImportTo.top = new FormAttachment(lastControl, margin);
        wlImportTo.setLayoutData(fdlImportTo);

        wComboImportTo = new Combo(shell, SWT.READ_ONLY);
        wComboImportTo.setItems(projectNames.toArray(new String[projectNames.size()]));
        props.setLook(wComboImportTo);
        FormData fdImportTo = new FormData();
        fdImportTo.left = new FormAttachment(middle, margin);
        fdImportTo.right = new FormAttachment(100, 0);
        fdImportTo.top = new FormAttachment(wlImportTo, 0, SWT.CENTER);
        wComboImportTo.setLayoutData(fdImportTo);
        lastControl = wlImportTo;

        // Import in path
        Label wlImportPath = new Label(shell, SWT.RIGHT);
        props.setLook(wlImportPath);
        wlImportPath.setText("Import to folder");
        FormData fdlImportPath = new FormData();
        fdlImportPath.left = new FormAttachment(0,0);
        fdlImportPath.right = new FormAttachment(middle, 0);
        fdlImportPath.top = new FormAttachment(lastControl, margin);
        wlImportPath.setLayoutData(fdlImportPath);

        wbImportPath = new Button(shell, SWT.PUSH);
        props.setLook(wbImportPath);
        wbImportPath.setText("Browse...");
        FormData fdbImportPath = new FormData();
        fdbImportPath.right = new FormAttachment(100, 0);
        fdbImportPath.top = new FormAttachment(wlImportPath, 0, SWT.CENTER);
        wbImportPath.setLayoutData(fdbImportPath);
        wbImportPath.addListener(SWT.Selection, this::browseTargetFolder);

        wImportPath = new TextVar(variables, shell, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
        props.setLook(wImportPath);
        FormData fdImportPath = new FormData();
        fdImportPath.left = new FormAttachment(middle, margin);
        fdImportPath.right = new FormAttachment(wbImportPath, -margin);
        fdImportPath.top = new FormAttachment(wlImportPath, 0, SWT.CENTER);
        wImportPath.setLayoutData(fdImportPath);
        lastControl = wImportPath;

        // Kettle properties path
        Label wlKettleProps = new Label(shell, SWT.RIGHT);
        props.setLook(wlKettleProps);
        wlKettleProps.setText("Path to kettle.properties ");
        FormData fdlKettleProps = new FormData();
        fdlKettleProps.left = new FormAttachment(0, 0);
        fdlKettleProps.right = new FormAttachment(middle, 0);
        fdlKettleProps.top = new FormAttachment(lastControl, margin);
        wlKettleProps.setLayoutData(fdlKettleProps);

        Button wbKettleProps = new Button(shell, SWT.PUSH);
        props.setLook(wbKettleProps);
        wbKettleProps.setText("Browse...");
        FormData fdbKettleProps = new FormData();
        fdbKettleProps.right = new FormAttachment(100, 0);
        fdbKettleProps.top = new FormAttachment(wlKettleProps, 0, SWT.CENTER);
        wbKettleProps.setLayoutData(fdbKettleProps);
        wbKettleProps.addListener(SWT.Selection, this::browseKettlePropsFile);

        wKettleProps = new TextVar(variables, shell, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
        props.setLook(wKettleProps);
        FormData fdKettleProps = new FormData();
        fdKettleProps.left = new FormAttachment(middle, margin);
        fdKettleProps.right = new FormAttachment(wbKettleProps, -margin);
        fdKettleProps.top = new FormAttachment(wlKettleProps, 0, SWT.CENTER);
        wKettleProps.setLayoutData(fdKettleProps);
        lastControl = wKettleProps;

        // Shared.xml path
        Label wlShared = new Label(shell, SWT.RIGHT);
        props.setLook(wlShared);
        wlShared.setText("Path to shared.xml");
        FormData fdlShared = new FormData();
        fdlShared.left = new FormAttachment(0, 0);
        fdlShared.right = new FormAttachment(middle, 0);
        fdlShared.top = new FormAttachment(lastControl, margin);
        wlShared.setLayoutData(fdlShared);

        Button wbShared = new Button(shell, SWT.PUSH);
        wbShared.setText("Browse...");
        FormData fdbShared = new FormData();
        fdbShared.right = new FormAttachment(100, 0);
        fdbShared.top = new FormAttachment(wlShared, 0, SWT.CENTER);
        wbShared.setLayoutData(fdbShared);
        wbShared.addListener(SWT.Selection, this::browseXmlFile);

        wShared = new TextVar(variables, shell, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
        props.setLook(wShared);
        FormData fdShared = new FormData();
        fdShared.left = new FormAttachment(middle, margin);
        fdShared.right = new FormAttachment(wbShared, -margin);
        fdShared.top = new FormAttachment(wlShared, 0, SWT.CENTER);
        wShared.setLayoutData(fdShared);
        lastControl = wShared;

        // Jdbc properties path
        Label wlJdbcProps = new Label(shell, SWT.RIGHT);
        props.setLook(wlJdbcProps);
        wlJdbcProps.setText("Path to jdbc.properties");
        FormData fdlJdbcProps = new FormData();
        fdlJdbcProps.left = new FormAttachment(0, 0);
        fdlJdbcProps.right = new FormAttachment(middle, 0);
        fdlJdbcProps.top = new FormAttachment(lastControl, margin);
        wlJdbcProps.setLayoutData(fdlJdbcProps);

        Button wbJdbcProps = new Button(shell, SWT.PUSH);
        props.setLook(wbJdbcProps);
        wbJdbcProps.setText("Browse...");
        FormData fdbJdbcProps = new FormData();
        fdbJdbcProps.right = new FormAttachment(100, 0);
        fdbJdbcProps.top = new FormAttachment(wlJdbcProps, 0, SWT.CENTER);
        wbJdbcProps.setLayoutData(fdbJdbcProps);
        wbJdbcProps.addListener(SWT.Selection, this::browseJdbcPropsFile);

        wJdbcProps = new TextVar(variables, shell, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
        props.setLook(wJdbcProps);
        FormData fdJdbcProps = new FormData();
        fdJdbcProps.left = new FormAttachment(middle, margin);
        fdJdbcProps.right = new FormAttachment(wbJdbcProps, -margin);
        fdJdbcProps.top = new FormAttachment(wlJdbcProps, 0, SWT.CENTER);
        wJdbcProps.setLayoutData(fdJdbcProps);
        lastControl = wJdbcProps;
        
        Button wbImport = new Button(shell, SWT.PUSH);
        props.setLook(wbImport);
        wbImport.setText("Import...");
        FormData fdbImport = new FormData();
        fdbImport.left = new FormAttachment(0, 0);
        fdbImport.right = new FormAttachment(middle, 0);
        fdbImport.top = new FormAttachment(lastControl, margin);
        wbImport.setLayoutData(fdbImport);
        wbImport.addListener(SWT.Selection, e -> { doImport(); });

        Label dummyLabel = new Label(shell, SWT.RIGHT);
        props.setLook(dummyLabel);
        dummyLabel.setText("");
        FormData fdDummy = new FormData();
        fdDummy.left = new FormAttachment(middle, margin);
        fdDummy.right = new FormAttachment(dummyLabel, -margin);
        fdDummy.top = new FormAttachment(dummyLabel, SWT.CENTER);
        dummyLabel.setLayoutData(fdDummy);
        lastControl = wbImport;

        BaseTransformDialog.setSize(shell);
        props.setDialogSize(shell, "ImportDialogSize");

        shell.open();

        while(!shell.isDisposed()){
            if(!display.readAndDispatch()){
                display.sleep();
            }
        }

//        return returnValue;

    }

    private void ok(){

    }

    private void cancel(){

    }

    public void dispose(){
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    private void browseHomeFolder( Event event ) {
        String homeFolder = BaseDialog.presentDirectoryDialog( shell, wImportFrom, variables );

        // Set the name to the base folder if the name is empty
        //
/*
        if (homeFolder!=null && StringUtils.isEmpty(wName.getText())) {
            File file = new File(homeFolder);
            wName.setText(Const.NVL(file.getName(), ""));
        }
*/
    }

    private void browseTargetFolder( Event event ) {
        String homeFolder = BaseDialog.presentDirectoryDialog(shell, wImportPath, variables);
    }

    private void browseKettlePropsFile(Event event){
        String filename = BaseDialog.presentFileDialog(shell, wKettleProps, variables, new String[]{"*.properties", "*.*"}, new String[]{"Properties files (*.properties)", "All Files (*.*)"}, true);
    }

    private void browseJdbcPropsFile(Event event){
        String filename = BaseDialog.presentFileDialog(shell, wJdbcProps, variables, new String[]{"*.properties", "*.*"}, new String[]{"Properties files (*.properties)", "All Files (*.*)"}, true);
    }

    private void browseXmlFile(Event event){
        String filename = BaseDialog.presentFileDialog(shell, wShared, variables, new String[]{"*.xml", "*.*"}, new String[]{"XML files (*.xml)", "All Files (*.*)"}, true);
    }

    private void doImport(){

        // Add gui option to delete everything in import folder.
        // Delete only ktr, kjb, definitely don't delete the entire folder

        HashMap<String, Object> importMap = new HashMap<String, Object>();
        importMap.put("importFromFolder", wImportFrom.getText());
        importMap.put("importToProject", "");
        importMap.put("importToFolder", wImportPath.getText());
        importMap.put("kettlePropertiesPath", wKettleProps.getText());
        importMap.put("sharedXmlPath", wShared.getText());
        importMap.put("jdbcPropsPath", wJdbcProps.getText());
        importMap.put("variables", variables);


        kettleImport.setInputFolder(wImportFrom.getText());
        kettleImport.setOutputFolder(wImportPath.getText());
        kettleImport.importHopFolder();
        variables = kettleImport.importVars(wKettleProps.getText(), HopVarImport.PROPERTIES, variables);
        kettleImport.importConnections(wShared.getText(), HopDbConnImport.XML);
        kettleImport.importConnections(wJdbcProps.getText(), HopDbConnImport.PROPERTIES);

        try {
            ExtensionPointHandler.callExtensionPoint(HopGui.getInstance().getLog(), variables, "HopProjectInformation", importMap);
        } catch (HopException e) {
            e.printStackTrace();
        }
    }

    private void showHideProjectFields(Event event){
        if(wcbimportInExisting.getSelection()){
            wComboImportTo.setEnabled(true);
            wImportPath.setEditable(false);
            wbImportPath.setEnabled(false);
        }else{
            wComboImportTo.setEnabled(false);
            wImportPath.setEditable(true);
            wbImportPath.setEnabled(true);
        }
    }
}
