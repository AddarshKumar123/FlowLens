package com.Flowlens.Flowlens.service;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;
import org.treesitter.TreeSitterJava;

import jakarta.annotation.PostConstruct;

import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.treesitter.TSNode;

@Service
public class TreeSitterService {
    RepositoryManager repositoryManager;
    TreeSitterService(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @PostConstruct
    public void onStartup() {
        try {
            parseFile("rootlytic_dashboard_API", "rootlytic\\src\\main\\java\\com\\project\\rootlytic\\configuration\\SecurityConfig.java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseFile(String repoName, String fileName) {
        TSParser parser = null;
        try {
            String fileContent = repositoryManager.readFileContent(repoName, fileName);
            parser = new TSParser();
            parser.setLanguage(new TreeSitterJava());
            TSTree tree = parser.parseString(null, fileContent);
            TSNode rootNode = tree.getRootNode();
            System.out.println("Root node type: " + rootNode.getType());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }
}
