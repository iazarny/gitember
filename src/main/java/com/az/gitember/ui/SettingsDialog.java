package com.az.gitember.ui;

import com.az.gitember.GitemberApp;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Igor_Azarny on 06.01.2017.
 */
public class SettingsDialog extends Dialog<SettingsModel>  {

    private final Logger log = Logger.getLogger(SettingsDialog.class.getName());

    private CheckBox rememberPasswords;
    private CheckBox useProxy;
    private TextField proxyServer;
    private TextField proxyPort;
    private CheckBox useProxyAuth;
    private TextField proxyUserName;
    private PasswordField proxyPassword;
    private CheckBox overwriteAuthorWithCommiter;


    private TextField repoUserName;
    private TextField repoUserEmail;


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
            bind();
            this.setResultConverter(
                    dialogButton -> {
                        if (dialogButton == ButtonType.OK) {
                            return settingsModel;
                        }
                        return null;
                    }
            );
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot load dialog", e);
            e.printStackTrace();
        }
    }


    private void bind() {


        Repository repo = GitemberApp.getRepositoryService().getRepository();
        if (repo != null) {
            Config config = repo.getConfig();
            String repoUserNameString = config.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME);
            String repoUserEmailString = config.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL);

            settingsModel.repoUserNameProperty().setValue(repoUserNameString);
            settingsModel.repoUserEmailProperty().setValue(repoUserEmailString);

            Bindings.bindBidirectional(repoUserName.textProperty(), settingsModel.repoUserNameProperty());
            Bindings.bindBidirectional(repoUserEmail.textProperty(), settingsModel.repoUserEmailProperty());


        }

        Bindings.bindBidirectional(rememberPasswords.selectedProperty(), settingsModel.rememberPasswordsProperty());
        Bindings.bindBidirectional(useProxy.selectedProperty(), settingsModel.useProxyProperty());
        Bindings.bindBidirectional(proxyServer.textProperty(), settingsModel.proxyServerProperty());
        Bindings.bindBidirectional(proxyPort.textProperty(), settingsModel.proxyPortProperty());
        Bindings.bindBidirectional(useProxyAuth.selectedProperty(), settingsModel.useProxyAuthProperty());
        Bindings.bindBidirectional(proxyUserName.textProperty(), settingsModel.proxyUserNameProperty());
        Bindings.bindBidirectional(proxyPassword.textProperty(), settingsModel.proxyPasswordProperty());



    }



}
