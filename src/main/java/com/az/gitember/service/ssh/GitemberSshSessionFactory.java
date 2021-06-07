package com.az.gitember.service.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

public class GitemberSshSessionFactory extends JschConfigSessionFactory {

    private final String pathToKey;
    private final String password;

    public GitemberSshSessionFactory(final String pathToKey, final String password) {
        this.pathToKey = pathToKey;
        this.password = password;
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
        session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch jSch = super.createDefaultJSch(fs);
        jSch.addIdentity(
                pathToKey,
                isBlank(password) ? (null) : password.getBytes()
        );
        return jSch;
    }
    private boolean isBlank(final String str) {
        return str == null || str.trim().length() == 0;
    }

}
