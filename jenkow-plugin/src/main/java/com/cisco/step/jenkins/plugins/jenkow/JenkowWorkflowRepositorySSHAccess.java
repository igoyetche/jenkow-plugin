package com.cisco.step.jenkins.plugins.jenkow;

import hudson.Extension;

import java.io.IOException;

import javax.inject.Inject;

import jenkins.model.Jenkins;

import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.jenkinsci.plugins.gitserver.RepositoryResolver;

/**
 * Exposes this repository over SSH.
 *
 * @author Max Spring
 */
@Extension
public class JenkowWorkflowRepositorySSHAccess extends RepositoryResolver {
    @Inject
    JenkowWorkflowRepository repo;

    @Override
    public ReceivePack createReceivePack(String fullRepositoryName) throws IOException, InterruptedException {
        if (isMine(fullRepositoryName)) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            ReceivePack rp = repo.createReceivePack(repo.openRepository());
            rp.setPostReceiveHook(new GitPostReceiveHook());
			return rp;
        }
        return null;
    }

    @Override
    public UploadPack createUploadPack(String fullRepositoryName) throws IOException, InterruptedException {
        if (isMine(fullRepositoryName)) return new UploadPack(repo.openRepository());
        return null;
    }

    private boolean isMine(String name) {
        if (name.startsWith("/")) name = name.substring(1);
        return name.equals(Consts.REPO_NAME+".git");
    }
}
