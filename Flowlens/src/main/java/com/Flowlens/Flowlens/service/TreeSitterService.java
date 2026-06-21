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

    private void extractMethods(TSNode node, String source) {

        if(node.getType().equals("method_declaration")) {
            TSNode methodNameNode = node.getChildByFieldName("name");
            if (methodNameNode != null) {
                String methodName = source.substring(methodNameNode.getStartByte(), methodNameNode.getEndByte());
                System.out.println("Method: " + methodName);
                extractInvocations(node, source);
            }
        }

        for(int i=0;i<node.getChildCount();i++) {
            extractMethods(node.getChild(i), source);
        }
    }

    private void extractInvocations(TSNode node,String source) {

        if(node.getType().equals("method_invocation")) {

            TSNode nameNode =
                    node.getChildByFieldName("name");

            System.out.println(
                    source.substring(
                            nameNode.getStartByte(),
                            nameNode.getEndByte()
                    )
            );
        }

        for(int i=0;i<node.getChildCount();i++) {

            extractInvocations(node.getChild(i), source);

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
            extractMethods(rootNode, fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }
}
