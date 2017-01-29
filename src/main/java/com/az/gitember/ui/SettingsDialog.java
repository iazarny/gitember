package com.az.gitember.ui;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Igor_Azarny on 06.01.2017.
 */
public class SettingsDialog extends Dialog<SettingsModel>  {

    private final Logger log = Logger.getLogger(SettingsDialog.class.getName());

    public CheckBox rememberPasswords;
    public CheckBox useProxy;
    public TextField proxyServer;
    public TextField proxyPort;
    public CheckBox useProxyAuth;
    public TextField proxyUserName;
    public PasswordField proxyPassword;
    public CheckBox overwriteAuthorWithCommiter;

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
        }
    }


    private void bind() {

        Bindings.bindBidirectional(rememberPasswords.selectedProperty(), settingsModel.rememberPasswordsProperty());
        Bindings.bindBidirectional(useProxy.selectedProperty(), settingsModel.useProxyProperty());
        Bindings.bindBidirectional(proxyServer.textProperty(), settingsModel.proxyServerProperty());
        Bindings.bindBidirectional(proxyPort.textProperty(), settingsModel.proxyPortProperty());
        Bindings.bindBidirectional(useProxyAuth.selectedProperty(), settingsModel.useProxyAuthProperty());
        Bindings.bindBidirectional(proxyUserName.textProperty(), settingsModel.proxyUserNameProperty());
        Bindings.bindBidirectional(proxyPassword.textProperty(), settingsModel.proxyPasswordProperty());

        Bindings.bindBidirectional(overwriteAuthorWithCommiter.selectedProperty(), settingsModel.overwriteAuthorWithCommiterProperty());

    }



}
