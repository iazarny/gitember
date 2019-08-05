package com.az.gitember.ui;

import com.az.gitember.GitemberApp;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Igor_Azarny on 06.01.2017.
 */
public class SettingsDialog extends Dialog<SettingsModel>  {

    private final Logger log = Logger.getLogger(SettingsDialog.class.getName());

    @FXML
    private CheckBox rememberMeChkb;

    @FXML
    private TextField projectNameTxt;

    @FXML
    private TextField projectHameFolderTxt;

    @FXML
    private TextField userNameTxt;

    @FXML
    private TextField userEmailTxt;

    @FXML
    private TextField projectRemoteUrlTxt;

    @FXML
    private TextField projectKeyPathTxt;

    @FXML
    private PasswordField projectPwd;

    ///------------------------------------------------------------------
    ///------------------------------------------------------------------
    // proxy
    ///------------------------------------------------------------------
    ///------------------------------------------------------------------
    @FXML
    private CheckBox proxyUseChkb;

    @FXML
    private TextField proxyUserNameTxt;

    @FXML
    private TextField proxyPwd;

    @FXML
    private TextField proxyPortTxt;

    @FXML
    private TextField proxyServerTxt;

    private SettingsModel settingsModel;

    public SettingsDialog(final SettingsModel settingsModel) {
        super();
        this.settingsModel = settingsModel;
        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = getClass().getResource("/fxml/SettingsPane.fxml").openStream()) {
            fxmlLoader.setController(this);
            Parent settingsView = fxmlLoader.load(is);
            getDialogPane().setContent(settingsView);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            this.setResultConverter(
                    dialogButton -> {
                        if (dialogButton == ButtonType.OK) {
                            return settingsModel;
                        }
                        return null;
                    }
            );

            bind();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot load dialog", e);
            e.printStackTrace();
        }
    }


    private void bind() {



        if (GitemberApp.getRepositoryService() != null && GitemberApp.getRepositoryService().getRepository() != null) {


        }


        Repository repo = GitemberApp.getRepositoryService().getRepository();
        Config config = repo.getConfig();

        String projectName =  repo.getIdentifier();
        String projectHameFolder =  repo.getDirectory().getAbsolutePath();
        String name = config.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME);
        String email = config.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL);
        String projectRemoteUrl =  config.getString(ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME, ConfigConstants.CONFIG_KEY_URL);
        String projectKeyPath =  "";
        String projectPwd =  "";

        settingsModel.setProjectName(projectName);
        settingsModel.setProjectHameFolder(projectHameFolder);
        settingsModel.setUserName(name);
        settingsModel.setUserEmail(email);
        settingsModel.setProjectRemoteUrl(projectRemoteUrl);
        settingsModel.setProjectKeyPath(projectKeyPath);
        settingsModel.setProjectPwd(projectPwd);

        Bindings.bindBidirectional(this.projectNameTxt.textProperty(), settingsModel.projectNameProperty());
        Bindings.bindBidirectional(this.projectHameFolderTxt.textProperty(), settingsModel.projectHameFolderProperty());
        Bindings.bindBidirectional(this.userNameTxt.textProperty(), settingsModel.userNameProperty());
        Bindings.bindBidirectional(this.userEmailTxt.textProperty(), settingsModel.userEmailProperty());
        Bindings.bindBidirectional(this.projectRemoteUrlTxt.textProperty(), settingsModel.projectRemoteUrlProperty());
        Bindings.bindBidirectional(this.projectKeyPathTxt.textProperty(), settingsModel.projectKeyPathProperty());
        Bindings.bindBidirectional(this.projectPwd.textProperty(), settingsModel.projectPwdProperty());
        Bindings.bindBidirectional(this.rememberMeChkb.selectedProperty(), settingsModel.rememberMeProperty());

        ////////////////////// proxy

        Bindings.bindBidirectional(proxyUseChkb.selectedProperty(), settingsModel.useProxyProperty());
        Bindings.bindBidirectional(proxyServerTxt.textProperty(), settingsModel.proxyServerProperty());
        Bindings.bindBidirectional(proxyPortTxt.textProperty(), settingsModel.proxyPortProperty());
        Bindings.bindBidirectional(proxyUserNameTxt.textProperty(), settingsModel.proxyUserNameProperty());
        Bindings.bindBidirectional(proxyPwd.textProperty(), settingsModel.proxyPasswordProperty());
        proxyUseChkb.setSelected(false);
        settingsModel.useProxyProperty().setValue(false);



    }



}
