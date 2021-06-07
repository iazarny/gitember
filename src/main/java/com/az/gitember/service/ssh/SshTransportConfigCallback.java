package com.az.gitember.service.ssh;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;

public class SshTransportConfigCallback implements TransportConfigCallback {

    private final GitemberSshSessionFactory sshSessionFactory;

    public SshTransportConfigCallback(final String pathToKey, final String password) {
        this.sshSessionFactory = new GitemberSshSessionFactory(pathToKey, password);
    }

    @Override
    public void configure(Transport transport) {
        SshTransport sshTransport = (SshTransport) transport;
        sshTransport.setSshSessionFactory(sshSessionFactory);
    }

}
