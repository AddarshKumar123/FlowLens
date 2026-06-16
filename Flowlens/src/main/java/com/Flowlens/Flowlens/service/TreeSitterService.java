package com.Flowlens.Flowlens.service;

import org.springframework.stereotype.Service;
import org.treesitter.TreeSitterJava;


@Service
public class TreeSitterService {
    RepositoryManager repositoryManager;
    TreeSitterService(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public void parseFile(String repoName, String fileName) {
        
    }
}
